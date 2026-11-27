package cz.fit.mit.Animal_Habitat_Sightings.controller;

import cz.fit.mit.Animal_Habitat_Sightings.model.Habitat;
import cz.fit.mit.Animal_Habitat_Sightings.model.HabitatForm;
import cz.fit.mit.Animal_Habitat_Sightings.service.AnimalService;
import cz.fit.mit.Animal_Habitat_Sightings.service.HabitatService;
import jakarta.validation.Valid;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/habitats")
public class HabitatController {

    private final HabitatService habitatService;
    private final AnimalService animalService;

    public HabitatController(HabitatService habitatService, AnimalService animalService) {
        this.habitatService = habitatService;
        this.animalService = animalService;
    }

    @GetMapping
    public String listHabitats(Model model) throws Exception {
        model.addAttribute("habitats", habitatService.getAllHabitats());
        return "habitats";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) throws Exception {
        model.addAttribute("habitat", new HabitatForm());
        model.addAttribute("animals", animalService.getAllAnimals());
        return "habitat-form";
    }

    @PostMapping
    public String addHabitat(@Valid @ModelAttribute("habitat") HabitatForm form,
                             BindingResult result,
                             Model model) throws Exception {

        JGeometry geometry = GeometryBinder.parse(
                form.getSdo_gtype(),
                form.getSdo_srid(),
                form.getSdo_elem_info(),
                form.getSdo_ordinates(),
                result);

        if (result.hasErrors()) {
            model.addAttribute("animals", animalService.getAllAnimals());
            return "habitat-form";
        }

        Habitat habitat = new Habitat(
                0,
                form.getHabitatName(),
                form.getAnimalId(),
                geometry,
                null
        );

        habitatService.addHabitat(habitat);

        return "redirect:/habitats";
    }
}
