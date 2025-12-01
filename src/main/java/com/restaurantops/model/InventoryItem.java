package com.restaurantops.model;

public class InventoryItem {

    private final String name;
    private int quantity;
    private final long expiryTimestamp;

    public InventoryItem(String name, int quantity, long expiryTimestamp) {
        this.name = name;
        this.quantity = quantity;
        this.expiryTimestamp = expiryTimestamp;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTimestamp;
    }

    public void reduce(int qty) {
        quantity -= qty;
        if (quantity < 0) quantity = 0;
    }

    @Override
    public String toString() {
        return name + " | qty=" + quantity +
                " | expired=" + isExpired();
    }
}
