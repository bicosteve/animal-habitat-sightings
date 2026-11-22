package cz.fit.mit.Animal_Habitat_Sightings.ApiControllers;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.Models.Habitat;
import cz.fit.mit.Animal_Habitat_Sightings.Models.HabitatRequest;
import cz.fit.mit.Animal_Habitat_Sightings.Queries.HabitatService;
import oracle.spatial.geometry.JGeometry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/habitats")
public class HabitatApiController {
    private final HabitatService habitatService;

    public HabitatApiController(HabitatService habitatService) {
        this.habitatService = habitatService;
    }

    @PostMapping
    public ResponseEntity<String> addHabitat(
            @RequestBody HabitatRequest request) throws  Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            JGeometry geometry = createGeometry(request);

            Habitat habitat = new Habitat(0, request.habitat_name, request.animal_id, geometry, null);
            habitatService.insertHabitat(conn, habitat);
            return ResponseEntity.ok("Habitat inserted successfully");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHabitat(
            @PathVariable long id) throws SQLException {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            habitatService.deleteHabitat(conn, id);
            return ResponseEntity.ok("Habitat deleted successfully");
        }
    }

    @GetMapping
    public List<Habitat> listHabitats() throws SQLException {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return habitatService.getAllHabitats(conn);
        }
    }

    private JGeometry createGeometry(HabitatRequest request) {
        return new JGeometry(
                request.sdo_gtype,
                request.sdo_srid,
                request.sdo_elem_info,
                request.sdo_ordinates
        );
    }
}
