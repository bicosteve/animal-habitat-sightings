package cz.fit.mit.Animal_Habitat_Sightings.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(){
        return "index";
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
