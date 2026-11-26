package cz.fit.mit.Animal_Habitat_Sightings.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import oracle.spatial.geometry.JGeometry;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Habitat {

    private long habitatId;

    private String habitatName;

    private long animalId;

    private JGeometry geom;

    private LocalDateTime createdAt;
}
