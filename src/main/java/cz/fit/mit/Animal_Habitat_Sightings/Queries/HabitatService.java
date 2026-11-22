package cz.fit.mit.Animal_Habitat_Sightings.Queries;

import cz.fit.mit.Animal_Habitat_Sightings.Models.Habitat;
import oracle.spatial.geometry.JGeometry;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class HabitatService {
    public void insertHabitat(Connection connection, Habitat habitat) throws Exception {
        String sql = ("""
                INSERT INTO habitats
                (habitat_name, animal_id, geom)
                values(?, ?, ?)
                """);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, habitat.getHabitat_name());
            stmt.setLong(2, habitat.getAnimal_id());

            if (habitat.getGeom() != null) {
                Struct obj = JGeometry.storeJS(connection, habitat.getGeom());
                stmt.setObject(3, obj);
            } else {
                stmt.setNull(3, Types.STRUCT);
            }
            stmt.executeUpdate();
        }
    }

    public List<Habitat> getAllHabitats(Connection connection) throws SQLException {
        List<Habitat> habitats = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM habitats")) {

            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    Struct obj = (Struct) rset.getObject("geom");

                    JGeometry geometry = obj != null
                            ? JGeometry.loadJS(obj)
                            : null;

                    Timestamp timestamp = rset.getTimestamp("created_at");
                    Habitat habitat = new Habitat(
                            rset.getLong("habitat_id"),
                            rset.getString("habitat_name"),
                            rset.getLong("animal_id"),
                            geometry,
                            timestamp != null ? timestamp.toLocalDateTime() : null
                    );

                    habitats.add(habitat);
                }
            }
        }

        return habitats;
    }

    public void deleteHabitat(Connection connection, long habitatId)
            throws SQLException {

        String sql = "DELETE FROM habitats WHERE habitat_id = ?";

        PreparedStatement stmt = connection.prepareStatement(sql);

        try {
            stmt.setLong(1, habitatId);
            stmt.executeUpdate();

        } finally {
            stmt.close();
        }
    }
}

