package cz.fit.mit.Animal_Habitat_Sightings.repository;

import cz.fit.mit.Animal_Habitat_Sightings.model.Checkpoint;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CheckpointRepository {
    public void insertCheckpoint(Connection connection, Checkpoint checkpoint) throws Exception {
        String sql = """
                INSERT INTO checkpoints
                (checkpoint_name, checkpoint_type, description, geom)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, checkpoint.getCheckpointName());
            stmt.setString(2, checkpoint.getCheckpointType());
            stmt.setString(3, checkpoint.getDescription());

            Struct obj = JGeometry.storeJS(connection, checkpoint.getGeom());
            stmt.setObject(4, obj);

            stmt.executeUpdate();
        }
    }

    public List<Checkpoint> getAllCheckpoints(Connection connection) throws Exception {
        List<Checkpoint> checkpoints = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM checkpoints");
             ResultSet rset = stmt.executeQuery()) {

            while (rset.next()) {

                Struct obj = (Struct) rset.getObject("geom");
                JGeometry geometry = JGeometry.loadJS(obj);

                Timestamp timestamp = rset.getTimestamp("created_at");

                Checkpoint checkpoint = new Checkpoint(
                        rset.getLong("checkpoint_id"),
                        rset.getString("checkpoint_name"),
                        rset.getString("checkpoint_type"),
                        rset.getString("description"),
                        geometry,
                        timestamp != null ? timestamp.toLocalDateTime() : null
                );

                checkpoints.add(checkpoint);
            }
        }

        return checkpoints;
    }

    public void deleteCheckpoint(Connection connection, long checkpoint_id) throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM checkpoints WHERE checkpoint_id = ?")) {
            stmt.setLong(1, checkpoint_id);
            stmt.executeUpdate();
        }
    }

    public Map<String, Object> getCheckpointDetails(
            Connection connection, long checkpointId) throws SQLException {

        String sql = """
        SELECT checkpoint_id, checkpoint_name, checkpoint_type, description, created_at,
            SDO_GEOM.SDO_POINTONSURFACE(geom, 0.05) AS point_location
        FROM checkpoints
        WHERE checkpoint_id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, checkpointId);

            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    Struct pointObj = (Struct) rset.getObject("point_location");

                    JGeometry point = pointObj != null
                            ? JGeometry.loadJS(pointObj)
                            : null;

                    double[] coordinates = point != null ? point.getPoint() : null;

                    Map<String, Object> details = new HashMap<>();

                    details.put("checkpointId", rset.getLong("checkpoint_id"));
                    details.put("checkpointName", rset.getString("checkpoint_name"));
                    details.put("checkpointType", rset.getString("checkpoint_type"));
                    details.put("description", rset.getString("description"));
                    details.put("createdAt", rset.getTimestamp("created_at"));

                    if (coordinates != null) {
                        details.put("longitude", coordinates[0]);
                        details.put("latitude", coordinates[1]);
                    }

                    return details;
                }
            }
        }

        return null;
    }

    // Get all checkpoints in a habitat
    public List<Map<String, Object>> getCheckpointsInHabitat(
            Connection connection, long habitatId) throws SQLException {

        String sql = """
        SELECT c.checkpoint_id, c.checkpoint_name, c.checkpoint_type, c.description
        FROM checkpoints c
        JOIN habitats h
            ON SDO_RELATE( c.geom, h.geom,
                'mask=INSIDE'
            ) = 'TRUE'
        WHERE h.habitat_id = ?
        """;

        List<Map<String, Object>> checkpoints = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, habitatId);

            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    Map<String, Object> checkpoint = new HashMap<>();

                    checkpoint.put("checkpointId", rset.getLong("checkpoint_id"));
                    checkpoint.put("checkpointName", rset.getString("checkpoint_name"));
                    checkpoint.put("checkpointType", rset.getString("checkpoint_type"));
                    checkpoint.put("description", rset.getString("description"));

                    checkpoints.add(checkpoint);
                }
            }
        }

        return checkpoints;
    }
}
