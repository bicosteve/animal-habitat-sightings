package cz.fit.mit.Animal_Habitat_Sightings.model;

import lombok.Data;

/**
 * Form-backing object for the "Add Habitat" page (habitat-form.html).
 * Property names match the th:field expressions in the template
 * (habitatName, animalId, biomeId).
 */
@Data
public class HabitatForm {
    private String habitatName;
    private Long animalId;
    private Long biomeId;
}
