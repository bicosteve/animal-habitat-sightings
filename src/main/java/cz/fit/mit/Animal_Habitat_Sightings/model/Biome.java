package cz.fit.mit.Animal_Habitat_Sightings.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import oracle.spatial.geometry.JGeometry;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Biome {

    // Geographical notation like forest, rain

    private long biomeId;

    private String biomeName;

    private String description;

    private JGeometry geom;
}
