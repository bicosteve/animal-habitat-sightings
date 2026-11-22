package cz.fit.mit.Animal_Habitat_Sightings.service;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.model.WildlifeTrail;
import cz.fit.mit.Animal_Habitat_Sightings.repository.WildlifeTrailRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;

@Service
public class WildlifeTrailService {

    private final WildlifeTrailRepository trailRepository;

    public WildlifeTrailService(WildlifeTrailRepository trailRepository) {
        this.trailRepository = trailRepository;
    }

    public void addTrail(WildlifeTrail trail) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            trailRepository.insertTrail(conn, trail);
        }
    }

    public List<WildlifeTrail> getAllTrails() throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return trailRepository.getAllTrails(conn);
        }
    }

    public void deleteTrail(long trailId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            trailRepository.deleteTrail(conn, trailId);
        }
    }
}
