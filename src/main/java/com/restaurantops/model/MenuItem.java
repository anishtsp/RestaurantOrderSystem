package com.restaurantops.model;

import java.util.List;

public class MenuItem {
    private final int id;
    private final String name;
    private final double price;
    private final String category;
    private final Recipe recipe;
    private final List<String> allergens;
    private final int calories;

    public MenuItem(int id,
                    String name,
                    double price,
                    String category,
                    Recipe recipe) {
        this(id, name, price, category, recipe, List.of(), 0);
    }

    public MenuItem(int id,
                    String name,
                    double price,
                    String category,
                    Recipe recipe,
                    List<String> allergens,
                    int calories) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.recipe = recipe;
        this.allergens = allergens;
        this.calories = calories;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public Recipe getRecipe() { return recipe; }
    public List<String> getAllergens() { return allergens; }
    public int getCalories() { return calories; }

    @Override
    public String toString() {
        return id + ". " + name + " - ₹" + price +
                " | " + calories + " kcal" +
                (!allergens.isEmpty() ? " | Allergens: " + allergens : "");
    }
}
