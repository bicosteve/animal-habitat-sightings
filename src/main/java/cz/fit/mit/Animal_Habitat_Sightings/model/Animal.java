package cz.fit.mit.Animal_Habitat_Sightings.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Animal {

    private long animalId;

    @NotBlank(message = "Animal name is required.")
    private String animalName;

    private String description;
}
