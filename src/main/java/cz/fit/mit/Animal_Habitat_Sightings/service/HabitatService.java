package cz.fit.mit.Animal_Habitat_Sightings.service;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.model.Habitat;
import cz.fit.mit.Animal_Habitat_Sightings.repository.HabitatRepository;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HabitatService {

    private final HabitatRepository habitatRepository;

    public HabitatService(HabitatRepository habitatRepository) {
        this.habitatRepository = habitatRepository;
    }

    public void addHabitat(Habitat habitat) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            habitatRepository.insertHabitat(conn, habitat);
        }
    }

    public List<Habitat> getAllHabitats() throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return habitatRepository.getAllHabitats(conn);
        }
    }

    public Map<String, Object> getHabitatDetails(long habitatId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return habitatRepository.getHabitatDetails(conn, habitatId);
        }
    }

    public List<Map<String, Object>> getAllHabitatsForMap() throws Exception {
        List<Habitat> habitats = getAllHabitats();

        return habitats.stream().map(habitat -> {
            Map<String, Object> feature = new HashMap<>();

            feature.put("id", habitat.getHabitatId());
            feature.put("name", habitat.getHabitatName());
            feature.put("geometry", convertGeometry(habitat.getGeom()));

            return feature;
        }).toList();
    }

    public List<Map<String, Object>> getIntersectingBiomes(long habitatId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return habitatRepository.getIntersectingBiomes(conn, habitatId);
        }
    }

    public List<Map<String, Object>> getSightingsInHabitat(long habitatId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return habitatRepository.getSightingsInHabitat(conn, habitatId);
        }
    }

    public void deleteHabitat(long habitatId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            habitatRepository.deleteHabitat(conn, habitatId);
        }
    }

    private Map<String, Object> convertGeometry(JGeometry geometry) {
        Map<String, Object> data = new HashMap<>();

        if (geometry == null) {
            return data;
        }

        data.put("gtype", geometry.getDimensions() * 1000
                + geometry.getType());
        data.put("srid", geometry.getSRID());
        data.put("elemInfo", geometry.getElemInfo());
        data.put("ordinates", geometry.getOrdinatesArray());

        return data;
    }
}
