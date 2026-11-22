package cz.fit.mit.Animal_Habitat_Sightings.Models;

import java.util.List;

// Model for Inserting Habitat Data
public class HabitatRequest {
    public String habitat_name;
    public long animal_id;
    public int sdo_gtype;
    public Integer sdo_srid;
    public int[] sdo_elem_info;
    public double[] sdo_ordinates;

    public HabitatRequest(String habitat_name, long animal_id, int sdo_gtype, Integer sdo_srid, int[] sdo_elem_info, double[] sdo_ordinates) {
        this.habitat_name = habitat_name;
        this.animal_id = animal_id;
        this.sdo_gtype = sdo_gtype;
        this.sdo_srid = sdo_srid;
        this.sdo_elem_info = sdo_elem_info;
        this.sdo_ordinates = sdo_ordinates;
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

    public int getSdo_gtype() {
        return sdo_gtype;
    }

    public void setSdo_gtype(int sdo_gtype) {
        this.sdo_gtype = sdo_gtype;
    }

    public Integer getSdo_srid() {
        return sdo_srid;
    }

    public void setSdo_srid(Integer sdo_srid) {
        this.sdo_srid = sdo_srid;
    }

    public int[] getSdo_elem_info() {
        return sdo_elem_info;
    }

    public void setSdo_elem_info(int[] sdo_elem_info) {
        this.sdo_elem_info = sdo_elem_info;
    }

    public double[] getSdo_ordinates() {
        return sdo_ordinates;
    }

    public void setSdo_ordinates(double[] sdo_ordinates) {
        this.sdo_ordinates = sdo_ordinates;
    }
}
