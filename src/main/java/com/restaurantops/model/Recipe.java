package com.restaurantops.model;

import java.util.Map;

public class Recipe {

    private final String dishName;
    private final Map<String, Integer> ingredients;

    public Recipe(String dishName, Map<String, Integer> ingredients) {
        this.dishName = dishName;
        this.ingredients = ingredients;
    }

    public String getDishName() {
        return dishName;
    }

    public Map<String, Integer> getIngredients() {
        return ingredients;
    }
}
