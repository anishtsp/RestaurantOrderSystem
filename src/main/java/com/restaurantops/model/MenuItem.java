package com.restaurantops.model;

public class MenuItem {
    private final int id;
    private final String name;
    private final double price;

    public MenuItem(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return id + ". " + name + " - ₹" + price;
    }
}
