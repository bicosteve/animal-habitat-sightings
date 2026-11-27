package cz.fit.mit.Animal_Habitat_Sightings.service;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.model.Sighting;
import cz.fit.mit.Animal_Habitat_Sightings.repository.SightingImageRepository;
import cz.fit.mit.Animal_Habitat_Sightings.repository.SightingRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SightingService {

    private static final Logger LOGGER =
            Logger.getLogger(SightingService.class.getName());

    private final SightingRepository sightingRepository;
    private final SightingImageRepository sightingImageRepository;

    public SightingService(SightingRepository sightingRepository, SightingImageRepository sightingImageRepository) {
        this.sightingRepository = sightingRepository;
        this.sightingImageRepository = sightingImageRepository;
    }

    public long addSighting(Sighting sighting) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return sightingRepository.insertSighting(conn, sighting);
        }
    }

    /**
     * Saves the sighting and, if image bytes are supplied, attempts to attach
     * them (best-effort).
     *
     * <p>The sighting is always created when this method returns normally. If an
     * image was supplied but could not be stored (for example a corrupt or
     * non-image file), the failure is logged and the returned flag is
     * {@code false} so the caller can warn the user without failing the whole
     * request. When no image is supplied, the flag is {@code true}.
     *
     * @return {@code true} if there was nothing to attach or the image attached
     *         successfully; {@code false} if an image was supplied but failed.
     */
    public boolean addSighting(Sighting sighting, byte[] imageBytes) throws Exception {
        long sightingId = addSighting(sighting);

        boolean hasImage = imageBytes != null && imageBytes.length > 0;
        if (!hasImage) {
            return true;
        }

        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            // Returns false if the photo was stored but its SQL/MM Still Image
            // feature descriptors could not be built. Either way the photo is
            // saved; the flag lets the caller warn that it was not fully
            // processed (similarity-search metadata is missing).
            return sightingImageRepository.insertImage(conn, sightingId, imageBytes);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING,
                    "Sighting " + sightingId + " was created but its image could not be stored.",
                    ex);
            return false;
        }
    }

    public List<Sighting> getAllSightings() throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return sightingRepository.getAllSightings(conn);
        }
    }

    public List<Map<String, Object>> getAllSightingsForList() throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return sightingRepository.getAllSightingsForList(conn);
        }
    }

    public Map<String, Object> getSightingDetails(long sightingId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return sightingRepository.getSightingDetails(conn, sightingId);
        }
    }

    /**
     * Returns the biome(s) whose polygon contains this sighting's point,
     * computed live via Oracle Spatial. Empty if the point is outside every
     * biome; usually one entry; several if biome polygons overlap.
     */
    public List<Map<String, Object>> getSightingBiomes(long sightingId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return sightingRepository.getContainingBiomes(conn, sightingId);
        }
    }

    public void deleteSighting(long sightingId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            sightingRepository.deleteSighting(conn, sightingId);
        }
    }

    public Map<String, Object> getSightingImage(long sightingId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return sightingImageRepository.getImage(conn, sightingId);
        }
    }
}
