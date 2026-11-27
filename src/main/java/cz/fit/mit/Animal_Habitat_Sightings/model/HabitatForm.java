package cz.fit.mit.Animal_Habitat_Sightings.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitatForm {

    @NotBlank(message = "Habitat name is required.")
    private String habitatName;

    @NotNull(message = "Please select an animal.")
    private Long animalId;

    private int sdo_gtype;

    private int sdo_srid;

    @NotBlank(message = "Draw the habitat polygon on the map before saving.")
    private String sdo_elem_info;

    @NotBlank(message = "Draw the habitat polygon on the map before saving.")
    private String sdo_ordinates;
}
