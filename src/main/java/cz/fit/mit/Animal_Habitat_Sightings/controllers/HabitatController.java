package cz.fit.mit.Animal_Habitat_Sightings.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/habitats")
public class HabitatController {


    @GetMapping
    public String listHabitats(){
        return "all habitats";
    }

    @GetMapping("/new")
    public String showAddForm(){
        return "habitat-form";
    }
}
