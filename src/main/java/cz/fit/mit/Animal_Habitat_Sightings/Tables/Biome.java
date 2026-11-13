package cz.fit.mit.Animal_Habitat_Sightings.Tables;

import oracle.spatial.geometry.JGeometry;

public class Biome {
    public long biome_id;
    public String biome_name;
    public String description;
    public JGeometry geom;

    public Biome(long biome_id, String biome_name, String description, JGeometry geom) {
        this.biome_id = biome_id;
        this.biome_name = biome_name;
        this.description = description;
        this.geom = geom;
    }

    public long getBiome_id() {
        return biome_id;
    }

    public void setBiome_id(long biome_id) {
        this.biome_id = biome_id;
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

    public JGeometry getGeom() {
        return geom;
    }

    public void setGeom(JGeometry geom) {
        this.geom = geom;
    }
}
