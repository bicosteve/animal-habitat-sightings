package cz.fit.mit.Animal_Habitat_Sightings.service;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.model.analysis.RelatedItem;
import cz.fit.mit.Animal_Habitat_Sightings.model.analysis.RelationshipGroup;
import cz.fit.mit.Animal_Habitat_Sightings.model.analysis.RelationshipResult;
import cz.fit.mit.Animal_Habitat_Sightings.repository.AnalysisRepository;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AnalysisService {

    /** Format used for the sighting timestamps in relationship details. */
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AnalysisRepository analysisRepository;
    private final BiomeService biomeService;
    private final CheckpointService checkpointService;
    private final HabitatService habitatService;
    private final WildlifeTrailService trailService;

    public AnalysisService(AnalysisRepository analysisRepository,
                           BiomeService biomeService,
                           CheckpointService checkpointService,
                           HabitatService habitatService,
                           WildlifeTrailService trailService) {
        this.analysisRepository = analysisRepository;
        this.biomeService = biomeService;
        this.checkpointService = checkpointService;
        this.habitatService = habitatService;
        this.trailService = trailService;
    }

    /** Immutable table + primary-key + name-column mapping for one entity type. */
    private record GeometrySource(String table, String pk, String nameColumn) {
    }

    /**
     * Whitelist of the entity types that carry a geometry. The table, primary
     * key, and name-column names come only from this map, never from request
     * data, so they are safe to splice into SQL.
     */
    private static final Map<String, GeometrySource> SOURCES = Map.of(
            "habitat",   new GeometrySource("habitats", "habitat_id", "habitat_name"),
            "biome",     new GeometrySource("biomes", "biome_id", "biome_name"),
            "checkpoint",new GeometrySource("checkpoints", "checkpoint_id", "checkpoint_name"),
            "trail",     new GeometrySource("wildlife_trails", "trail_id", "trail_name"),
            // Sightings have no name column; their notes are used as a label
            // and the service falls back to "Sighting #<id>" when blank.
            "sighting",  new GeometrySource("sightings", "sighting_id", "notes")
    );

    /**
     * Area/perimeter rows with map-ready geometry: each row's {@code geometry}
     * and {@code centroid} JGeometry values are converted to plain maps the
     * JavaScript map can read (gtype/srid/elemInfo/ordinates).
     */
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

    /**
     * Shortest distance (metres) between two selected entities, each given as
     * a type ("habitat"/"biome"/"checkpoint"/"trail") and id. Returns
     * {@code null} if a type is unknown or a geometry is missing.
     */
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

    /**
     * A selected feature's map-ready geometry and representative point, or
     * {@code null} if the type is unknown or the feature does not exist. The
     * returned map carries {@code geometry} and {@code point} as plain maps.
     */
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

    /**
     * Runs the spatial relationship queries for one subject feature and
     * returns the groups of related features plus their map geometry.
     *
     * <p>Each group is produced by one of the project's existing spatial
     * queries; nothing here issues new SQL. The subject may be a habitat
     * (biomes intersecting it, sightings inside it, checkpoints inside it,
     * trails crossing it), a biome (habitats in it, distances to those
     * habitats), or a trail (sightings near it).
     *
     * @param subjectType   "habitat", "biome", or "trail"
     * @param subjectId     id of the analysed feature
     * @param trailDistance radius in metres for the "sightings near trail" query
     */
    public RelationshipResult getRelationships(String subjectType, long subjectId,
                                               double trailDistance) throws Exception {

        List<RelationshipGroup> groups = new ArrayList<>();
        // Related feature ids per type, collected while building the groups
        // so their geometry can be fetched with a single connection later.
        Map<String, List<Long>> relatedIds = new LinkedHashMap<>();
        String subjectName;

        switch (subjectType == null ? "" : subjectType) {
            case "habitat" -> {
                subjectName = habitatRelationships(subjectId, groups, relatedIds);
            }
            case "biome" -> {
                subjectName = biomeRelationships(subjectId, groups, relatedIds);
            }
            case "trail" -> {
                subjectName = trailRelationships(subjectId, trailDistance, groups, relatedIds);
            }
            default -> {
                throw new IllegalArgumentException("Unknown subject type: " + subjectType);
            }
        }

        List<Map<String, Object>> mapFeatures =
                fetchFeatures(subjectType, subjectId, relatedIds);

        return new RelationshipResult(subjectType,
                typeLabel(subjectType),
                subjectName,
                groups,
                mapFeatures);
    }

    // --- relationship groups ------------------------------------------------

    /**
     * Relationships of a habitat: biomes it overlaps, sightings inside it,
     * checkpoints inside it, and trails crossing it.
     */
    private String habitatRelationships(long habitatId,
                                       List<RelationshipGroup> groups,
                                       Map<String, List<Long>> relatedIds) throws Exception {

        Map<String, Object> details = habitatService.getHabitatDetails(habitatId);
        if (details == null) {
            throw new IllegalArgumentException("Habitat not found: " + habitatId);
        }
        String subjectName = text(details, "habitatName");

        List<Map<String, Object>> biomes = habitatService.getIntersectingBiomes(habitatId);
        groups.add(new RelationshipGroup(
                "Biomes intersecting this habitat",
                "Biomes whose polygon overlaps the habitat "
                        + "(SDO_RELATE with mask=ANYINTERACT).",
                biomes.stream()
                        .map(row -> new RelatedItem(longValue(row, "biomeId"),
                                text(row, "biomeName"),
                                biomeSummary(longValue(row, "biomeId"))))
                        .toList()));
        collect(relatedIds, "biome", biomes, "biomeId");

        List<Map<String, Object>> sightings = habitatService.getSightingsInHabitat(habitatId);
        groups.add(new RelationshipGroup(
                "Sightings inside this habitat",
                "Sighting points that fall within the habitat polygon "
                        + "(SDO_RELATE with mask=INSIDE).",
                sightings.stream()
                        .map(row -> new RelatedItem(longValue(row, "sightingId"),
                                "Sighting #" + longValue(row, "sightingId"),
                                sightingDetail(row)))
                        .toList()));
        collect(relatedIds, "sighting", sightings, "sightingId");

        List<Map<String, Object>> checkpoints = checkpointService.getCheckpointsInHabitat(habitatId);
        groups.add(new RelationshipGroup(
                "Checkpoints inside this habitat",
                "Checkpoint points located within the habitat polygon "
                        + "(SDO_RELATE with mask=INSIDE).",
                checkpoints.stream()
                        .map(row -> new RelatedItem(longValue(row, "checkpointId"),
                                text(row, "checkpointName"),
                                join(text(row, "checkpointType"), text(row, "description"))))
                        .toList()));
        collect(relatedIds, "checkpoint", checkpoints, "checkpointId");

        List<Map<String, Object>> trails = trailService.getTrailsIntersectingHabitat(habitatId);
        groups.add(new RelationshipGroup(
                "Trails crossing this habitat",
                "Trail lines that intersect the habitat polygon "
                        + "(SDO_RELATE with mask=ANYINTERACT).",
                trails.stream()
                        .map(row -> new RelatedItem(longValue(row, "trailId"),
                                text(row, "trailName"),
                                text(row, "description")))
                        .toList()));
        collect(relatedIds, "trail", trails, "trailId");

        return subjectName;
    }

    /**
     * Relationships of a biome: the habitats it overlaps, the distance to
     * each of those habitats, and the biome's own area and perimeter.
     */
    private String biomeRelationships(long biomeId,
                                      List<RelationshipGroup> groups,
                                      Map<String, List<Long>> relatedIds) throws Exception {

        Map<String, Object> details = biomeService.getBiomeDetails(biomeId);
        if (details == null) {
            throw new IllegalArgumentException("Biome not found: " + biomeId);
        }
        String subjectName = text(details, "biomeName");

        groups.add(new RelationshipGroup(
                "About this biome",
                "Area and perimeter of the biome polygon, computed with "
                        + "SDO_AREA and SDO_LENGTH.",
                List.of(new RelatedItem(biomeId, subjectName,
                        "Area " + formatNumber(detailNumber(details, "area")) + " m²"
                                + " · Perimeter " + formatNumber(detailNumber(details, "perimeter")) + " m"))));

        List<Map<String, Object>> habitats = biomeService.getHabitatsInBiome(biomeId);
        groups.add(new RelationshipGroup(
                "Habitats in this biome",
                "Habitats whose polygon overlaps the biome "
                        + "(SDO_RELATE with mask=ANYINTERACT).",
                habitats.stream()
                        .map(row -> new RelatedItem(longValue(row, "habitatId"),
                                text(row, "habitatName"),
                                join(text(row, "animalName"), null)))
                        .toList()));
        collect(relatedIds, "habitat", habitats, "habitatId");

        List<Map<String, Object>> distances = biomeService.getDistancesToHabitats(biomeId);
        groups.add(new RelationshipGroup(
                "Distance to each habitat",
                "Shortest distance between this biome and every habitat "
                        + "(SDO_DISTANCE).",
                distances.stream()
                        .map(row -> new RelatedItem(longValue(row, "habitatId"),
                                text(row, "habitatName"),
                                formatNumber(detailNumber(row, "distance")) + " m"))
                        .toList()));

        return subjectName;
    }

    /**
     * Relationships of a trail: the sightings recorded close to its line.
     */
    private String trailRelationships(long trailId, double trailDistance,
                                      List<RelationshipGroup> groups,
                                      Map<String, List<Long>> relatedIds) throws Exception {

        Map<String, Object> details = trailService.getTrailDetails(trailId);
        if (details == null) {
            throw new IllegalArgumentException("Trail not found: " + trailId);
        }
        String subjectName = text(details, "trailName");

        groups.add(new RelationshipGroup(
                "About this trail",
                "Length of the trail line, computed with SDO_LENGTH.",
                List.of(new RelatedItem(trailId, subjectName,
                        "Length " + formatNumber(detailNumber(details, "length")) + " m"))));

        List<Map<String, Object>> sightings =
                trailService.getSightingsNearTrail(trailId, trailDistance);
        groups.add(new RelationshipGroup(
                "Sightings near this trail",
                "Sighting points within " + formatNumber(trailDistance)
                        + " m of the trail line (SDO_WITHIN_DISTANCE).",
                sightings.stream()
                        .map(row -> new RelatedItem(longValue(row, "sightingId"),
                                "Sighting #" + longValue(row, "sightingId"),
                                sightingDetail(row)))
                        .toList()));
        collect(relatedIds, "sighting", sightings, "sightingId");

        return subjectName;
    }

    // --- helpers -------------------------------------------------------------

    /**
     * Collects the ids of the related features of one type so their geometry
     * can be fetched afterwards.
     */
    private void collect(Map<String, List<Long>> relatedIds, String type,
                         List<Map<String, Object>> rows, String idKey) {
        List<Long> ids = relatedIds.computeIfAbsent(type, key -> new ArrayList<>());
        for (Map<String, Object> row : rows) {
            long id = longValue(row, idKey);
            if (id != 0L && !ids.contains(id)) {
                ids.add(id);
            }
        }
    }

    /**
     * Loads the map-ready geometry of the subject and of every related
     * feature, using a single database connection for all of them.
     */
    private List<Map<String, Object>> fetchFeatures(String subjectType, long subjectId,
                                                     Map<String, List<Long>> relatedIds)
            throws Exception {

        List<Map<String, Object>> features = new ArrayList<>();

        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {

            if (SOURCES.containsKey(subjectType)) {
                Map<String, Object> subject = analysisRepository.getFeatureForMap(
                        conn, SOURCES.get(subjectType).table(),
                        SOURCES.get(subjectType).pk(),
                        SOURCES.get(subjectType).nameColumn(),
                        subjectId);

                if (!subject.isEmpty()) {
                    features.add(toMapFeature(subjectType, subjectId,
                            text(subject, "name"), subject, "subject"));
                }
            }

            for (Map.Entry<String, List<Long>> entry : relatedIds.entrySet()) {
                GeometrySource source = SOURCES.get(entry.getKey());
                if (source == null) {
                    continue;
                }
                for (Long id : entry.getValue()) {
                    Map<String, Object> feature = analysisRepository.getFeatureForMap(
                            conn, source.table(), source.pk(), source.nameColumn(), id);

                    if (!feature.isEmpty()) {
                        features.add(toMapFeature(entry.getKey(), id,
                                text(feature, "name"), feature, entry.getKey()));
                    }
                }
            }
        }

        return features;
    }

    /** Builds the map-ready feature map consumed by the JavaScript map. */
    private Map<String, Object> toMapFeature(String type, long id, String name,
                                             Map<String, Object> raw, String role) {
        Map<String, Object> feature = new HashMap<>();
        feature.put("type", type);
        feature.put("id", id);
        feature.put("name", labelFor(type, id, name));
        feature.put("role", role);
        feature.put("geometry", convertGeometry((JGeometry) raw.get("geometry")));
        feature.put("point", convertGeometry((JGeometry) raw.get("point")));
        return feature;
    }

    /**
     * A readable label for a feature. Sightings (and any feature without a
     * name of its own) fall back to "&lt;Type&gt; #&lt;id&gt;".
     */
    private String labelFor(String type, long id, String name) {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return switch (type == null ? "" : type) {
            case "sighting" -> "Sighting #" + id;
            case "habitat" -> "Habitat #" + id;
            case "biome" -> "Biome #" + id;
            case "checkpoint" -> "Checkpoint #" + id;
            case "trail" -> "Trail #" + id;
            default -> type + " #" + id;
        };
    }

    /**
     * Converts an Oracle {@link JGeometry} to the plain gtype/srid/elemInfo/
     * ordinates map the map JavaScript consumes (mirrors HabitatService), or
     * {@code null} if the geometry is absent.
     */
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

    /** Human-readable label for a subject type. */
    private String typeLabel(String subjectType) {
        return switch (subjectType == null ? "" : subjectType) {
            case "habitat" -> "Habitat";
            case "biome" -> "Biome";
            case "trail" -> "Wildlife Trail";
            default -> subjectType;
        };
    }

    /** Reads a numeric column from a query row. */
    private double detailNumber(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }

    /** Reads an id column from a query row. */
    private long longValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value instanceof Number number ? number.longValue() : 0L;
    }

    /** Reads a text column from a query row. */
    private String text(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : value.toString();
    }

    /** Joins the non-empty parts with a separator. */
    private String join(String first, String second) {
        boolean hasFirst = first != null && !first.isBlank();
        boolean hasSecond = second != null && !second.isBlank();
        if (hasFirst && hasSecond) {
            return first + " · " + second;
        }
        return hasFirst ? first : (hasSecond ? second : "");
    }

    /**
     * A short area/perimeter summary of a biome, used as the detail of
     * the intersecting-biomes group. Returns an empty string when the
     * biome's details cannot be loaded, so one failing row never breaks
     * the whole analysis.
     */
    private String biomeSummary(long biomeId) {
        try {
            Map<String, Object> details = biomeService.getBiomeDetails(biomeId);
            if (details == null) {
                return "";
            }
            return "Area " + formatNumber(detailNumber(details, "area")) + " m²"
                    + " · Perimeter " + formatNumber(detailNumber(details, "perimeter")) + " m";
        } catch (Exception e) {
            return "";
        }
    }

    /** Formats a number with one decimal place, independent of the locale. */
    private String formatNumber(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    /** Builds the detail line of a sighting row (animal, time, notes). */
    private String sightingDetail(Map<String, Object> row) {
        StringBuilder detail = new StringBuilder();
        detail.append("Animal #").append(longValue(row, "animalId"));

        Object sightedAt = row.get("sightedAt");
        if (sightedAt instanceof Timestamp timestamp) {
            detail.append(" · ").append(timestamp.toLocalDateTime().format(DATE_TIME));
        } else if (sightedAt instanceof LocalDateTime localDateTime) {
            detail.append(" · ").append(localDateTime.format(DATE_TIME));
        }

        String notes = text(row, "notes");
        if (notes != null && !notes.isBlank()) {
            detail.append(" · ").append(notes);
        }

        return detail.toString();
    }
}
