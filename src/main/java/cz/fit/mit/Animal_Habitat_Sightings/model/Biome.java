package cz.fit.mit.Animal_Habitat_Sightings.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import oracle.spatial.geometry.JGeometry;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Biome {

    private long biomeId;

    private String biomeName;

    private String description;

    private JGeometry geom;
}
