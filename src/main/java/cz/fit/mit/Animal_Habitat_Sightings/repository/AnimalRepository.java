package cz.fit.mit.Animal_Habitat_Sightings.repository;

import cz.fit.mit.Animal_Habitat_Sightings.model.Animal;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AnimalRepository {
    public void insertAnimal(Connection connection, Animal animal) throws SQLException {
        String sql = """
                INSERT INTO animals
                (animal_name, scientific_name, description)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, animal.getAnimalName());
            stmt.setString(2, animal.getScientificName());
            stmt.setString(3, animal.getDescription());

            stmt.executeUpdate();
        }
    }

    public List<Animal> getAllAnimals(Connection connection) throws SQLException {
        List<Animal> animals = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM animals");
             ResultSet rset = stmt.executeQuery()) {

            while (rset.next()) {
                Animal animal = new Animal(
                        rset.getLong("animal_id"),
                        rset.getString("animal_name"),
                        rset.getString("scientific_name"),
                        rset.getString("description")
                );

                animals.add(animal);
            }
        }

        return animals;
    }

    public void deleteAnimal(Connection connection, long animalId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM animals WHERE animal_id = ?")) {
            stmt.setLong(1, animalId);
            stmt.executeUpdate();
        }
    }
}
