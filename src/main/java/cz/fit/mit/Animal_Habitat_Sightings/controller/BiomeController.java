package cz.fit.mit.Animal_Habitat_Sightings.controller;

import cz.fit.mit.Animal_Habitat_Sightings.model.Biome;
import cz.fit.mit.Animal_Habitat_Sightings.model.BiomeForm;
import cz.fit.mit.Animal_Habitat_Sightings.service.BiomeService;
import jakarta.validation.Valid;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String addBiome(@Valid @ModelAttribute("biome") BiomeForm form,
                           BindingResult result) throws Exception {

        JGeometry geometry = GeometryBinder.parse(
                form.getSdo_gtype(),
                form.getSdo_srid(),
                form.getSdo_elem_info(),
                form.getSdo_ordinates(),
                result);

        if (result.hasErrors()) {
            return "biome-form";
        }

        Biome biome = new Biome(
                0,
                form.getBiomeName(),
                form.getDescription(),
                geometry);

        biomeService.addBiome(biome);

        return "redirect:/biomes";
    }
}
