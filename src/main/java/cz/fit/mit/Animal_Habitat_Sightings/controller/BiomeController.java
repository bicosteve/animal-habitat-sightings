package cz.fit.mit.Animal_Habitat_Sightings.controller;

import cz.fit.mit.Animal_Habitat_Sightings.model.Biome;
import cz.fit.mit.Animal_Habitat_Sightings.model.BiomeForm;
import cz.fit.mit.Animal_Habitat_Sightings.service.BiomeService;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;

@Controller
@RequestMapping("/biomes")
public class BiomeController {

    private final BiomeService biomeService;

    public BiomeController(BiomeService biomeService) {
        this.biomeService = biomeService;
    }

    @GetMapping
    public String listBiomes(Model model) throws Exception {
        model.addAttribute("biomes", biomeService.getAllBiomes());
        return "biomes";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("biome", new BiomeForm());
        return "biome-form";
    }

    @PostMapping
    public String addBiome(@ModelAttribute BiomeForm form) throws Exception {
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

        Biome biome = new Biome(
                0,
                form.getBiomeName(),
                form.getDescription(),
                geometry);

        biomeService.addBiome(biome);

        return "redirect:/biomes";
    }
}
