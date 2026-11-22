package cz.fit.mit.Animal_Habitat_Sightings.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import oracle.spatial.geometry.JGeometry;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Checkpoint {

    private long checkpointId;

    private String checkpointName;

    private String checkpointType;

    private String description;

    private JGeometry geom;

    private LocalDateTime createdAt;
}
