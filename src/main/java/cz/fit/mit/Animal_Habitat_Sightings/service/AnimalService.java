package cz.fit.mit.Animal_Habitat_Sightings.service;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.model.Animal;
import cz.fit.mit.Animal_Habitat_Sightings.repository.AnimalRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

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

    public Animal getAnimalById(long animalId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return animalRepository.getAnimalById(conn, animalId);
        }
    }

    public void updateAnimal(Animal animal) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            animalRepository.updateAnimal(conn, animal);
        }
    }

    /**
     * Returns how many sightings and habitats reference this animal, so the UI
     * can warn before a cascade delete.
     */
    public Map<String, Integer> getDeletionImpact(long animalId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return animalRepository.countReferences(conn, animalId);
        }
    }

    /**
     * Deletes the animal and all rows that reference it (sightings, their
     * images, and habitats) in a single transaction.
     */
    public void deleteAnimal(long animalId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            animalRepository.cascadeDeleteAnimal(conn, animalId);
        }
    }
}
