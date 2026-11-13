package cz.fit.mit.Animal_Habitat_Sightings.Tables;

public class Animal {
    public Long animal_id;
    public String animal_name;
    public String scientific_name;
    public String description;

    public Animal(Long animal_id, String animal_name, String scientific_name, String description) {
        this.animal_id = animal_id;
        this.animal_name = animal_name;
        this.scientific_name = scientific_name;
        this.description = description;
    }

    public Long getAnimal_id() {
        return animal_id;
    }

    public void setAnimal_id(Long animal_id) {
        this.animal_id = animal_id;
    }

    public String getAnimal_name() {
        return animal_name;
    }

    public void setAnimal_name(String animal_name) {
        this.animal_name = animal_name;
    }

    public String getScientific_name() {
        return scientific_name;
    }

    public void setScientific_name(String scientific_name) {
        this.scientific_name = scientific_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
