package cz.fit.mit.Animal_Habitat_Sightings.model.forms;

import lombok.Data;

@Data
public class WildlifeTrailForm {
    private String trailName;

    private String description;

    private int sdo_gtype;

    private int sdo_srid;

    private int[] sdo_elem_info;

    private double[] sdo_ordinates;
}
