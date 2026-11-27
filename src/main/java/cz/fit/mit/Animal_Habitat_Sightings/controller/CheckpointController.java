package cz.fit.mit.Animal_Habitat_Sightings.controller;

import cz.fit.mit.Animal_Habitat_Sightings.model.Checkpoint;
import cz.fit.mit.Animal_Habitat_Sightings.model.forms.CheckpointForm;
import cz.fit.mit.Animal_Habitat_Sightings.service.CheckpointService;
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
@RequestMapping("/checkpoints")
public class CheckpointController {

    private final CheckpointService checkpointService;

    public CheckpointController(CheckpointService checkpointService) {
        this.checkpointService = checkpointService;
    }

    @GetMapping
    public String listCheckpoints(Model model) throws Exception {
        model.addAttribute("checkpoints", checkpointService.getAllCheckpoints());
        return "checkpoints";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("checkpoint", new CheckpointForm());
        model.addAttribute("editing", false);
        return "checkpoint-form";
    }

    @PostMapping
    public String addCheckpoint(@Valid @ModelAttribute("checkpoint") CheckpointForm form,
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
            return "checkpoint-form";
        }

        Checkpoint checkpoint = new Checkpoint(
                0,
                form.getCheckpointName(),
                form.getCheckpointType(),
                form.getDescription(),
                geometry,
                null);

        checkpointService.addCheckpoint(checkpoint);

        return "redirect:/checkpoints";
    }

    /**
     * Shows the read-only detail page for a single checkpoint: its name, type,
     * description, location, and creation time, with controls to edit or delete.
     */
    @GetMapping("/{checkpointId}")
    public String viewCheckpoint(@PathVariable long checkpointId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) throws Exception {

        Map<String, Object> checkpoint = checkpointService.getCheckpointDetails(checkpointId);

        if (checkpoint == null) {
            redirectAttributes.addFlashAttribute("warning",
                    "That checkpoint no longer exists.");
            return "redirect:/checkpoints";
        }

        model.addAttribute("checkpoint", checkpoint);
        return "checkpoint-detail";
    }

    /**
     * Shows the edit form for a checkpoint, pre-filled with its current values.
     * The stored point is passed as lat/lng so the map can pre-place it; the
     * user can move or redraw it like on the create form.
     */
    @GetMapping("/{checkpointId}/edit")
    public String showEditForm(@PathVariable long checkpointId,
                               Model model,
                               RedirectAttributes redirectAttributes) throws Exception {

        Map<String, Object> details = checkpointService.getCheckpointDetails(checkpointId);

        if (details == null) {
            redirectAttributes.addFlashAttribute("warning",
                    "That checkpoint no longer exists.");
            return "redirect:/checkpoints";
        }

        CheckpointForm form = new CheckpointForm();
        form.setCheckpointName((String) details.get("checkpointName"));
        form.setCheckpointType((String) details.get("checkpointType"));
        form.setDescription((String) details.get("description"));

        model.addAttribute("checkpoint", form);
        model.addAttribute("editing", true);
        model.addAttribute("checkpointId", checkpointId);
        // Seed the map marker from the stored point (may be null if absent).
        model.addAttribute("initialLat", details.get("latitude"));
        model.addAttribute("initialLng", details.get("longitude"));
        return "checkpoint-form";
    }

    @PostMapping("/{checkpointId}")
    public String updateCheckpoint(@PathVariable long checkpointId,
                                   @Valid @ModelAttribute("checkpoint") CheckpointForm form,
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
            model.addAttribute("checkpointId", checkpointId);
            return "checkpoint-form";
        }

        Checkpoint checkpoint = new Checkpoint(
                checkpointId,
                form.getCheckpointName(),
                form.getCheckpointType(),
                form.getDescription(),
                geometry,
                null);

        checkpointService.updateCheckpoint(checkpoint);

        return "redirect:/checkpoints";
    }

    @PostMapping("/{checkpointId}/delete")
    public String deleteCheckpoint(@PathVariable long checkpointId,
                                   RedirectAttributes redirectAttributes) throws Exception {

        checkpointService.deleteCheckpoint(checkpointId);
        redirectAttributes.addFlashAttribute("success", "Checkpoint deleted.");
        return "redirect:/checkpoints";
    }
}
