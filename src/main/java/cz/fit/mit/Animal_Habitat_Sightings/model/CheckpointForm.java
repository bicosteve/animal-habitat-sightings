package cz.fit.mit.Animal_Habitat_Sightings.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckpointForm {
    private String checkpointName;

    private String checkpointType;

    private String description;

    private int sdo_gtype;

    private int sdo_srid;

    private int[] sdo_elem_info;

    private double[] sdo_ordinates;
}
