package cz.fit.mit.Animal_Habitat_Sightings.model.forms;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiomeForm {

    @NotBlank(message = "Biome name is required.")
    private String biomeName;

    private String description;

    private int sdo_gtype;

    private int sdo_srid;

    @NotBlank(message = "Draw the biome polygon on the map before saving.")
    private String sdo_elem_info;

    @NotBlank(message = "Draw the biome polygon on the map before saving.")
    private String sdo_ordinates;
}
