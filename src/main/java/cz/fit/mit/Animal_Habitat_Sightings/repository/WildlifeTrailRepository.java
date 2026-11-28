package cz.fit.mit.Animal_Habitat_Sightings.repository;

import cz.fit.mit.Animal_Habitat_Sightings.model.WildlifeTrail;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class WildlifeTrailRepository {
    public void insertTrail(Connection connection, WildlifeTrail trail) throws Exception {
        String sql = """
                INSERT INTO wildlife_trails
                (trail_name, description, geom)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, trail.getTrailName());
            stmt.setString(2, trail.getDescription());

            Struct obj = JGeometry.storeJS(connection, trail.getGeom());
            stmt.setObject(3, obj);

            stmt.executeUpdate();
        }
    }

    public List<WildlifeTrail> getAllTrails(Connection connection) throws Exception {
        List<WildlifeTrail> trails = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM wildlife_trails");
             ResultSet rset = stmt.executeQuery()) {

            while (rset.next()) {
                Struct obj = (Struct) rset.getObject("geom");

                JGeometry geometry = obj != null
                        ? JGeometry.loadJS(obj)
                        : null;

                Timestamp timestamp = rset.getTimestamp("created_at");

                WildlifeTrail trail = new WildlifeTrail(
                        rset.getLong("trail_id"),
                        rset.getString("trail_name"),
                        rset.getString("description"),
                        geometry,
                        timestamp != null ? timestamp.toLocalDateTime() : null
                );

                trails.add(trail);
            }
        }

        return trails;
    }

    //Trail details
    public Map<String, Object> getTrailDetails(Connection connection, long trailId) throws SQLException {
        String sql = """
        SELECT trail_id, trail_name, description, created_at,
            SDO_GEOM.SDO_LENGTH(geom, 0.05, 'unit=M') AS trail_length
        FROM wildlife_trails
        WHERE trail_id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, trailId);

            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    Map<String, Object> details = new HashMap<>();

                    details.put("trailId", rset.getLong("trail_id"));
                    details.put("trailName", rset.getString("trail_name"));
                    details.put("description", rset.getString("description"));
                    details.put("createdAt", rset.getTimestamp("created_at"));
                    details.put("length", rset.getDouble("trail_length"));

                    return details;
                }
            }
        }

        return null;
    }


    public JGeometry getTrailGeometry(Connection connection, long trailId) throws SQLException {
        String sql = "SELECT geom FROM wildlife_trails WHERE trail_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, trailId);

            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    Struct obj = (Struct) rset.getObject("geom");
                    return obj != null ? JGeometry.loadJS(obj) : null;
                }
            }
        }

        return null;
    }

    // Habitats the trail intersects
    public List<Map<String, Object>> getTrailsIntersectingHabitat(Connection connection, long habitatId) throws SQLException {
        String sql = """
        SELECT t.trail_id, t.trail_name, t.description
        FROM wildlife_trails t
        JOIN habitats h
            ON SDO_RELATE(t.geom,  h.geom,
                'mask=ANYINTERACT'
            ) = 'TRUE'
        WHERE h.habitat_id = ?
        """;

        List<Map<String, Object>> trails = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, habitatId);

            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    Map<String, Object> trail = new HashMap<>();

                    trail.put("trailId", rset.getLong("trail_id"));
                    trail.put("trailName", rset.getString("trail_name"));
                    trail.put("description", rset.getString("description"));

                    trails.add(trail);
                }
            }
        }

        return trails;
    }

    // Sigthings within 100m of a trail
    public List<Map<String, Object>> getSightingsNearTrail(
            Connection connection, long trailId, double distance) throws SQLException {

        String sql = """
        SELECT s.sighting_id, s.animal_id, s.sighted_at, s.notes
        FROM sightings s
        JOIN wildlife_trails t
            ON SDO_WITHIN_DISTANCE( s.geom, t.geom, ?
            ) = 'TRUE'
        WHERE t.trail_id = ?
        """;

        List<Map<String, Object>> sightings = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "distance=" + distance + " unit=M");
            stmt.setLong(2, trailId);

            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    Map<String, Object> sighting = new HashMap<>();

                    sighting.put("sightingId", rset.getLong("sighting_id"));
                    sighting.put("animalId", rset.getLong("animal_id"));
                    sighting.put("sightedAt", rset.getTimestamp("sighted_at"));
                    sighting.put("notes", rset.getString("notes"));

                    sightings.add(sighting);
                }
            }
        }

        return sightings;
    }

    public void updateTrail(Connection connection, WildlifeTrail trail) throws Exception {
        String sql = """
                UPDATE wildlife_trails
                SET trail_name = ?,
                    description = ?,
                    geom = ?
                WHERE trail_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, trail.getTrailName());
            stmt.setString(2, trail.getDescription());

            Struct obj = JGeometry.storeJS(connection, trail.getGeom());
            stmt.setObject(3, obj);

            stmt.setLong(4, trail.getTrailId());

            stmt.executeUpdate();
        }
    }

    public void deleteTrail(Connection connection, long trailId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM wildlife_trails WHERE trail_id = ?")) {
            stmt.setLong(1, trailId);
            stmt.executeUpdate();
        }
    }
}

