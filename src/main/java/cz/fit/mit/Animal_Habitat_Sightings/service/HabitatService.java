package cz.fit.mit.Animal_Habitat_Sightings.service;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.model.Habitat;
import cz.fit.mit.Animal_Habitat_Sightings.repository.HabitatRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;

@Service
public class HabitatService {

    private final HabitatRepository habitatRepository;

    public HabitatService(HabitatRepository habitatRepository) {
        this.habitatRepository = habitatRepository;
    }

    public void addHabitat(Habitat habitat) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            habitatRepository.insertHabitat(conn, habitat);
        }
    }

    public List<Habitat> getAllHabitats() throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return habitatRepository.getAllHabitats(conn);
        }
    }

    public void deleteHabitat(long habitatId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            habitatRepository.deleteHabitat(conn, habitatId);
        }
    }
}
