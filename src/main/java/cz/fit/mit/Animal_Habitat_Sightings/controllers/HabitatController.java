package cz.fit.mit.Animal_Habitat_Sightings.controllers;

import cz.fit.mit.Animal_Habitat_Sightings.models.Habitat;
import cz.fit.mit.Animal_Habitat_Sightings.services.HabitatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/habitats")
@Slf4j
public class HabitatController {

  private final HabitatService habitatService;

  @GetMapping
  public String listHabitats(){
      return "habitats";
  }

  @GetMapping("/new")
  public String showAddForm(Model model){
    model.addAttribute("habitat", new Habitat());
    return "habitat-form";
  }

  @PostMapping
  public String addHabitat(@ModelAttribute("habitat") Habitat habitat){

      log.info("Habitat name: {}",habitat.getHabitatName());
      log.info("Animal ID: {}",habitat.getAnimalId());
      log.info("Biome Id: {}",habitat.getBiomeId());
      return "redirect:/habitats";
  }
}
