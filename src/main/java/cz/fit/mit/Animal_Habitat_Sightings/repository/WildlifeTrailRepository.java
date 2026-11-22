package cz.fit.mit.Animal_Habitat_Sightings.repository;

import cz.fit.mit.Animal_Habitat_Sightings.model.WildlifeTrail;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public void deleteTrail(Connection connection, long trailId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM wildlife_trails WHERE trail_id = ?")) {
            stmt.setLong(1, trailId);
            stmt.executeUpdate();
        }
    }
}

