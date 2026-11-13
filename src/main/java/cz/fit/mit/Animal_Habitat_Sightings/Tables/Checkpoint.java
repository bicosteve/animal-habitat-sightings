package cz.fit.mit.Animal_Habitat_Sightings.Tables;

import oracle.spatial.geometry.JGeometry;

import java.time.LocalDateTime;

public class Checkpoint {
    public long checkpoint_id;
    public String checkpoint_name;
    public String checkpoint_type;
    public String description;
    public JGeometry geom;
    public LocalDateTime created_at;

    public Checkpoint(long checkpoint_id, String checkpoint_name, String checkpoint_type, String description,
                      JGeometry geom, LocalDateTime created_at) {
        this.checkpoint_id = checkpoint_id;
        this.checkpoint_name = checkpoint_name;
        this.checkpoint_type = checkpoint_type;
        this.description = description;
        this.geom = geom;
        this.created_at = created_at;
    }

    public long getCheckpoint_id() {
        return checkpoint_id;
    }

    public void setCheckpoint_id(long checkpoint_id) {
        this.checkpoint_id = checkpoint_id;
    }

    public String getCheckpoint_name() {
        return checkpoint_name;
    }

    public void setCheckpoint_name(String checkpoint_name) {
        this.checkpoint_name = checkpoint_name;
    }

    public String getCheckpoint_type() {
        return checkpoint_type;
    }

    public void setCheckpoint_type(String checkpoint_type) {
        this.checkpoint_type = checkpoint_type;
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
