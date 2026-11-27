package cz.fit.mit.Animal_Habitat_Sightings.controller;

import cz.fit.mit.Animal_Habitat_Sightings.model.WildlifeTrail;
import cz.fit.mit.Animal_Habitat_Sightings.model.WildlifeTrailForm;
import cz.fit.mit.Animal_Habitat_Sightings.service.WildlifeTrailService;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;

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
        return "trail-form";
    }

    @PostMapping
    public String addTrail(@ModelAttribute WildlifeTrailForm form) throws Exception {
        int[] elemInfo = Arrays.stream(form.getSdo_elem_info().split(","))
                .mapToInt(Integer::parseInt)
                .toArray();

        double[] ordinates = Arrays.stream(form.getSdo_ordinates().split(","))
                .mapToDouble(Double::parseDouble)
                .toArray();

        JGeometry geometry = new JGeometry(
                form.getSdo_gtype(),
                form.getSdo_srid(),
                elemInfo,
                ordinates);

        WildlifeTrail trail = new WildlifeTrail(
                0,
                form.getTrailName(),
                form.getDescription(),
                geometry,
                null);

        trailService.addTrail(trail);

        return "redirect:/trails";
    }
}
