package cz.fit.mit.Animal_Habitat_Sightings.controller;

import org.springframework.ui.Model;
import cz.fit.mit.Animal_Habitat_Sightings.service.HabitatService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private HabitatService habitatService;

    public HomeController(HabitatService habitatService) {
        this.habitatService = habitatService;
    }

    @GetMapping("/")
    public String dashboard(Model model) throws Exception {
        model.addAttribute("habitats", habitatService.getAllHabitatsForMap());
        return "index";
    }

    @GetMapping("/analysis")
    public String analysis(){
        return "analysis";
    }
}
