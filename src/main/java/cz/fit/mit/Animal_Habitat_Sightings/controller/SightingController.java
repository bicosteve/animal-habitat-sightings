package cz.fit.mit.Animal_Habitat_Sightings.controller;

import cz.fit.mit.Animal_Habitat_Sightings.model.Sighting;
import cz.fit.mit.Animal_Habitat_Sightings.model.forms.SightingForm;
import cz.fit.mit.Animal_Habitat_Sightings.service.AnimalService;
import cz.fit.mit.Animal_Habitat_Sightings.service.SightingService;
import jakarta.validation.Valid;
import oracle.spatial.geometry.JGeometry;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/sightings")
public class SightingController {

    private final SightingService sightingService;
    private final AnimalService animalService;

    public SightingController(SightingService sightingService, AnimalService animalService) {
        this.sightingService = sightingService;
        this.animalService = animalService;
    }

    @GetMapping
    public String listSightings(Model model) throws Exception {
        model.addAttribute("sightings", sightingService.getAllSightingsForList());
        return "sightings";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) throws Exception {
        SightingForm form = new SightingForm();
        form.setSightedAt(LocalDateTime.now());

        model.addAttribute("sighting", form);
        model.addAttribute("animals", animalService.getAllAnimals());
        return "sighting-form";
    }

    @PostMapping
    public String addSighting(@Valid @ModelAttribute("sighting") SightingForm form,
                              BindingResult result,
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              Model model,
                              RedirectAttributes redirectAttributes) throws Exception {

        JGeometry geometry = GeometryBinder.parse(
                form.getSdo_gtype(),
                form.getSdo_srid(),
                form.getSdo_elem_info(),
                form.getSdo_ordinates(),
                result);

        if (result.hasErrors()) {
            model.addAttribute("animals", animalService.getAllAnimals());
            return "sighting-form";
        }

        Sighting sighting = new Sighting(
                0,
                form.getAnimalId(),
                form.getSightedAt(),
                form.getNotes(),
                geometry);

        byte[] imageBytes = (image != null && !image.isEmpty())
                ? image.getBytes()
                : null;

        boolean imageStored = sightingService.addSighting(sighting, imageBytes);

        if (!imageStored) {
            redirectAttributes.addFlashAttribute("warning",
                    "Sighting saved, but the image could not be attached.");
        }

        return "redirect:/sightings";
    }

    /**
     * Streams the stored image for a sighting. Returns 404 when the sighting has
     * no image.
     */
    @GetMapping("/{sightingId}/image")
    @ResponseBody
    public ResponseEntity<byte[]> getSightingImage(@PathVariable long sightingId)
            throws Exception {

        Map<String, Object> image = sightingService.getSightingImage(sightingId);

        if (image == null || image.get("imageData") == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] data = (byte[]) image.get("imageData");
        String mimeType = (String) image.get("mimeType");

        MediaType mediaType = mimeType != null
                ? MediaType.parseMediaType(mimeType)
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(data);
    }
}
