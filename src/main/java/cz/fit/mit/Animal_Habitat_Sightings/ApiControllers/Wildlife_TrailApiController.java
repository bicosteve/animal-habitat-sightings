package cz.fit.mit.Animal_Habitat_Sightings.ApiControllers;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.Models.Wildlife_Trail;
import cz.fit.mit.Animal_Habitat_Sightings.Models.Wildlife_TrailRequest;
import cz.fit.mit.Animal_Habitat_Sightings.Queries.Wildlife_TrailService;
import oracle.spatial.geometry.JGeometry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.util.List;

@RestController
@RequestMapping("/api/trails")
public class Wildlife_TrailApiController {

    private final Wildlife_TrailService trailService = new Wildlife_TrailService();

    @PostMapping
    public ResponseEntity<String> addTrail(@RequestBody Wildlife_TrailRequest request) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            JGeometry geometry = createGeometry(request);
            Wildlife_Trail trail = new Wildlife_Trail(
                    0,
                    request.trail_name,
                    request.description,
                    geometry,
                    null
            );
            trailService.insertTrail(conn, trail);

            return ResponseEntity.ok("Wildlife trail inserted successfully");
        }
    }

    @GetMapping
    public ResponseEntity<List<Wildlife_Trail>> getAllTrails() throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            List<Wildlife_Trail> trails = trailService.getAllTrails(conn);

            return ResponseEntity.ok(trails);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTrail(@PathVariable long id) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            trailService.deleteTrail(conn, id);

            return ResponseEntity.ok("Wildlife trail deleted successfully");
        }
    }

    private JGeometry createGeometry(Wildlife_TrailRequest request) {
        return new JGeometry(
                request.sdo_gtype,
                request.sdo_srid,
                request.sdo_elem_info,
                request.sdo_ordinates
        );
    }
}