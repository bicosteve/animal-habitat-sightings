package cz.fit.mit.Animal_Habitat_Sightings.repository;

import org.springframework.stereotype.Repository;
import cz.fit.mit.Animal_Habitat_Sightings.model.Biome;
import oracle.spatial.geometry.JGeometry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public void deleteBiome(Connection connection, long biomeId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM biomes WHERE biome_id = ?")) {
            stmt.setLong(1, biomeId);
            stmt.executeUpdate();
        }
    }
}

