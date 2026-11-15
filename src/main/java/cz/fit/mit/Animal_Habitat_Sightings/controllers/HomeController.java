package cz.fit.mit.Animal_Habitat_Sightings.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(){
        return "index";
    }

    @GetMapping("/habitats")
    public String habitats(){
        return "habitats";
    }

    @GetMapping("/sightings")
    public String sightings(){
        return "sightings";
    }

    @GetMapping("/analysis")
    public String analysis(){
        return "analysis";
    }
}
