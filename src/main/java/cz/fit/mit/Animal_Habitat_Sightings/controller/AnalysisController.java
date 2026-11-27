package cz.fit.mit.Animal_Habitat_Sightings.controller;

import cz.fit.mit.Animal_Habitat_Sightings.service.AnalysisService;
import cz.fit.mit.Animal_Habitat_Sightings.service.BiomeService;
import cz.fit.mit.Animal_Habitat_Sightings.service.CheckpointService;
import cz.fit.mit.Animal_Habitat_Sightings.service.HabitatService;
import cz.fit.mit.Animal_Habitat_Sightings.service.WildlifeTrailService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final HabitatService habitatService;
    private final BiomeService biomeService;
    private final CheckpointService checkpointService;
    private final WildlifeTrailService trailService;

    public AnalysisController(AnalysisService analysisService,
                             HabitatService habitatService,
                             BiomeService biomeService,
                             CheckpointService checkpointService,
                             WildlifeTrailService trailService) {
        this.analysisService = analysisService;
        this.habitatService = habitatService;
        this.biomeService = biomeService;
        this.checkpointService = checkpointService;
        this.trailService = trailService;
    }

    @GetMapping
    public String analysis(Model model) throws Exception {
        populateModel(model);
        return "analysis";
    }

    @PostMapping("/distance")
    public String calculateDistance(@RequestParam String fromType,
                                    @RequestParam long fromId,
                                    @RequestParam String toType,
                                    @RequestParam long toId,
                                    Model model) throws Exception {

        Double distanceMetres = analysisService.getDistance(fromType, fromId, toType, toId);

        // Map-ready geometry + name for both features (used for the map and
        // for the result message).
        Map<String, Object> fromFeature = analysisService.getFeatureForMap(fromType, fromId);
        Map<String, Object> toFeature = analysisService.getFeatureForMap(toType, toId);

        if (distanceMetres == null || fromFeature == null || toFeature == null) {
            model.addAttribute("distanceError",
                    "Could not compute the distance for that selection.");
        } else {
            double distanceKm = distanceMetres / 1000.0;

            model.addAttribute("distanceKm", distanceKm);
            model.addAttribute("fromName", fromFeature.get("name"));
            model.addAttribute("toName", toFeature.get("name"));

            // Echo the selection back so the dropdowns keep what the user chose.
            model.addAttribute("fromType", fromType);
            model.addAttribute("fromId", fromId);
            model.addAttribute("toType", toType);
            model.addAttribute("toId", toId);

            // For the map: both features' shapes + representative points.
            model.addAttribute("fromFeature", fromFeature);
            model.addAttribute("toFeature", toFeature);
        }

        populateModel(model);
        return "analysis";
    }

    /** Adds the data every render of the page needs. */
    private void populateModel(Model model) throws Exception {
        model.addAttribute("areaRows", analysisService.getAreaAndPerimeter());
        model.addAttribute("habitats", habitatService.getAllHabitats());
        model.addAttribute("biomes", biomeService.getAllBiomes());
        model.addAttribute("checkpoints", checkpointService.getAllCheckpoints());
        model.addAttribute("trails", trailService.getAllTrails());
    }
}
