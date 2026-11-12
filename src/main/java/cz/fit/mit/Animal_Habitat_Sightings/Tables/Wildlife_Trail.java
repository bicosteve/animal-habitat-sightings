package cz.fit.mit.Animal_Habitat_Sightings.Tables;

import oracle.spatial.geometry.JGeometry;

import java.time.LocalDateTime;

public class Wildlife_Trail {
    public long trail_id;
    public String trail_name;
    public String description;
    public JGeometry geom;
    public LocalDateTime created_at;

    public Wildlife_Trail(long trail_id, String trail_name, String description, JGeometry geom, LocalDateTime created_at) {
        this.trail_id = trail_id;
        this.trail_name = trail_name;
        this.description = description;
        this.geom = geom;
        this.created_at = created_at;
    }

    public long getTrail_id() {
        return trail_id;
    }

    public void setTrail_id(long trail_id) {
        this.trail_id = trail_id;
    }

    public String getTrail_name() {
        return trail_name;
    }

    public void setTrail_name(String trail_name) {
        this.trail_name = trail_name;
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

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}
