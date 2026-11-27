package cz.fit.mit.Animal_Habitat_Sightings.service;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.model.Sighting;
import cz.fit.mit.Animal_Habitat_Sightings.repository.SightingImageRepository;
import cz.fit.mit.Animal_Habitat_Sightings.repository.SightingRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

@Service
public class SightingService {
    private final SightingRepository sightingRepository;
    private final SightingImageRepository sightingImageRepository;

    public SightingService(SightingRepository sightingRepository, SightingImageRepository sightingImageRepository) {
        this.sightingRepository = sightingRepository;
        this.sightingImageRepository = sightingImageRepository;
    }

    public long addSighting(Sighting sighting) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return sightingRepository.insertSighting(conn, sighting);
        }
    }

    public List<Sighting> getAllSightings() throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return sightingRepository.getAllSightings(conn);
        }
    }

    public Map<String, Object> getSightingDetails(long sightingId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return sightingRepository.getSightingDetails(conn, sightingId);
        }
    }

    public void deleteSighting(long sightingId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            sightingRepository.deleteSighting(conn, sightingId);
        }
    }

    public Map<String, Object> getSightingImage(long sightingId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return sightingImageRepository.getImage(conn, sightingId);
        }
    }
}
