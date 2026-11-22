package cz.fit.mit.Animal_Habitat_Sightings.Models;

public class CheckpointRequest {
    public String checkpoint_name;
    public String checkpoint_type;
    public String description;
    public int sdo_gtype;
    public Integer sdo_srid;
    public int[] sdo_elem_info;
    public double[] sdo_ordinates;

    public CheckpointRequest(String checkpoint_name, String checkpoint_type, String description, int sdo_gtype, Integer sdo_srid, int[] sdo_elem_info, double[] sdo_ordinates) {
        this.checkpoint_name = checkpoint_name;
        this.checkpoint_type = checkpoint_type;
        this.description = description;
        this.sdo_gtype = sdo_gtype;
        this.sdo_srid = sdo_srid;
        this.sdo_elem_info = sdo_elem_info;
        this.sdo_ordinates = sdo_ordinates;
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
