package cz.fit.mit.Animal_Habitat_Sightings.models;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import oracle.spatial.geometry.JGeometry;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Habitat {
    public long habitatId;
    public String habitatName;
    public long animalId;
    public long biomeId;
    public JGeometry geom;
    public LocalDateTime createdAt;
}
