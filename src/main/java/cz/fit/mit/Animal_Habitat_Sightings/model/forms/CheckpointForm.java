package cz.fit.mit.Animal_Habitat_Sightings.model.forms;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckpointForm {

    @NotBlank(message = "Checkpoint name is required.")
    private String checkpointName;

    private String checkpointType;

    private String description;

    private int sdo_gtype;

    private int sdo_srid;

    @NotBlank(message = "Place the checkpoint on the map before saving.")
    private String sdo_elem_info;

    @NotBlank(message = "Place the checkpoint on the map before saving.")
    private String sdo_ordinates;
}
