package cz.fit.mit.Animal_Habitat_Sightings.repository;

import cz.fit.mit.Animal_Habitat_Sightings.model.Animal;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AnimalRepository {
    public void insertAnimal(Connection connection, Animal animal) throws SQLException {
        String sql = """
                INSERT INTO animals
                (animal_name, description)
                VALUES (?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, animal.getAnimalName());
            stmt.setString(2, animal.getDescription());

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
                        rset.getString("description")
                );

                animals.add(animal);
            }
        }

        return animals;
    }

    public Animal getAnimalById(Connection connection, long animalId) throws SQLException {
        String sql = "SELECT animal_id, animal_name, description FROM animals WHERE animal_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, animalId);

            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    return new Animal(
                            rset.getLong("animal_id"),
                            rset.getString("animal_name"),
                            rset.getString("description"));
                }
            }
        }

        return null;
    }

    public void updateAnimal(Connection connection, Animal animal) throws SQLException {
        String sql = """
                UPDATE animals
                SET animal_name = ?, description = ?
                WHERE animal_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, animal.getAnimalName());
            stmt.setString(2, animal.getDescription());
            stmt.setLong(3, animal.getAnimalId());

            stmt.executeUpdate();
        }
    }

    /**
     * Counts the rows that reference this animal, so the UI can warn about the
     * impact of a cascade delete before it happens.
     *
     * @return a map with keys {@code sightings} and {@code habitats}.
     */
    public Map<String, Integer> countReferences(Connection connection, long animalId)
            throws SQLException {

        Map<String, Integer> counts = new HashMap<>();
        counts.put("sightings", countWhere(connection,
                "SELECT COUNT(*) FROM sightings WHERE animal_id = ?", animalId));
        counts.put("habitats", countWhere(connection,
                "SELECT COUNT(*) FROM habitats WHERE animal_id = ?", animalId));
        return counts;
    }

    private int countWhere(Connection connection, String sql, long animalId)
            throws SQLException {

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, animalId);

            try (ResultSet rset = stmt.executeQuery()) {
                return rset.next() ? rset.getInt(1) : 0;
            }
        }
    }

    public void deleteAnimal(Connection connection, long animalId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM animals WHERE animal_id = ?")) {
            stmt.setLong(1, animalId);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes the animal and everything that references it, in one transaction.
     *
     * <p>The FK chain is: animals &larr; sightings &larr; sighting_images, and
     * animals &larr; habitats (a leaf). We therefore delete in dependency
     * order: sighting_images, sightings, habitats, then the animal itself. The
     * whole thing is atomic -- either all rows go, or none do.
     */
    public void cascadeDeleteAnimal(Connection connection, long animalId) throws SQLException {
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            String deleteImages = """
                    DELETE FROM sighting_images
                    WHERE sighting_id IN (
                        SELECT sighting_id FROM sightings WHERE animal_id = ?
                    )
                    """;
            runUpdate(connection, deleteImages, animalId);

            runUpdate(connection,
                    "DELETE FROM sightings WHERE animal_id = ?", animalId);
            runUpdate(connection,
                    "DELETE FROM habitats WHERE animal_id = ?", animalId);
            runUpdate(connection,
                    "DELETE FROM animals WHERE animal_id = ?", animalId);

            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    private void runUpdate(Connection connection, String sql, long animalId)
            throws SQLException {

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, animalId);
            stmt.executeUpdate();
        }
    }
}
