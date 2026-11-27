package cz.fit.mit.Animal_Habitat_Sightings.model.forms;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WildlifeTrailForm {

    @NotBlank(message = "Trail name is required.")
    private String trailName;

    private String description;

    private int sdo_gtype;

    private int sdo_srid;

    @NotBlank(message = "Draw the trail line on the map before saving.")
    private String sdo_elem_info;

    @NotBlank(message = "Draw the trail line on the map before saving.")
    private String sdo_ordinates;
}
