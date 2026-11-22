package cz.fit.mit.Animal_Habitat_Sightings.ApiControllers;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.Models.Biome;
import cz.fit.mit.Animal_Habitat_Sightings.Models.BiomeRequest;
import cz.fit.mit.Animal_Habitat_Sightings.Queries.BiomeService;
import oracle.spatial.geometry.JGeometry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/biomes")
public class BiomeApiController {

    private final BiomeService biomeService;

    public BiomeApiController(BiomeService biomeService) {
        this.biomeService = biomeService;
    }

    @PostMapping
    public ResponseEntity<String> addBiome(@RequestBody BiomeRequest request) throws Exception {
        try (Connection conn =
                     AnimalHabitatSightingsApplication.getConnection()) {
            JGeometry geometry = createGeometry(request);

            Biome biome = new Biome(
                    0,
                    request.biome_name,
                    request.description,
                    geometry
            );

            biomeService.insertBiome(conn, biome);

            return ResponseEntity.ok("Biome inserted successfully");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBiome(@PathVariable long id) throws SQLException {
        try (Connection conn =
                     AnimalHabitatSightingsApplication.getConnection()) {

            biomeService.deleteBiome(conn, id);

            return ResponseEntity.ok("Biome deleted successfully");
        }
    }

    @GetMapping
    public List<Biome> listBiomes() throws SQLException {
        try (Connection conn =
                     AnimalHabitatSightingsApplication.getConnection()) {

            return biomeService.getAllBiomes(conn);
        }
    }

    private JGeometry createGeometry(BiomeRequest request) {
        return new JGeometry(
                request.sdo_gtype,
                request.sdo_srid,
                request.sdo_elem_info,
                request.sdo_ordinates
        );
    }
}
