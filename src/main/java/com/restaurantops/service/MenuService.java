package com.restaurantops.service;

import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MenuService - now with basic CRUD and recipe access helpers.
 */
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
        defaultRecipes.put("margherita pizza", new Recipe("margherita pizza",
                Map.of("dough", 1, "tomato_sauce", 1, "cheese", 1)));

        defaultRecipes.put("pepperoni pizza", new Recipe("pepperoni pizza",
                Map.of("dough", 1, "tomato_sauce", 1, "cheese", 1, "pepperoni", 2)));

        defaultRecipes.put("veg burger", new Recipe("veg burger",
                Map.of("bun", 1, "patty", 1, "cheese", 1, "lettuce", 1)));

        defaultRecipes.put("chicken burger", new Recipe("chicken burger",
                Map.of("bun", 1, "chicken_patty", 1, "lettuce", 1)));

        defaultRecipes.put("pasta alfredo", new Recipe("pasta alfredo",
                Map.of("pasta", 1, "cream", 1, "cheese", 1)));

        defaultRecipes.put("pasta arrabiata", new Recipe("pasta arrabiata",
                Map.of("pasta", 1, "tomato_sauce", 1, "chili", 1)));

        defaultRecipes.put("lemonade", new Recipe("lemonade",
                Map.of("lemon", 2, "sugar", 1, "water", 1)));

        defaultRecipes.put("iced tea", new Recipe("iced tea",
                Map.of("tea", 1, "sugar", 1, "ice", 1)));

        defaultRecipes.put("hot coffee", new Recipe("hot coffee",
                Map.of("coffee", 1, "milk", 1, "water", 1)));

        defaultRecipes.put("cold coffee", new Recipe("cold coffee",
                Map.of("coffee", 1, "milk", 1, "ice", 1, "sugar", 1)));

        defaultRecipes.put("chocolate cake", new Recipe("chocolate cake",
                Map.of("flour", 1, "sugar", 1, "egg", 1, "cream", 1, "cocoa", 1)));

        defaultRecipes.put("gulab jamun", new Recipe("gulab jamun",
                Map.of("khoya", 1, "sugar_syrup", 1)));

        for (Recipe r : defaultRecipes.values()) {
            recipeService.addRecipe(r);
        }
    }

    private void loadDefaultMenu() {
        items.clear();

        items.add(new MenuItem(1, "Margherita Pizza", 250, "Grill",
                recipeService.getRecipeForDish("margherita pizza"),
                List.of("dairy", "gluten"), 700));

        items.add(new MenuItem(2, "Pepperoni Pizza", 320, "Grill",
                recipeService.getRecipeForDish("pepperoni pizza"),
                List.of("dairy", "gluten"), 850));

        items.add(new MenuItem(3, "Veg Burger", 180, "Grill",
                recipeService.getRecipeForDish("veg burger"),
                List.of("gluten", "soy", "dairy"), 500));

        items.add(new MenuItem(4, "Chicken Burger", 210, "Grill",
                recipeService.getRecipeForDish("chicken burger"),
                List.of("gluten"), 550));

        items.add(new MenuItem(5, "Pasta Alfredo", 260, "Grill",
                recipeService.getRecipeForDish("pasta alfredo"),
                List.of("dairy", "gluten"), 600));

        items.add(new MenuItem(6, "Pasta Arrabiata", 240, "Grill",
                recipeService.getRecipeForDish("pasta arrabiata"),
                List.of("gluten"), 550));

        items.add(new MenuItem(7, "Lemonade", 90, "ColdBeverage",
                recipeService.getRecipeForDish("lemonade"),
                List.of(), 150));

        items.add(new MenuItem(8, "Iced Tea", 110, "ColdBeverage",
                recipeService.getRecipeForDish("iced tea"),
                List.of(), 120));

        items.add(new MenuItem(9, "Cold Coffee", 150, "ColdBeverage",
                recipeService.getRecipeForDish("cold coffee"),
                List.of("dairy"), 220));

        items.add(new MenuItem(10, "Hot Coffee", 120, "HotBeverage",
                recipeService.getRecipeForDish("hot coffee"),
                List.of("dairy"), 180));

        items.add(new MenuItem(11, "Chocolate Cake", 150, "Dessert",
                recipeService.getRecipeForDish("chocolate cake"),
                List.of("dairy", "egg", "gluten"), 450));

        items.add(new MenuItem(12, "Gulab Jamun", 80, "Dessert",
                recipeService.getRecipeForDish("gulab jamun"),
                List.of("dairy"), 300));
    }

    // --------------------
    // READ
    // --------------------
    public synchronized List<MenuItem> getAllItems() {
        return new ArrayList<>(items);
    }

    public synchronized MenuItem getById(int id) {
        for (MenuItem m : items) {
            if (m.getId() == id) return m;
        }
        return null;
    }

    public synchronized void printMenu() {
        for (MenuItem m : items) {
            System.out.println(m);
        }
    }

    // --------------------
    // RECIPES helper (exposed for UI)
    // --------------------
    public Map<String, Recipe> getAllRecipes() {
        return recipeService.getAllRecipes();
    }

    public Recipe getRecipeByName(String name) {
        return recipeService.getRecipeForDish(name);
    }

    // --------------------
    // CRUD
    // --------------------
    public synchronized int getNextId() {
        int max = 0;
        for (MenuItem m : items) {
            if (m.getId() > max) max = m.getId();
        }
        return max + 1;
    }

    public synchronized boolean addMenuItem(MenuItem item) {
        if (item == null) return false;
        // ensure no duplicate id
        if (getById(item.getId()) != null) return false;
        items.add(item);
        return true;
    }

    public synchronized boolean editMenuItem(MenuItem updated) {
        if (updated == null) return false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == updated.getId()) {
                items.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean removeMenuItem(int id) {
        return items.removeIf(m -> m.getId() == id);
    }
}
