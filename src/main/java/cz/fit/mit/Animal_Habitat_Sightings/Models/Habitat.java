package cz.fit.mit.Animal_Habitat_Sightings.Models;
import com.fasterxml.jackson.annotation.JsonIgnore;
import oracle.spatial.geometry.JGeometry;

import java.time.LocalDateTime;

public class Habitat {
    public long habitat_id;
    public String habitat_name;
    public long animal_id;
    public JGeometry geom;
    public LocalDateTime created_at;

    public Habitat(long habitat_id, String habitat_name, long animal_id, JGeometry geom, LocalDateTime created_at) {
        this.habitat_id = habitat_id;
        this.habitat_name = habitat_name;
        this.animal_id = animal_id;
        this.geom = geom;
        this.created_at = created_at;
    }

    public long getHabitat_id() {
        return habitat_id;
    }

    public void setHabitat_id(long habitat_id) {
        this.habitat_id = habitat_id;
    }

    public String getHabitat_name() {
        return habitat_name;
    }

    public void setHabitat_name(String habitat_name) {
        this.habitat_name = habitat_name;
    }

    public long getAnimal_id() {
        return animal_id;
    }

    public void setAnimal_id(long animal_id) {
        this.animal_id = animal_id;
    }

    @JsonIgnore
    public JGeometry getGeom() {
        return geom;
    }

    public void setGeom(JGeometry geom) {
        this.geom = geom;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public int getSdo_gtype() {
        return geom.getType();
    }

    public int getSdo_srid() {
        return geom.getSRID();
    }

    public int[] getSdo_elem_info() {
        return geom.getElemInfo();
    }

    public double[] getSdo_ordinates() {
        return geom.getOrdinatesArray();
    }
}
