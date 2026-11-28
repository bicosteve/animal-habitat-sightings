package cz.fit.mit.Animal_Habitat_Sightings.model.analysis;

/**
 * One related feature inside a relationship group: its identifier, a
 * display name, and a short human-readable detail (distance, date, type,
 * description, ...).
 *
 * <p>The spatial queries return heterogeneous {@code Map<String, Object>}
 * rows; the service layer normalises them into this type so the template
 * can render every group the same way.
 */
public record RelatedItem(long id, String name, String detail) {
}
