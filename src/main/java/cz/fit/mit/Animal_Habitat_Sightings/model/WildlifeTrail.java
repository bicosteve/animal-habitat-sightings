package cz.fit.mit.Animal_Habitat_Sightings.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import oracle.spatial.geometry.JGeometry;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WildlifeTrail {

    private long trailId;

    private String trailName;

    private String description;

    private JGeometry geom;

    private LocalDateTime createdAt;
}
