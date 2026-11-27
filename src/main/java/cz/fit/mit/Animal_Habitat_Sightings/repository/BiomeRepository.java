package cz.fit.mit.Animal_Habitat_Sightings.repository;

import org.springframework.stereotype.Repository;
import cz.fit.mit.Animal_Habitat_Sightings.model.Biome;
import oracle.spatial.geometry.JGeometry;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BiomeRepository {
    public void insertBiome(Connection connection, Biome biome) throws Exception {
        String sql = """
                INSERT INTO biomes
                (biome_name, description, geom)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, biome.getBiomeName());
            stmt.setString(2, biome.getDescription());

            Struct obj = JGeometry.storeJS(connection, biome.getGeom());
            stmt.setObject(3, obj);

            stmt.executeUpdate();
        }
    }

    public List<Biome> getAllBiomes(Connection connection) throws SQLException {
        List<Biome> biomes = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM biomes");
             ResultSet rset = stmt.executeQuery()) {

            while (rset.next()) {
                Struct obj = (Struct) rset.getObject("geom");

                JGeometry geometry = obj != null
                        ? JGeometry.loadJS(obj)
                        : null;

                Biome biome = new Biome(
                        rset.getLong("biome_id"),
                        rset.getString("biome_name"),
                        rset.getString("description"),
                        geometry
                );

                biomes.add(biome);
            }
        }

        return biomes;
    }

    // Intersecting Habitats
    public Map<String, Object> getBiomeDetails(
            Connection connection, long biomeId) throws SQLException {

        String sql = """
        SELECT biome_id, biome_name, description,
            SDO_GEOM.SDO_AREA(geom, 0.05, 'unit=SQ_M') AS area,
            SDO_GEOM.SDO_LENGTH(geom, 0.05, 'unit=M') AS perimeter,
            SDO_GEOM.SDO_CENTROID(geom, 0.05) AS centroid
        FROM biomes
        WHERE biome_id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, biomeId);

            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    Struct centroidObj = (Struct) rset.getObject("centroid");

                    JGeometry centroid = centroidObj != null
                            ? JGeometry.loadJS(centroidObj)
                            : null;

                    double[] coordinates = centroid != null
                            ? centroid.getPoint()
                            : null;

                    Map<String, Object> details = new HashMap<>();

                    details.put("biomeId", rset.getLong("biome_id"));
                    details.put("biomeName", rset.getString("biome_name"));
                    details.put("description", rset.getString("description"));
                    details.put("area", rset.getDouble("area"));
                    details.put("perimeter", rset.getDouble("perimeter"));

                    if (coordinates != null) {
                        details.put("centroidLongitude", coordinates[0]);
                        details.put("centroidLatitude", coordinates[1]);
                    }

                    return details;
                }
            }
        }

        return null;
    }

    //Intersecting Habitats
    public List<Map<String, Object>> getHabitatsInBiome(Connection connection, long biomeId) throws SQLException {
        String sql = """
        SELECT h.habitat_id, h.habitat_name, a.animal_name
        FROM habitats h
        JOIN animals a ON h.animal_id = a.animal_id
        JOIN biomes b
            ON SDO_RELATE(
                h.geom,
                b.geom,
                'mask=ANYINTERACT'
            ) = 'TRUE'
        WHERE b.biome_id = ?
        """;

        List<Map<String, Object>> habitats = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, biomeId);

            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    Map<String, Object> habitat = new HashMap<>();

                    habitat.put("habitatId", rset.getLong("habitat_id"));
                    habitat.put("habitatName", rset.getString("habitat_name"));
                    habitat.put("animalName", rset.getString("animal_name"));

                    habitats.add(habitat);
                }
            }
        }

        return habitats;
    }

    // Distance to habitats
    public List<Map<String, Object>> getDistancesToHabitats(Connection connection, long biomeId) throws SQLException {
        String sql = """
        SELECT b.biome_id,  b.biome_name, h.habitat_id, h.habitat_name,
            SDO_GEOM.SDO_DISTANCE(  b.geom,  h.geom,
                0.05,
                'unit=M'
            ) AS distance
        FROM biomes b
        JOIN habitats h
            ON b.biome_id = ?
        ORDER BY distance
        """;

        List<Map<String, Object>> distances = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, biomeId);

            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    Map<String, Object> result = new HashMap<>();

                    result.put("biomeId", rset.getLong("biome_id"));
                    result.put("biomeName", rset.getString("biome_name"));
                    result.put("habitatId", rset.getLong("habitat_id"));
                    result.put("habitatName", rset.getString("habitat_name"));
                    result.put("distance", rset.getDouble("distance"));

                    distances.add(result);
                }
            }
        }

        return distances;
    }

    public void deleteBiome(Connection connection, long biomeId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM biomes WHERE biome_id = ?")) {
            stmt.setLong(1, biomeId);
            stmt.executeUpdate();
        }
    }
}

