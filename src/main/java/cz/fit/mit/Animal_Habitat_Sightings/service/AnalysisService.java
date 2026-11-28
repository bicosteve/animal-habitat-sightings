package cz.fit.mit.Animal_Habitat_Sightings.service;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.repository.AnalysisRepository;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;

    public AnalysisService(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    /** Immutable table + primary-key + name-column mapping for one entity type. */
    private record GeometrySource(String table, String pk, String nameColumn) {
    }


    private static final Map<String, GeometrySource> SOURCES = Map.of(
            "habitat",   new GeometrySource("habitats", "habitat_id", "habitat_name"),
            "biome",     new GeometrySource("biomes", "biome_id", "biome_name"),
            "checkpoint",new GeometrySource("checkpoints", "checkpoint_id", "checkpoint_name"),
            "trail",     new GeometrySource("wildlife_trails", "trail_id", "trail_name")
    );


    public List<Map<String, Object>> getAreaAndPerimeter() throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            List<Map<String, Object>> rows = analysisRepository.getAreaAndPerimeter(conn);

            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                Map<String, Object> out = new HashMap<>();
                out.put("type", row.get("type"));
                out.put("name", row.get("name"));
                out.put("area", row.get("area"));
                out.put("perimeter", row.get("perimeter"));
                out.put("geometry", convertGeometry((JGeometry) row.get("geometry")));
                out.put("centroid", convertGeometry((JGeometry) row.get("centroid")));
                result.add(out);
            }
            return result;
        }
    }


    public Double getDistance(String fromType, long fromId,
                              String toType, long toId) throws Exception {

        GeometrySource from = SOURCES.get(fromType);
        GeometrySource to = SOURCES.get(toType);
        if (from == null || to == null) {
            return null;
        }

        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return analysisRepository.getDistance(conn,
                    from.table(), from.pk(), fromId,
                    to.table(), to.pk(), toId);
        }
    }


    public Map<String, Object> getFeatureForMap(String type, long id) throws Exception {
        GeometrySource source = SOURCES.get(type);
        if (source == null) {
            return null;
        }

        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            Map<String, Object> feature = analysisRepository.getFeatureForMap(
                    conn, source.table(), source.pk(), source.nameColumn(), id);

            if (feature.isEmpty()) {
                return null;
            }

            Map<String, Object> out = new HashMap<>();
            out.put("type", type);
            out.put("id", id);
            out.put("name", feature.get("name"));
            out.put("geometry", convertGeometry((JGeometry) feature.get("geometry")));
            out.put("point", convertGeometry((JGeometry) feature.get("point")));
            return out;
        }
    }


    private Map<String, Object> convertGeometry(JGeometry geometry) {
        if (geometry == null) {
            return null;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("gtype", geometry.getDimensions() * 1000 + geometry.getType());
        data.put("srid", geometry.getSRID());
        data.put("elemInfo", geometry.getElemInfo());
        data.put("ordinates", geometry.getOrdinatesArray());
        return data;
    }
}
