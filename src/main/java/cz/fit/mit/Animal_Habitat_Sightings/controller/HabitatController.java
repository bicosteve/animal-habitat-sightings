package cz.fit.mit.Animal_Habitat_Sightings.controller;

import cz.fit.mit.Animal_Habitat_Sightings.model.HabitatForm;
import cz.fit.mit.Animal_Habitat_Sightings.service.HabitatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/habitats")
public class HabitatController {

    private final HabitatService habitatService;

    public HabitatController(HabitatService habitatService) {
        this.habitatService = habitatService;
    }

    @GetMapping
    public String listHabitats(Model model) throws Exception {
        model.addAttribute("habitats", habitatService.getAllHabitats());
        return "habitats";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("habitat", new HabitatForm());
        return "habitat-form";
    }
}
