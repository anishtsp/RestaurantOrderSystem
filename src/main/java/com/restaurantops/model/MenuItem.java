package com.restaurantops.model;

public class MenuItem {

    private final int id;
    private final String name;
    private final double price;
    private final String category;
    private final Recipe recipe;

    public MenuItem(int id, String name, double price, String category, Recipe recipe) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.recipe = recipe;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public Recipe getRecipe() { return recipe; }

    @Override
    public String toString() {
        return id + ". " + name + " - ₹" + price + " [" + category + "]";
    }
}
