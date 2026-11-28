package cz.fit.mit.Animal_Habitat_Sightings.repository;

import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class AnalysisRepository {

    private static final double TOLERANCE = 0.05;

    public List<Map<String, Object>> getAreaAndPerimeter(Connection connection) throws SQLException {
        String sql = """
                SELECT 'Habitat' AS type, habitat_name AS name,
                       SDO_GEOM.SDO_AREA(geom, ?, 'unit=SQ_M')   AS area,
                       SDO_GEOM.SDO_LENGTH(geom, ?, 'unit=M')    AS perimeter,
                       geom,
                       SDO_GEOM.SDO_CENTROID(geom, ?)            AS centroid
                FROM habitats
                UNION ALL
                SELECT 'Biome' AS type, biome_name AS name,
                       SDO_GEOM.SDO_AREA(geom, ?, 'unit=SQ_M')   AS area,
                       SDO_GEOM.SDO_LENGTH(geom, ?, 'unit=M')    AS perimeter,
                       geom,
                       SDO_GEOM.SDO_CENTROID(geom, ?)            AS centroid
                FROM biomes
                ORDER BY type, name
                """;

        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, TOLERANCE);
            stmt.setDouble(2, TOLERANCE);
            stmt.setDouble(3, TOLERANCE);
            stmt.setDouble(4, TOLERANCE);
            stmt.setDouble(5, TOLERANCE);
            stmt.setDouble(6, TOLERANCE);

            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("type", rset.getString("type"));
                    row.put("name", rset.getString("name"));
                    row.put("area", rset.getDouble("area"));
                    row.put("perimeter", rset.getDouble("perimeter"));

                    Struct geomObj = (Struct) rset.getObject("geom");
                    row.put("geometry", geomObj != null ? JGeometry.loadJS(geomObj) : null);

                    Struct centroidObj = (Struct) rset.getObject("centroid");
                    row.put("centroid", centroidObj != null ? JGeometry.loadJS(centroidObj) : null);

                    rows.add(row);
                }
            }
        }

        return rows;
    }


    public Double getDistance(Connection connection,
                              String fromTable, String fromPk, long fromId,
                              String toTable, String toPk, long toId) throws SQLException {

        String sql = "SELECT SDO_GEOM.SDO_DISTANCE(a.geom, b.geom, ?, 'unit=M') AS distance "
                + "FROM " + fromTable + " a, " + toTable + " b "
                + "WHERE a." + fromPk + " = ? AND b." + toPk + " = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, TOLERANCE);
            stmt.setLong(2, fromId);
            stmt.setLong(3, toId);

            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    return rset.getDouble("distance");
                }
            }
        }

        return null;
    }


    public Map<String, Object> getFeatureForMap(Connection connection,
                                                String table, String pk, String nameColumn,
                                                long id) throws SQLException {

        // SDO_POINTONSURFACE works for polygons, lines and points, giving a
        // representative point for any geometry type.
        String sql = "SELECT " + nameColumn + " AS name, geom, "
                + "SDO_GEOM.SDO_POINTONSURFACE(geom, ?) AS rep_point "
                + "FROM " + table + " WHERE " + pk + " = ?";

        Map<String, Object> feature = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, TOLERANCE);
            stmt.setLong(2, id);

            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    feature.put("name", rset.getString("name"));

                    Struct geomObj = (Struct) rset.getObject("geom");
                    feature.put("geometry", geomObj != null ? JGeometry.loadJS(geomObj) : null);

                    Struct pointObj = (Struct) rset.getObject("rep_point");
                    feature.put("point", pointObj != null ? JGeometry.loadJS(pointObj) : null);
                }
            }
        }

        return feature;
    }
}
