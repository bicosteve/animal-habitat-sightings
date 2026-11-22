package cz.fit.mit.Animal_Habitat_Sightings.ApiControllers;


import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.Models.Checkpoint;
import cz.fit.mit.Animal_Habitat_Sightings.Models.CheckpointRequest;
import cz.fit.mit.Animal_Habitat_Sightings.Queries.CheckpointService;
import oracle.spatial.geometry.JGeometry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.util.List;

@RestController
@RequestMapping("/api/checkpoints")
public class CheckpointApiController {

    private final CheckpointService checkpointService = new CheckpointService();

    @PostMapping
    public ResponseEntity<String> addCheckpoint(@RequestBody CheckpointRequest request) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            JGeometry geometry = createGeometry(request);

            Checkpoint checkpoint = new Checkpoint(
                    0,
                    request.checkpoint_name,
                    request.checkpoint_type,
                    request.description,
                    geometry,
                    null
            );

            checkpointService.insertCheckpoint(conn, checkpoint);

            return ResponseEntity.ok("Checkpoint inserted successfully");
        }
    }

    @GetMapping
    public ResponseEntity<List<Checkpoint>> getAllCheckpoints() throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            List<Checkpoint> checkpoints =
                    checkpointService.getAllCheckpoints(conn);

            return ResponseEntity.ok(checkpoints);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCheckpoint(@PathVariable long id) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {

            checkpointService.deleteCheckpoint(conn, id);

            return ResponseEntity.ok("Checkpoint deleted successfully");
        }
    }

    private JGeometry createGeometry(CheckpointRequest request) {
        return JGeometry.createPoint(
                request.sdo_ordinates,
                2,
                request.sdo_srid
        );
    }
}