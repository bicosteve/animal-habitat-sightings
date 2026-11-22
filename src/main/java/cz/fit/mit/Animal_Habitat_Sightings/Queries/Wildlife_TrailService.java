package cz.fit.mit.Animal_Habitat_Sightings.Queries;

import cz.fit.mit.Animal_Habitat_Sightings.Models.Wildlife_Trail;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class Wildlife_TrailService {
    public void insertTrail(Connection connection, Wildlife_Trail trail) throws Exception {
        String sql = """
                INSERT INTO wildlife_trails
                (trail_name, description, geom)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, trail.getTrail_name());
            stmt.setString(2, trail.getDescription());

            Struct obj = JGeometry.storeJS(connection, trail.getGeom());
            stmt.setObject(3, obj);

            stmt.executeUpdate();
        }
    }

    public List<Wildlife_Trail> getAllTrails(Connection connection) throws Exception {
        List<Wildlife_Trail> trails = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM wildlife_trails");
             ResultSet rset = stmt.executeQuery()) {

            while (rset.next()) {
                Struct obj = (Struct) rset.getObject("geom");

                JGeometry geometry = obj != null
                        ? JGeometry.loadJS(obj)
                        : null;

                Timestamp timestamp = rset.getTimestamp("created_at");

                Wildlife_Trail trail = new Wildlife_Trail(
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

