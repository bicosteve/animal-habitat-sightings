package cz.fit.mit.Animal_Habitat_Sightings.service;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.model.Biome;
import cz.fit.mit.Animal_Habitat_Sightings.repository.BiomeRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

@Service
public class BiomeService {

    private final BiomeRepository biomeRepository;

    public BiomeService(BiomeRepository biomeRepository) {
        this.biomeRepository = biomeRepository;
    }

    public void addBiome(Biome biome) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            biomeRepository.insertBiome(conn, biome);
        }
    }

    public List<Biome> getAllBiomes() throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return biomeRepository.getAllBiomes(conn);
        }
    }

    public Map<String, Object> getBiomeDetails(long biomeId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return biomeRepository.getBiomeDetails(conn, biomeId);
        }
    }

    public List<Map<String, Object>> getHabitatsInBiome(long biomeId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return biomeRepository.getHabitatsInBiome(conn, biomeId);
        }
    }

    public List<Map<String, Object>> getDistancesToHabitats(long biomeId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return biomeRepository.getDistancesToHabitats(conn, biomeId);
        }
    }


    public void deleteBiome(long biomeId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            biomeRepository.deleteBiome(conn, biomeId);
        }
    }
}
