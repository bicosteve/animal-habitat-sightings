package cz.fit.mit.Animal_Habitat_Sightings.controller;

import cz.fit.mit.Animal_Habitat_Sightings.model.Checkpoint;
import cz.fit.mit.Animal_Habitat_Sightings.model.CheckpointForm;
import cz.fit.mit.Animal_Habitat_Sightings.service.CheckpointService;
import jakarta.validation.Valid;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        return "checkpoint-form";
    }

    @PostMapping
    public String addCheckpoint(@Valid @ModelAttribute("checkpoint") CheckpointForm form,
                                BindingResult result) throws Exception {

        JGeometry geometry = GeometryBinder.parse(
                form.getSdo_gtype(),
                form.getSdo_srid(),
                form.getSdo_elem_info(),
                form.getSdo_ordinates(),
                result);

        if (result.hasErrors()) {
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
}
