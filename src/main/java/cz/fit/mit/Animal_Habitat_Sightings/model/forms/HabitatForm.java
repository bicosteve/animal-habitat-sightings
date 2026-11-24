package cz.fit.mit.Animal_Habitat_Sightings.model.forms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitatForm {

    private String habitatName;

    private Long animalId;

    private int sdo_gtype;

    private int sdo_srid;

    private String sdo_elem_info;

    private String sdo_ordinates;
}
