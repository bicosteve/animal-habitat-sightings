package cz.fit.mit.Animal_Habitat_Sightings;

import oracle.spatial.geometry.JGeometry;

import java.sql.SQLException;
import java.sql.Struct;

public class GeometryMapper {
    public static JGeometry toJGeometry(Struct struct) throws SQLException {
        if (struct == null) {
            return null;
        }

        return JGeometry.loadJS(struct);
    }
}
