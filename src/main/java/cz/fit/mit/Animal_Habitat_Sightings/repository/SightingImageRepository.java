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

@Repository
public class SightingImageRepository {
    public void insertImage(
            Connection connection, long sightingId, byte[] imageData)
            throws Exception {

        // The ORDImage LOB write-back below (INSERT init -> SELECT ... FOR
        // UPDATE -> loadDataFromByteArray -> UPDATE) must run as a single
        // transaction. Under JDBC auto-commit each statement commits on its
        // own, which releases the FOR UPDATE lock early and leaves the LOB
        // locator populated with incomplete bytes -- the image is then stored
        // corrupt (broken on display) without raising an error. We therefore
        // switch auto-commit off for the duration and commit once at the end.
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
