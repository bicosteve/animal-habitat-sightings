package cz.fit.mit.Animal_Habitat_Sightings.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import oracle.spatial.geometry.JGeometry;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sighting {
    private long sightingId;

    private long animalId;

    private LocalDateTime sightedAt;

    private String notes;

    private JGeometry geom;
}
