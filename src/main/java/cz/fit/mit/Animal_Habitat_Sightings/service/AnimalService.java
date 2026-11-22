package cz.fit.mit.Animal_Habitat_Sightings.service;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.model.Animal;
import cz.fit.mit.Animal_Habitat_Sightings.repository.AnimalRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;

@Service
public class AnimalService {

    private final AnimalRepository animalRepository;

    public AnimalService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    public void addAnimal(Animal animal) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            animalRepository.insertAnimal(conn, animal);
        }
    }

    public List<Animal> getAllAnimals() throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return animalRepository.getAllAnimals(conn);
        }
    }

    public void deleteAnimal(long animalId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            animalRepository.deleteAnimal(conn, animalId);
        }
    }
}
