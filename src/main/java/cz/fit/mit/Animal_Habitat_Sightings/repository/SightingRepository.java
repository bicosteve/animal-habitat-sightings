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
                SELECT s.sighting_id, s.animal_id, a.animal_name, s.sighted_at, s.notes, s.geom,
                       CASE WHEN EXISTS (
                           SELECT 1 FROM sighting_images i
                           WHERE i.sighting_id = s.sighting_id
                       ) THEN 1 ELSE 0 END AS has_image
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

                    Timestamp timestamp = rset.getTimestamp("sighted_at");

                    Map<String, Object> details = new HashMap<>();

                    details.put("sightingId", rset.getLong("sighting_id"));
                    details.put("animalId", rset.getLong("animal_id"));
                    details.put("animalName", rset.getString("animal_name"));
                    details.put("sightedAt", timestamp != null ? timestamp.toLocalDateTime() : null);
                    details.put("notes", rset.getString("notes"));
                    details.put("hasImage", rset.getInt("has_image") == 1);

                    if (coordinates != null && coordinates.length >= 2) {
                        details.put("longitude", coordinates[0]);
                        details.put("latitude", coordinates[1]);
                    }

                    return details;
                }
            }
        }

        return null;
    }


    public List<Map<String, Object>> getContainingBiomes(
            Connection connection, long sightingId) throws SQLException {

        List<Map<String, Object>> biomes = new ArrayList<>();

        String sql = """
                SELECT b.biome_id, b.biome_name
                FROM biomes b, sightings s
                WHERE s.sighting_id = ?
                  AND SDO_CONTAINS(b.geom, s.geom) = 'TRUE'
                ORDER BY b.biome_name
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, sightingId);

            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    Map<String, Object> biome = new HashMap<>();
                    biome.put("biomeId", rset.getLong("biome_id"));
                    biome.put("biomeName", rset.getString("biome_name"));
                    biomes.add(biome);
                }
            }
        }

        return biomes;
    }


    public void deleteSighting(Connection connection, long sightingId) throws SQLException {
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            runUpdate(connection,
                    "DELETE FROM sighting_images WHERE sighting_id = ?", sightingId);
            runUpdate(connection,
                    "DELETE FROM sightings WHERE sighting_id = ?", sightingId);

            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    private void runUpdate(Connection connection, String sql, long sightingId)
            throws SQLException {

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, sightingId);
            stmt.executeUpdate();
        }
    }
}
