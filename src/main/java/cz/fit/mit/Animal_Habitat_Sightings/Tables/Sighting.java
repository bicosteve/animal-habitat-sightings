package cz.fit.mit.Animal_Habitat_Sightings.Tables;

import oracle.spatial.geometry.JGeometry;

import java.time.LocalDateTime;

public class Sighting {
    public long sighting_id;
    public long animal_id;
    public LocalDateTime sighted_at;
    public String notes;
    public JGeometry geom;

    public Sighting(long sighting_id, long animal_id, LocalDateTime sighted_at, String notes, JGeometry geom) {
        this.sighting_id = sighting_id;
        this.animal_id = animal_id;
        this.sighted_at = sighted_at;
        this.notes = notes;
        this.geom = geom;
    }

    public long getSighting_id() {
        return sighting_id;
    }

    public void setSighting_id(long sighting_id) {
        this.sighting_id = sighting_id;
    }

    public long getAnimal_id() {
        return animal_id;
    }

    public void setAnimal_id(long animal_id) {
        this.animal_id = animal_id;
    }

    public LocalDateTime getSighted_at() {
        return sighted_at;
    }

    public void setSighted_at(LocalDateTime sighted_at) {
        this.sighted_at = sighted_at;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public JGeometry getGeom() {
        return geom;
    }

    public void setGeom(JGeometry geom) {
        this.geom = geom;
    }
}
