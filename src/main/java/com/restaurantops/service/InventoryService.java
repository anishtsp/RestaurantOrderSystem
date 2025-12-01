package com.restaurantops.service;

import com.restaurantops.model.InventoryItem;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.model.Recipe;

import java.util.HashMap;
import java.util.Map;

public class InventoryService {

    private final Map<String, InventoryItem> inventory = new HashMap<>();

    public void addItem(String name, int qty, long expiryMillis) {
        inventory.put(name.toLowerCase(),
                new InventoryItem(name, qty, expiryMillis));
    }

    public boolean reserveIngredients(Order order) {
        MenuItem item = order.getItem();
        String key = item.getName().toLowerCase();

        InventoryItem inv = inventory.get(key);
        if (inv == null) return false;
        if (inv.isExpired()) return false;
        if (inv.getQuantity() < order.getQuantity()) return false;

        inv.reduce(order.getQuantity());
        return true;
    }

    // atomically reserve a recipe; returns true if reserved, false otherwise
    public synchronized boolean reserveRecipe(Recipe recipe, int servings) {
        Map<String, Integer> needed = new HashMap<>();
        for (Map.Entry<String,Integer> e : recipe.getIngredients().entrySet()) {
            String key = e.getKey().toLowerCase();
            int totalNeeded = e.getValue() * servings;
            InventoryItem inv = inventory.get(key);
            if (inv == null || inv.isExpired() || inv.getQuantity() < totalNeeded) {
                return false;
            }
            needed.put(key, totalNeeded);
        }
        // commit reductions
        for (Map.Entry<String,Integer> e : needed.entrySet()) {
            inventory.get(e.getKey()).reduce(e.getValue());
        }
        return true;
    }

    // find low stock items under threshold
    public synchronized Map<String, InventoryItem> getLowStock(int threshold) {
        Map<String, InventoryItem> low = new HashMap<>();
        for (Map.Entry<String, InventoryItem> e : inventory.entrySet()) {
            if (e.getValue().getQuantity() <= threshold) low.put(e.getKey(), e.getValue());
        }
        return low;
    }

    // restock item (used by SupplierService)
    public synchronized void restock(String name, int qty, long newExpiryMillis) {
        String key = name.toLowerCase();
        InventoryItem item = inventory.get(key);
        if (item == null) inventory.put(key, new InventoryItem(name, qty, newExpiryMillis));
        else {
            item.reduce(-qty); // implement increase in InventoryItem or add method
            // ensure expiry updated if later than current
        }
    }


    public void printInventory() {
        for (InventoryItem i : inventory.values()) {
            System.out.println(i);
        }
    }

    public void refreshExpiries() {
        inventory.values().removeIf(InventoryItem::isExpired);
    }

    public Map<String, InventoryItem> getInventory() {
        return inventory;
    }
}
