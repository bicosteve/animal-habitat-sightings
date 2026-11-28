package cz.fit.mit.Animal_Habitat_Sightings.model.analysis;

import java.util.List;

/**
 * A titled group of related features, e.g. "Biomes intersecting this
 * habitat". The description explains the spatial predicate that produced
 * the group (which Oracle Spatial function and mask were used).
 */
public record RelationshipGroup(String title, String description, List<RelatedItem> items) {
}
