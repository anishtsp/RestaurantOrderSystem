package com.restaurantops.model;

public class MenuItem {

    private final int id;
    private final String name;
    private final double price;

    // NEW FIELDS
    private final String categoryName;  // e.g., Grill, Dessert, Beverage
    private final Recipe recipe;        // nullable if no recipe exists

    public MenuItem(int id,
                    String name,
                    double price,
                    String categoryName,
                    Recipe recipe) {

        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryName = categoryName;
        this.recipe = recipe;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }

    public String getCategoryName() { return categoryName; }
    public Recipe getRecipe() { return recipe; }

    @Override
    public String toString() {
        return id + ". " + name + " - ₹" + price +
                (categoryName != null ? " [" + categoryName + "]" : "");
    }
}
