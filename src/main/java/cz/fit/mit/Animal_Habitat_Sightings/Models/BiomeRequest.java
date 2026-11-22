package cz.fit.mit.Animal_Habitat_Sightings.Models;

public class BiomeRequest {
    public String biome_name;
    public String description;
    public int sdo_gtype;
    public Integer sdo_srid;
    public int[] sdo_elem_info;
    public double[] sdo_ordinates;

    public BiomeRequest(String biome_name, String description,
                        int sdo_gtype, Integer sdo_srid,
                        int[] sdo_elem_info, double[] sdo_ordinates) {
        this.biome_name = biome_name;
        this.description = description;
        this.sdo_gtype = sdo_gtype;
        this.sdo_srid = sdo_srid;
        this.sdo_elem_info = sdo_elem_info;
        this.sdo_ordinates = sdo_ordinates;
    }

    public String getBiome_name() {
        return biome_name;
    }

    public void setBiome_name(String biome_name) {
        this.biome_name = biome_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

