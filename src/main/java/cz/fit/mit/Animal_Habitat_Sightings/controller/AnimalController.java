package cz.fit.mit.Animal_Habitat_Sightings.controller;

import cz.fit.mit.Animal_Habitat_Sightings.model.Animal;
import cz.fit.mit.Animal_Habitat_Sightings.service.AnimalService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/animals")
public class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping
    public String listAnimals(Model model) throws Exception {
        model.addAttribute("animals", animalService.getAllAnimals());
        return "animals";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("animal", new Animal());
        return "animal-form";
    }

    @PostMapping
    public String addAnimal(@Valid @ModelAttribute("animal") Animal animal,
                            BindingResult result) throws Exception {
        if (result.hasErrors()) {
            return "animal-form";
        }
        animalService.addAnimal(animal);
        return "redirect:/animals";
    }
}
