package com.example.noreact.model;

import java.util.List;
import java.io.Serializable;

public class RecipeItem implements Serializable{
    public String name;
    public String image;
    public List<String> ingredients;
    public String duration;
    public String allergen;
    public List<String> instructions;

    public RecipeItem(String name, String image, String duration, List<String> ingredients, String allergens, List<String> instructions) {

        this.name = name;
        this.allergen = allergens;
        this.image = image;
        this.ingredients = ingredients;
        this.duration = duration;
        this.instructions = instructions;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public String getDuration() {
        return duration;
    }

    public String getAllergen() {
        return allergen;
    }

    public String getImage() {
        return image;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public String getName() {
        return name;
    }

    public void setAllergen(String allergen) {
        this.allergen = allergen;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public void setName(String name) {
        this.name = name;
    }
}
