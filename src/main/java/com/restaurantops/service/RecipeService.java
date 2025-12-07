package com.restaurantops.service;

import com.restaurantops.model.Recipe;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RecipeService {

    private final Map<String, Recipe> recipes = new HashMap<>();

    public void addRecipe(Recipe r) {
        if (r == null) return;
        recipes.put(r.getDishName().toLowerCase(), r);
    }

    public Recipe getRecipeForDish(String dish) {
        if (dish == null) return null;
        return recipes.get(dish.toLowerCase());
    }

    public Map<String, Recipe> getAllRecipes() {
        return Collections.unmodifiableMap(recipes);
    }

    public void loadDefaultRecipes() {
        addRecipe(new Recipe("margherita pizza", Map.of(
                "dough", 1,
                "tomato_sauce", 1,
                "cheese", 1,
                "toppings", 1
        )));
        addRecipe(new Recipe("pepperoni pizza", Map.of(
                "dough", 1,
                "tomato_sauce", 1,
                "cheese", 1,
                "pepperoni", 3
        )));
        addRecipe(new Recipe("veg burger", Map.of(
                "bun", 1,
                "patty", 1,
                "cheese", 1,
                "lettuce", 1
        )));
        addRecipe(new Recipe("chicken burger", Map.of(
                "bun", 1,
                "patty_chicken", 1,
                "cheese", 1,
                "lettuce", 1
        )));
        addRecipe(new Recipe("cheesy pasta", Map.of(
                "pasta", 1,
                "tomato_sauce", 1,
                "cheese", 1
        )));
        addRecipe(new Recipe("lemonade", Map.of(
                "lemon", 2,
                "sugar", 1,
                "water", 1
        )));
        addRecipe(new Recipe("iced tea", Map.of(
                "tea_leaves", 1,
                "sugar", 1,
                "water", 1,
                "ice", 3
        )));
        addRecipe(new Recipe("espresso", Map.of(
                "coffee_beans", 1,
                "water", 1
        )));
        addRecipe(new Recipe("cappuccino", Map.of(
                "coffee_beans", 1,
                "milk", 1,
                "water", 1
        )));
        addRecipe(new Recipe("hot chocolate", Map.of(
                "cocoa", 1,
                "milk", 1,
                "sugar", 1
        )));
        addRecipe(new Recipe("chocolate cake", Map.of(
                "flour", 1,
                "sugar", 1,
                "egg", 1,
                "cream", 1
        )));
        addRecipe(new Recipe("pancakes", Map.of(
                "pancake_batter", 1,
                "egg", 1,
                "milk", 1
        )));
        addRecipe(new Recipe("omelette", Map.of(
                "egg", 2,
                "milk", 1,
                "cheese", 1
        )));
        addRecipe(new Recipe("french fries", Map.of(
                "potato", 3,
                "salt", 1,
                "oil", 1
        )));
        addRecipe(new Recipe("garlic bread", Map.of(
                "bread", 2,
                "garlic", 1,
                "butter", 1
        )));
        addRecipe(new Recipe("chicken tikka", Map.of(
                "chicken", 1,
                "spices", 1,
                "yogurt", 1
        )));
        addRecipe(new Recipe("fish and chips", Map.of(
                "fish", 1,
                "potato", 2,
                "oil", 1
        )));
        addRecipe(new Recipe("green salad", Map.of(
                "lettuce", 1,
                "tomato", 1,
                "cucumber", 1
        )));
        addRecipe(new Recipe("mango smoothie", Map.of(
                "mango", 1,
                "milk", 1,
                "sugar", 1
        )));
        addRecipe(new Recipe("combo meal 1", Map.of(
                "dough", 1,
                "tomato_sauce", 1,
                "cheese", 1,
                "patty", 1,
                "potato", 2
        )));
    }
}
