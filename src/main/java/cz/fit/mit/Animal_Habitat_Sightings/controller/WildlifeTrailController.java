package cz.fit.mit.Animal_Habitat_Sightings.controller;

import cz.fit.mit.Animal_Habitat_Sightings.model.WildlifeTrail;
import cz.fit.mit.Animal_Habitat_Sightings.model.forms.WildlifeTrailForm;
import cz.fit.mit.Animal_Habitat_Sightings.service.WildlifeTrailService;
import jakarta.validation.Valid;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/trails")
public class WildlifeTrailController {

    private final WildlifeTrailService trailService;

    public WildlifeTrailController(WildlifeTrailService trailService) {
        this.trailService = trailService;
    }

    @GetMapping
    public String listTrails(Model model) throws Exception {
        model.addAttribute("trails", trailService.getAllTrails());
        return "trails";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("trail", new WildlifeTrailForm());
        model.addAttribute("editing", false);
        return "trail-form";
    }

    @PostMapping
    public String addTrail(@Valid @ModelAttribute("trail") WildlifeTrailForm form,
                           BindingResult result,
                           Model model) throws Exception {

        JGeometry geometry = GeometryBinder.parse(
                form.getSdo_gtype(),
                form.getSdo_srid(),
                form.getSdo_elem_info(),
                form.getSdo_ordinates(),
                result);

        if (result.hasErrors()) {
            model.addAttribute("editing", false);
            return "trail-form";
        }

        WildlifeTrail trail = new WildlifeTrail(
                0,
                form.getTrailName(),
                form.getDescription(),
                geometry,
                null);

        trailService.addTrail(trail);

        return "redirect:/trails";
    }

    /**
     * Shows the read-only detail page for a single trail: its name,
     * description, length, and creation time, with controls to edit or delete.
     */
    @GetMapping("/{trailId}")
    public String viewTrail(@PathVariable long trailId,
                            Model model,
                            RedirectAttributes redirectAttributes) throws Exception {

        Map<String, Object> trail = trailService.getTrailDetails(trailId);

        if (trail == null) {
            redirectAttributes.addFlashAttribute("warning",
                    "That trail no longer exists.");
            return "redirect:/trails";
        }

        model.addAttribute("trail", trail);
        return "trail-detail";
    }

    /**
     * Shows the edit form for a trail, pre-filled with its current values. The
     * stored line is passed as a JSON array of [lat, lng] vertices so the map
     * can pre-draw it; the user can reshape or redraw it like on the create
     * form.
     */
    @GetMapping("/{trailId}/edit")
    public String showEditForm(@PathVariable long trailId,
                               Model model,
                               RedirectAttributes redirectAttributes) throws Exception {

        Map<String, Object> details = trailService.getTrailDetails(trailId);

        if (details == null) {
            redirectAttributes.addFlashAttribute("warning",
                    "That trail no longer exists.");
            return "redirect:/trails";
        }

        WildlifeTrailForm form = new WildlifeTrailForm();
        form.setTrailName((String) details.get("trailName"));
        form.setDescription((String) details.get("description"));

        model.addAttribute("trail", form);
        model.addAttribute("editing", true);
        model.addAttribute("trailId", trailId);
        // Seed the map line from the stored geometry: a JSON [[lat,lng],...]
        // string the map JS parses, or null if the geometry is unreadable.
        model.addAttribute("initialLine", trailLineAsJson(trailId));
        return "trail-form";
    }

    @PostMapping("/{trailId}")
    public String updateTrail(@PathVariable long trailId,
                              @Valid @ModelAttribute("trail") WildlifeTrailForm form,
                              BindingResult result,
                              Model model) throws Exception {

        JGeometry geometry = GeometryBinder.parse(
                form.getSdo_gtype(),
                form.getSdo_srid(),
                form.getSdo_elem_info(),
                form.getSdo_ordinates(),
                result);

        if (result.hasErrors()) {
            model.addAttribute("editing", true);
            model.addAttribute("trailId", trailId);
            model.addAttribute("initialLine", trailLineAsJson(trailId));
            return "trail-form";
        }

        WildlifeTrail trail = new WildlifeTrail(
                trailId,
                form.getTrailName(),
                form.getDescription(),
                geometry,
                null);

        trailService.updateTrail(trail);

        return "redirect:/trails";
    }

    @PostMapping("/{trailId}/delete")
    public String deleteTrail(@PathVariable long trailId,
                              RedirectAttributes redirectAttributes) throws Exception {

        trailService.deleteTrail(trailId);
        redirectAttributes.addFlashAttribute("success", "Trail deleted.");
        return "redirect:/trails";
    }

    /**
     * Converts a trail's stored line geometry into a JSON array of [lat, lng]
     * vertices for seeding the edit map, or {@code null} if it cannot be read.
     * The stored ordinates are (lng, lat) pairs, so they are swapped to the
     * [lat, lng] order Leaflet expects.
     */
    private String trailLineAsJson(long trailId) throws Exception {
        JGeometry geometry = trailService.getTrailGeometry(trailId);
        if (geometry == null) {
            return null;
        }

        double[] ordinates = geometry.getOrdinatesArray();
        if (ordinates == null || ordinates.length < 4) {
            return null;
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i + 1 < ordinates.length; i += 2) {
            double lng = ordinates[i];
            double lat = ordinates[i + 1];
            if (i > 0) {
                json.append(",");
            }
            json.append("[").append(lat).append(",").append(lng).append("]");
        }
        json.append("]");

        return json.toString();
    }
}
