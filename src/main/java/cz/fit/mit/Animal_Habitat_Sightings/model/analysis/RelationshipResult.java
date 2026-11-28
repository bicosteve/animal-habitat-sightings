package cz.fit.mit.Animal_Habitat_Sightings.model.analysis;

import java.util.List;
import java.util.Map;

/**
 * The outcome of a spatial-relationship analysis: the feature that was
 * analysed, the groups of related features found for it, and the
 * map-ready geometry of the subject plus every related feature.
 *
 * @param subjectType      entity type of the subject ("habitat"/"biome"/"trail")
 * @param subjectTypeLabel human-readable label for that type
 * @param subjectName      name of the analysed feature
 * @param groups           the relationship groups (may be empty)
 * @param mapFeatures      map-ready features; each is a map with the keys
 *                         type, id, name, role, geometry, and point
 */
public record RelationshipResult(String subjectType,
                                 String subjectTypeLabel,
                                 String subjectName,
                                 List<RelationshipGroup> groups,
                                 List<Map<String, Object>> mapFeatures) {
}
