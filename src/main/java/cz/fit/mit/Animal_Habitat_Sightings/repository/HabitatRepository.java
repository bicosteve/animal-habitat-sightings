package cz.fit.mit.Animal_Habitat_Sightings.repository;

import cz.fit.mit.Animal_Habitat_Sightings.model.Habitat;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class HabitatRepository {
    public void insertHabitat(Connection connection, Habitat habitat) throws Exception {
        String sql = ("""
                INSERT INTO habitats
                (habitat_name, animal_id, geom)
                values(?, ?, ?)
                """);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, habitat.getHabitatName());
            stmt.setLong(2, habitat.getAnimalId());

            if (habitat.getGeom() != null) {
                Struct obj = JGeometry.storeJS(connection, habitat.getGeom());
                stmt.setObject(3, obj);
            } else {
                stmt.setNull(3, Types.STRUCT);
            }
            stmt.executeUpdate();
        }
    }

    public List<Habitat> getAllHabitats(Connection connection) throws SQLException {
        List<Habitat> habitats = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM habitats")) {

            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    Struct obj = (Struct) rset.getObject("geom");

                    JGeometry geometry = obj != null
                            ? JGeometry.loadJS(obj)
                            : null;

                    Timestamp timestamp = rset.getTimestamp("created_at");
                    Habitat habitat = new Habitat(
                            rset.getLong("habitat_id"),
                            rset.getString("habitat_name"),
                            rset.getLong("animal_id"),
                            geometry,
                            timestamp != null ? timestamp.toLocalDateTime() : null
                    );

                    habitats.add(habitat);
                }
            }
        }

        return habitats;
    }

    public void deleteHabitat(Connection connection, long habitatId) throws SQLException {
        String sql = "DELETE FROM habitats WHERE habitat_id = ?";

        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, habitatId);
            stmt.executeUpdate();
        }
    }

    //Habitat details
    public Map<String, Object> getHabitatDetails(Connection connection, long habitatId) throws SQLException {
        String sql = """
            SELECT
                h.habitat_id,
                h.habitat_name,
                h.animal_id,
                a.animal_name,
                h.created_at,
                SDO_GEOM.SDO_AREA(h.geom, 0.05, 'unit=SQ_M') AS area,
                SDO_GEOM.SDO_LENGTH(h.geom, 0.05, 'unit=M') AS perimeter,
                SDO_GEOM.SDO_CENTROID(h.geom, 0.05) AS centroid
            FROM habitats h
            JOIN animals a ON h.animal_id = a.animal_id
            WHERE h.habitat_id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, habitatId);

            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    Struct centroidObj = (Struct) rset.getObject("centroid");
                    JGeometry centroid = centroidObj != null
                            ? JGeometry.loadJS(centroidObj)
                            : null;

                    double[] centroidPoint = centroid != null ? centroid.getPoint() : null;

                    Map<String, Object> details = new HashMap<>();
                    details.put("habitatId", rset.getLong("habitat_id"));
                    details.put("habitatName", rset.getString("habitat_name"));
                    details.put("animalName", rset.getString("animal_name"));
                    details.put("createdAt", rset.getTimestamp("created_at"));
                    details.put("area", rset.getDouble("area"));
                    details.put("perimeter", rset.getDouble("perimeter"));

                    if (centroidPoint != null) {
                        details.put("centroidLongitude", centroidPoint[0]);
                        details.put("centroidLatitude", centroidPoint[1]);
                    }

                    return details;
                }
            }
        }
        return null;
    }

    //Biomes that intersect a Habitat
    public List<Map<String, Object>> getIntersectingBiomes(
            Connection connection, long habitatId) throws SQLException {

        String sql = """
        SELECT
            h.habitat_id, h.habitat_name, b.biome_id, b.biome_name
        FROM habitats h
        JOIN biomes b
            ON SDO_RELATE( b.geom, h.geom,
                'mask=ANYINTERACT'
            ) = 'TRUE'
        WHERE h.habitat_id = ?
        """;

        List<Map<String, Object>> biomes = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, habitatId);

            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    Map<String, Object> biome = new HashMap<>();

                    biome.put("habitatId", rset.getLong("habitat_id"));
                    biome.put("habitatName", rset.getString("habitat_name"));
                    biome.put("biomeId", rset.getLong("biome_id"));
                    biome.put("biomeName", rset.getString("biome_name"));

                    biomes.add(biome);
                }
            }
        }

        return biomes;
    }

    //What sightings are inside the habitat
    public List<Map<String, Object>> getSightingsInHabitat(Connection connection, long habitatId) throws SQLException {
        String sql = """
        SELECT h.habitat_id, h.habitat_name,  s.sighting_id,  s.animal_id,  s.sighted_at,  s.notes
        FROM habitats h
        JOIN sightings s
            ON SDO_RELATE(  s.geom,  h.geom,
                'mask=INSIDE'
            ) = 'TRUE'
        WHERE h.habitat_id = ?
        """;

        List<Map<String, Object>> sightings = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, habitatId);

            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    Map<String, Object> sighting = new HashMap<>();

                    sighting.put("habitatId", rset.getLong("habitat_id"));
                    sighting.put("habitatName", rset.getString("habitat_name"));
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
}

