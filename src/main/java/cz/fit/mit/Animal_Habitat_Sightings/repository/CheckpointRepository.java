package cz.fit.mit.Animal_Habitat_Sightings.repository;

import cz.fit.mit.Animal_Habitat_Sightings.model.Checkpoint;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

                JGeometry geometry = obj != null
                        ? JGeometry.loadJS(obj)
                        : null;

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
}
