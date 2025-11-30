package com.restaurantops.model;

public class InventoryItem {
    private final String name;
    private int quantity;

    public InventoryItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public int getQuantity() { return quantity; }

    public boolean hasAtLeast(int required) {
        return quantity >= required;
    }

    public void deduct(int amount) {
        if (amount <= 0) return;
        if (amount > quantity) {
            throw new IllegalArgumentException("Not enough stock for " + name);
        }
        quantity -= amount;
    }

    public void add(int amount) {
        if (amount > 0) quantity += amount;
    }

    @Override
    public String toString() {
        return name + " = " + quantity + " units";
    }
}
