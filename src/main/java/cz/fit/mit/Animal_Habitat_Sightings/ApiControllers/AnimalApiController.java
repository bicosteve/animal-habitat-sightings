package cz.fit.mit.Animal_Habitat_Sightings.ApiControllers;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.Models.Animal;
import cz.fit.mit.Animal_Habitat_Sightings.Queries.AnimalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/animals")
public class AnimalApiController {

    private final AnimalService animalService;

    public AnimalApiController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @PostMapping
    public ResponseEntity<String> addAnimal(@RequestBody Animal animal) throws SQLException {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {

            animalService.insertAnimal(conn, animal);

            return ResponseEntity.ok("Animal inserted successfully");
        }
    }

    @GetMapping
    public List<Animal> listAnimals() throws SQLException {
        try (Connection conn =
                     AnimalHabitatSightingsApplication.getConnection()) {

            return animalService.getAllAnimals(conn);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAnimal(@PathVariable long id) throws SQLException {
        try (Connection conn =
                     AnimalHabitatSightingsApplication.getConnection()) {

            animalService.deleteAnimal(conn, id);

            return ResponseEntity.ok("Animal deleted successfully");
        }
    }
}