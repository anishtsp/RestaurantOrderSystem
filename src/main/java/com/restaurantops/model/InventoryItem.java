package com.restaurantops.model;

public class InventoryItem {

    private final String name;
    private int quantity;
    private long expiryTimestamp;

    public InventoryItem(String name, int quantity, long expiryTimestamp) {
        this.name = name;
        this.quantity = Math.max(0, quantity);
        this.expiryTimestamp = expiryTimestamp;
    }

    public String getName() {
        return name;
    }

    public synchronized int getQuantity() {
        return quantity;
    }

    public synchronized boolean isExpired() {
        return System.currentTimeMillis() > expiryTimestamp;
    }

    public synchronized void reduce(int qty) {
        quantity -= qty;
        if (quantity < 0) quantity = 0;
    }

    public synchronized void increase(int qty) {
        if (qty <= 0) return;
        quantity += qty;
    }

    public synchronized void setExpiry(long newExpiry) {
        this.expiryTimestamp = newExpiry;
    }

    @Override
    public String toString() {
        return name + " | qty=" + quantity +
                " | expired=" + isExpired();
    }

    public synchronized long getExpiryTimestamp() {
        return expiryTimestamp;
    }

}
