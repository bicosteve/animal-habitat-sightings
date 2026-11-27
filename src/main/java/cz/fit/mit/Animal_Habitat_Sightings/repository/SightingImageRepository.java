package cz.fit.mit.Animal_Habitat_Sightings.repository;

import oracle.jdbc.internal.OraclePreparedStatement;
import oracle.jdbc.internal.OracleResultSet;
import oracle.ord.im.OrdImage;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class SightingImageRepository {
    private static final Logger LOGGER =
            Logger.getLogger(SightingImageRepository.class.getName());

    /**
     * Stores the image for a sighting and then populates its Oracle Multimedia
     * SQL/MM Still Image feature descriptors.
     *
     * <p>The work is split into two transactions on purpose:
     * <ol>
     *   <li>The ORDImage (the actual photo) is stored and committed first, so
     *       the photo is always saved.</li>
     *   <li>The {@code SI_StillImage} + feature columns (average colour, colour
     *       histogram, positional colour, texture) are then derived from the
     *       stored image. If that step fails -- which can happen for valid but
     *       awkward images -- it is rolled back and logged, leaving those
     *       columns NULL, but the photo from step 1 is untouched.</li>
     * </ol>
     *
     * @return {@code true} if the still-image feature descriptors were
     *         populated; {@code false} if the photo was stored but the
     *         descriptors could not be (the photo still displays normally).
     */
    public boolean insertImage(
            Connection connection, long sightingId, byte[] imageData)
            throws Exception {

        storeOrdImage(connection, sightingId, imageData);
        return populateStillImageFeatures(connection, sightingId);
    }

    /**
     * Writes the ORDImage LOB in a single transaction.
     *
     * <p>The write-back (INSERT init -> SELECT ... FOR UPDATE ->
     * loadDataFromByteArray -> UPDATE) must run as one transaction. Under JDBC
     * auto-commit each statement commits on its own, which releases the FOR
     * UPDATE lock early and leaves the LOB locator populated with incomplete
     * bytes -- the image is then stored corrupt (broken on display) without
     * raising an error. We therefore switch auto-commit off for the duration
     * and commit once at the end.
     */
    private void storeOrdImage(
            Connection connection, long sightingId, byte[] imageData)
            throws Exception {

        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            String insertSql = """
                    INSERT INTO sighting_images (sighting_id, image)
                    VALUES (?, ORDSYS.ORDImage.init())
                    """;

            try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                stmt.setLong(1, sightingId);
                stmt.executeUpdate();
            }

            String selectSql = """
                    SELECT image
                    FROM sighting_images
                    WHERE sighting_id = ?
                    FOR UPDATE
                    """;

            try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
                stmt.setLong(1, sightingId);

                try (ResultSet resultSet = stmt.executeQuery()) {

                    if (!resultSet.next()) {
                        throw new SQLException("Sighting image record not found.");
                    }

                    OracleResultSet rset = (OracleResultSet) resultSet;

                    OrdImage image = (OrdImage) rset.getORAData(
                            1, OrdImage.getORADataFactory());

                    image.loadDataFromByteArray(imageData);
                    image.setProperties();

                    String updateSql = """
                            UPDATE sighting_images
                            SET image = ?
                            WHERE sighting_id = ?
                            """;

                    try (PreparedStatement updateStmt =
                                 connection.prepareStatement(updateSql)) {

                        OraclePreparedStatement oracleStmt =
                                (OraclePreparedStatement) updateStmt;

                        oracleStmt.setORAData(1, image);
                        oracleStmt.setLong(2, sightingId);
                        oracleStmt.executeUpdate();
                    }
                }
            }

            connection.commit();
        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    /**
     * Populates the SQL/MM Still Image columns from the already-stored
     * ORDImage, in its own transaction.
     *
     * <p>Per the Oracle Multimedia convention, {@code image_si} is built from
     * the image's BLOB content ({@code ORDSYS.ORDImage.getContent}), and the
     * four feature descriptors are derived from that {@code SI_StillImage}.
     * Because the features depend on {@code image_si}, they are set in a second
     * statement after it exists.
     *
     * @return {@code true} on success; {@code false} if extraction failed (the
     *         columns are left NULL and the photo is unaffected).
     */
    private boolean populateStillImageFeatures(Connection connection, long sightingId) {
        boolean previousAutoCommit;
        try {
            previousAutoCommit = connection.getAutoCommit();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING,
                    "Could not read auto-commit state while building image features "
                            + "for sighting " + sightingId + "; skipping features.", ex);
            return false;
        }

        try {
            connection.setAutoCommit(false);

            // Step 1: build the SI_StillImage from the stored image's BLOB
            // content. ORDImage.getContent() is a member function, so it is
            // called on the column via a table alias (member-call form in DML).
            String stillImageSql = """
                    UPDATE sighting_images si
                    SET si.image_si = SI_StillImage(si.image.getContent())
                    WHERE si.sighting_id = ?
                    """;

            try (PreparedStatement stmt = connection.prepareStatement(stillImageSql)) {
                stmt.setLong(1, sightingId);
                stmt.executeUpdate();
            }

            // Step 2: derive the four feature descriptors from image_si.
            String featuresSql = """
                    UPDATE sighting_images
                    SET image_ac = SI_AverageColor(image_si),
                        image_ch = SI_ColorHistogram(image_si),
                        image_pc = SI_PositionalColor(image_si),
                        image_tx = SI_Texture(image_si)
                    WHERE sighting_id = ?
                      AND image_si IS NOT NULL
                    """;

            try (PreparedStatement stmt = connection.prepareStatement(featuresSql)) {
                stmt.setLong(1, sightingId);
                stmt.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING,
                    "Sighting " + sightingId + " image was stored, but its SQL/MM "
                            + "Still Image feature descriptors could not be built; "
                            + "those columns remain NULL.", ex);
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.WARNING,
                        "Rollback of image-feature population failed for sighting "
                                + sightingId + ".", rollbackEx);
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(previousAutoCommit);
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING,
                        "Could not restore auto-commit after image-feature population "
                                + "for sighting " + sightingId + ".", ex);
            }
        }
    }

    public Map<String, Object> getImage(Connection connection, long sightingId) throws Exception {
        String sql = """
            SELECT image
            FROM sighting_images
            WHERE sighting_id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, sightingId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    OracleResultSet rset = (OracleResultSet) resultSet;

                    OrdImage image = (OrdImage) rset.getORAData(
                            1, OrdImage.getORADataFactory());

                    if (image == null || image.getContentLength() == 0) {
                        return null;
                    }

                    byte[] imageData = image.getDataInByteArray();

                    Map<String, Object> result = new HashMap<>();
                    result.put("imageData", imageData);
                    result.put("mimeType", image.getMimeType());

                    return result;
                }
            }
        }
        return null;
    }
}
