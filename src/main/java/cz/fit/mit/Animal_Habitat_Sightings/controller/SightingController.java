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

        // Reject unsupported image formats (e.g. WebP, HEIC) up front. The
        // Oracle ORDImage codec only decodes a limited set of formats and
        // would otherwise fail with ORA-29400 / IMG-00705 at setProperties().
        if (image != null && !image.isEmpty() && !isSupportedImage(image)) {
            result.reject("image.unsupported",
                    "Only PNG or JPEG images are allowed.");
        }

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
     * Accepts only PNG and JPEG uploads, checking both the content type and the
     * file-name extension. Other formats (WebP, HEIC, GIF, ...) are rejected
     * because the Oracle ORDImage codec cannot decode them.
     */
    private boolean isSupportedImage(MultipartFile image) {
        String contentType = image.getContentType();
        boolean mimeOk = "image/png".equalsIgnoreCase(contentType)
                || "image/jpeg".equalsIgnoreCase(contentType);

        String filename = image.getOriginalFilename();
        String lower = filename != null ? filename.toLowerCase() : "";
        boolean extensionOk = lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg");

        return mimeOk && extensionOk;
    }

    /**
     * Shows the detail page for a single sighting: its image and details, with
     * controls to rotate the image (view-only) and delete the sighting.
     */
    @GetMapping("/{sightingId}")
    public String viewSighting(@PathVariable long sightingId,
                               Model model,
                               RedirectAttributes redirectAttributes) throws Exception {

        Map<String, Object> sighting = sightingService.getSightingDetails(sightingId);

        if (sighting == null) {
            redirectAttributes.addFlashAttribute("warning",
                    "That sighting no longer exists.");
            return "redirect:/sightings";
        }

        model.addAttribute("sighting", sighting);
        return "sighting-detail";
    }

    /**
     * Deletes the sighting and its image (if any) in a single transaction, then
     * returns to the list with a success flash.
     */
    @PostMapping("/{sightingId}/delete")
    public String deleteSighting(@PathVariable long sightingId,
                                 RedirectAttributes redirectAttributes) throws Exception {

        sightingService.deleteSighting(sightingId);
        redirectAttributes.addFlashAttribute("success",
                "Sighting deleted, along with its photo.");
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
