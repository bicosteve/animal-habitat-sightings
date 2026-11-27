package cz.fit.mit.Animal_Habitat_Sightings.controller;

import cz.fit.mit.Animal_Habitat_Sightings.model.Animal;
import cz.fit.mit.Animal_Habitat_Sightings.service.AnimalService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

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
        model.addAttribute("editing", false);
        return "animal-form";
    }

    @PostMapping
    public String addAnimal(@Valid @ModelAttribute("animal") Animal animal,
                            BindingResult result,
                            Model model) throws Exception {
        if (result.hasErrors()) {
            model.addAttribute("editing", false);
            return "animal-form";
        }

        try {
            animalService.addAnimal(animal);
        } catch (Exception ex) {
            if (isDuplicateName(ex)) {
                result.reject("animal.duplicate",
                        "An animal with that name already exists.");
                model.addAttribute("editing", false);
                return "animal-form";
            }
            throw ex;
        }

        return "redirect:/animals";
    }

    @GetMapping("/{animalId}/edit")
    public String showEditForm(@PathVariable long animalId,
                               Model model,
                               RedirectAttributes redirectAttributes) throws Exception {

        Animal animal = animalService.getAnimalById(animalId);
        if (animal == null) {
            redirectAttributes.addFlashAttribute("warning",
                    "That animal no longer exists.");
            return "redirect:/animals";
        }

        model.addAttribute("animal", animal);
        model.addAttribute("editing", true);
        return "animal-form";
    }

    @PostMapping("/{animalId}")
    public String updateAnimal(@PathVariable long animalId,
                               @Valid @ModelAttribute("animal") Animal animal,
                               BindingResult result,
                               Model model) throws Exception {

        // Keep the id authoritative from the path, not the form body.
        animal.setAnimalId(animalId);

        if (result.hasErrors()) {
            model.addAttribute("editing", true);
            return "animal-form";
        }

        try {
            animalService.updateAnimal(animal);
        } catch (Exception ex) {
            if (isDuplicateName(ex)) {
                result.reject("animal.duplicate",
                        "An animal with that name already exists.");
                model.addAttribute("editing", true);
                return "animal-form";
            }
            throw ex;
        }

        return "redirect:/animals";
    }

    /**
     * Returns the number of sightings and habitats that reference this animal,
     * so the delete confirmation modal can show the impact of a cascade delete.
     */
    @GetMapping("/{animalId}/delete-impact")
    @ResponseBody
    public Map<String, Integer> deleteImpact(@PathVariable long animalId) throws Exception {
        return animalService.getDeletionImpact(animalId);
    }

    @PostMapping("/{animalId}/delete")
    public String deleteAnimal(@PathVariable long animalId,
                               RedirectAttributes redirectAttributes) throws Exception {

        animalService.deleteAnimal(animalId);
        redirectAttributes.addFlashAttribute("success",
                "Animal deleted, along with its sightings and habitats.");
        return "redirect:/animals";
    }

    /**
     * Detects the Oracle unique-constraint violation on animal_name
     * (ORA-00001) anywhere in the exception chain.
     */
    private boolean isDuplicateName(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            String message = t.getMessage();
            if (message != null && message.contains("ORA-00001")) {
                return true;
            }
            if (t.getCause() == t) {
                break;
            }
        }
        return false;
    }
}
