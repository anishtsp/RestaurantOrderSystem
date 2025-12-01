package com.restaurantops.service;

import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuService {

    private final List<MenuItem> menu = new ArrayList<>();
    private final RecipeService recipeService;

    public MenuService(RecipeService recipeService) {
        this.recipeService = recipeService;
        loadDefaultMenu();
        loadDefaultRecipes();
    }

    private void loadDefaultMenu() {
        menu.add(new MenuItem(1,"pizza", 200));
        menu.add(new MenuItem(2, "burger", 150));
        menu.add(new MenuItem(3, "lemonade", 80));
        menu.add(new MenuItem(4, "cake", 120));
    }

    private void loadDefaultRecipes() {
        recipeService.addRecipe(new Recipe("pizza", Map.of(
                "dough", 1,
                "tomato_sauce", 1,
                "cheese", 1,
                "toppings", 1
        )));

        recipeService.addRecipe(new Recipe("burger", Map.of(
                "bun", 1,
                "patty", 1,
                "cheese", 1,
                "lettuce", 1
        )));

        recipeService.addRecipe(new Recipe("lemonade", Map.of(
                "lemon", 2,
                "sugar", 1,
                "water", 1
        )));

        recipeService.addRecipe(new Recipe("cake", Map.of(
                "flour", 1,
                "sugar", 1,
                "egg", 1,
                "cream", 1
        )));
    }

    public List<MenuItem> getAllItems() {
        return menu;
    }
    public MenuItem getById(int id) {
        if (id < 1 || id > menu.size()) return null;
        return menu.get(id - 1);
    }

    public void printMenu() {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem m = menu.get(i);
            System.out.println((i + 1) + ". " + m.getName() + " - ₹" + m.getPrice());
        }
    }
}
