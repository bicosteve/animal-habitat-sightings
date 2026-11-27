package cz.fit.mit.Animal_Habitat_Sightings.model.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Command object for the sighting create form.
 *
 * <p>The geometry is a single point drawn on the map; its SDO components arrive
 * as comma-separated strings (see {@code HabitatForm} for the same pattern) and
 * are parsed in the controller via {@code GeometryBinder}.
 *
 * <p>The uploaded image is <em>not</em> part of this form; it is received as a
 * separate {@code MultipartFile} request parameter so the optional file and the
 * validated fields stay decoupled.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SightingForm {

    @NotNull(message = "Please select an animal.")
    private Long animalId;

    @NotNull(message = "The sighting date and time are required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime sightedAt;

    @Size(max = 1000, message = "Notes must be at most 1000 characters.")
    private String notes;

    private int sdo_gtype;

    private int sdo_srid;

    @NotBlank(message = "Mark the sighting location on the map before saving.")
    private String sdo_elem_info;

    @NotBlank(message = "Mark the sighting location on the map before saving.")
    private String sdo_ordinates;
}
