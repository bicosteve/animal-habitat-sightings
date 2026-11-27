package cz.fit.mit.Animal_Habitat_Sightings.controller;

import cz.fit.mit.Animal_Habitat_Sightings.model.Habitat;
import cz.fit.mit.Animal_Habitat_Sightings.model.HabitatForm;
import cz.fit.mit.Animal_Habitat_Sightings.service.AnimalService;
import cz.fit.mit.Animal_Habitat_Sightings.service.HabitatService;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

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
    public String addHabitat(@ModelAttribute HabitatForm form) throws Exception {
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
