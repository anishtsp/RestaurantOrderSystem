package com.restaurantops.service;

import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuService {

    private final List<MenuItem> items = new ArrayList<>();
    private final RecipeService recipeService;
    private final Map<String, Recipe> defaultRecipes = new HashMap<>();

    public MenuService(RecipeService recipeService) {
        this.recipeService = recipeService;
        loadDefaultRecipes();
        loadDefaultMenu();
    }

    private void loadDefaultRecipes() {
        defaultRecipes.put("margherita pizza", new Recipe("margherita pizza", Map.of(
                "dough", 1,
                "tomato_sauce", 1,
                "cheese", 1,
                "toppings", 1
        )));

        defaultRecipes.put("veg burger", new Recipe("veg burger", Map.of(
                "bun", 1,
                "patty", 1,
                "cheese", 1,
                "lettuce", 1
        )));

        defaultRecipes.put("lemonade", new Recipe("lemonade", Map.of(
                "lemon", 2,
                "sugar", 1,
                "water", 1
        )));

        defaultRecipes.put("chocolate cake", new Recipe("chocolate cake", Map.of(
                "flour", 1,
                "sugar", 1,
                "egg", 1,
                "cream", 1
        )));

        // register to RecipeService so others can look them up
        for (Recipe r : defaultRecipes.values()) {
            recipeService.addRecipe(r);
        }
    }

    private void loadDefaultMenu() {
        items.clear();
        items.add(new MenuItem(1, "Margherita Pizza", 250.0, "Grill", recipeService.getRecipeForDish("margherita pizza")));
        items.add(new MenuItem(2, "Veg Burger", 180.0, "Grill", recipeService.getRecipeForDish("veg burger")));
        items.add(new MenuItem(3, "Lemonade", 90.0, "Beverage", recipeService.getRecipeForDish("lemonade")));
        items.add(new MenuItem(4, "Chocolate Cake", 150.0, "Dessert", recipeService.getRecipeForDish("chocolate cake")));
    }

    public List<MenuItem> getAllItems() {
        return items;
    }

    public MenuItem getById(int id) {
        for (MenuItem m : items) {
            if (m.getId() == id) return m;
        }
        return null;
    }

    public void printMenu() {
        for (MenuItem m : items) {
            System.out.println(m);
        }
    }
}
