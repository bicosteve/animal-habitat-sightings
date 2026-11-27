package cz.fit.mit.Animal_Habitat_Sightings.repository;

import cz.fit.mit.Animal_Habitat_Sightings.model.Sighting;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SightingRepository {
    public long insertSighting(Connection connection, Sighting sighting) throws Exception {
        String sql = """
            INSERT INTO sightings
            (animal_id, sighted_at, notes, geom)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(
                sql, new String[]{"SIGHTING_ID"})) {

            stmt.setLong(1, sighting.getAnimalId());

            if (sighting.getSightedAt() != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(sighting.getSightedAt()));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }

            stmt.setString(3, sighting.getNotes());

            Struct obj = JGeometry.storeJS(connection, sighting.getGeom());
            stmt.setObject(4, obj);

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("Creating sighting failed, no ID returned.");
    }

    public List<Sighting> getAllSightings(Connection connection)
            throws SQLException {

        List<Sighting> sightings = new ArrayList<>();

        String sql = "SELECT * FROM sightings";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rset = stmt.executeQuery()) {

            while (rset.next()) {
                Struct obj = (Struct) rset.getObject("geom");

                JGeometry geometry = obj != null
                        ? JGeometry.loadJS(obj)
                        : null;

                Timestamp timestamp = rset.getTimestamp("sighted_at");

                Sighting sighting = new Sighting(
                        rset.getLong("sighting_id"),
                        rset.getLong("animal_id"),
                        timestamp != null ? timestamp.toLocalDateTime() : null,
                        rset.getString("notes"),
                        geometry
                );

                sightings.add(sighting);
            }
        }

        return sightings;
    }

    /**
     * Returns one row per sighting for the list page, joined to the animal name
     * and flagged with whether an image exists. The point coordinates are
     * extracted from the geometry for display.
     */
    public List<Map<String, Object>> getAllSightingsForList(Connection connection)
            throws SQLException {

        List<Map<String, Object>> rows = new ArrayList<>();

        String sql = """
                SELECT s.sighting_id,
                       s.animal_id,
                       a.animal_name,
                       s.sighted_at,
                       s.notes,
                       s.geom,
                       CASE WHEN EXISTS (
                           SELECT 1 FROM sighting_images i
                           WHERE i.sighting_id = s.sighting_id
                       ) THEN 1 ELSE 0 END AS has_image
                FROM sightings s
                JOIN animals a ON s.animal_id = a.animal_id
                ORDER BY s.sighted_at DESC
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rset = stmt.executeQuery()) {

            while (rset.next()) {
                Struct obj = (Struct) rset.getObject("geom");

                JGeometry geometry = obj != null
                        ? JGeometry.loadJS(obj)
                        : null;

                double[] coordinates = geometry != null
                        ? geometry.getPoint()
                        : null;

                Timestamp timestamp = rset.getTimestamp("sighted_at");

                Map<String, Object> row = new HashMap<>();
                row.put("sightingId", rset.getLong("sighting_id"));
                row.put("animalId", rset.getLong("animal_id"));
                row.put("animalName", rset.getString("animal_name"));
                row.put("sightedAt", timestamp != null ? timestamp.toLocalDateTime() : null);
                row.put("notes", rset.getString("notes"));
                row.put("hasImage", rset.getInt("has_image") == 1);

                if (coordinates != null && coordinates.length >= 2) {
                    row.put("longitude", coordinates[0]);
                    row.put("latitude", coordinates[1]);
                }

                rows.add(row);
            }
        }

        return rows;
    }

    public Map<String, Object> getSightingDetails(Connection connection, long sightingId) throws SQLException {
        String sql = """
                SELECT s.sighting_id, s.animal_id,  a.animal_name, s.sighted_at, s.notes, s.geom
                FROM sightings s
                JOIN animals a ON s.animal_id = a.animal_id
                WHERE s.sighting_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, sightingId);

            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    Struct obj = (Struct) rset.getObject("geom");

                    JGeometry geometry = obj != null
                            ? JGeometry.loadJS(obj)
                            : null;

                    double[] coordinates = geometry != null
                            ? geometry.getPoint()
                            : null;

                    Map<String, Object> details = new HashMap<>();

                    details.put("sightingId", rset.getLong("sighting_id"));
                    details.put("animalId", rset.getLong("animal_id"));
                    details.put("animalName", rset.getString("animal_name"));
                    details.put("sightedAt", rset.getTimestamp("sighted_at"));
                    details.put("notes", rset.getString("notes"));

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

    public void deleteSighting(Connection connection, long sightingId) throws SQLException {
        String sql = "DELETE FROM sightings WHERE sighting_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, sightingId);
            stmt.executeUpdate();
        }
    }
}
