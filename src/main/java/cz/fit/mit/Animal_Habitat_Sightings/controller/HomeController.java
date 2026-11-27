package cz.fit.mit.Animal_Habitat_Sightings.controller;

import org.springframework.ui.Model;
import cz.fit.mit.Animal_Habitat_Sightings.service.AnimalService;
import cz.fit.mit.Animal_Habitat_Sightings.service.HabitatService;
import cz.fit.mit.Animal_Habitat_Sightings.service.SightingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final AnimalService animalService;
    private final SightingService sightingService;
    private final HabitatService habitatService;

    public HomeController(AnimalService animalService,
                          SightingService sightingService,
                          HabitatService habitatService) {
        this.animalService = animalService;
        this.sightingService = sightingService;
        this.habitatService = habitatService;
    }

    @GetMapping("/")
    public String dashboard(Model model) throws Exception {
        model.addAttribute("animals", animalService.getAllAnimals());
        model.addAttribute("sightings", sightingService.getAllSightingsForList());
        model.addAttribute("habitats", habitatService.getAllHabitats());
        return "index";
    }

    @GetMapping("/analysis")
    public String analysis(){
        return "analysis";
    }
}
