package com.restaurantops.service;

import com.restaurantops.model.Recipe;

import java.util.HashMap;
import java.util.Map;

public class RecipeService {

    private final Map<String, Recipe> recipes = new HashMap<>();

    public void addRecipe(Recipe recipe) {
        recipes.put(recipe.getDishName().toLowerCase(), recipe);
    }

    public Recipe getRecipeForDish(String dishName) {
        return recipes.get(dishName.toLowerCase());
    }

    public Map<String, Recipe> getAllRecipes() {
        return recipes;
    }
}
