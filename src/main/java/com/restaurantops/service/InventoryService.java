package com.restaurantops.service;

import com.restaurantops.model.InventoryItem;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryService {

    private final Map<String, InventoryItem> stock = new ConcurrentHashMap<>();

    public InventoryService() {
        stock.put("Pasta",       new InventoryItem("Pasta", 20));
        stock.put("Pizza",       new InventoryItem("Pizza", 15));
        stock.put("Burger",      new InventoryItem("Burger", 25));
        stock.put("Lemonade",    new InventoryItem("Lemonade", 30));
        stock.put("Gulab Jamun", new InventoryItem("Gulab Jamun", 20));
    }

    public boolean reserveIngredients(Order order) {
        MenuItem item = order.getItem();
        InventoryItem inv = stock.get(item.getName());
        if (inv == null) {
            System.out.println("   [INVENTORY] No stock record for " + item.getName());
            return false;
        }

        synchronized (inv) {
            if (!inv.hasAtLeast(order.getQuantity())) {
                System.out.println("   [INVENTORY] Out of stock for " + item.getName());
                return false;
            }
            inv.deduct(order.getQuantity());
        }
        System.out.println("   [INVENTORY] Reserved " + order.getQuantity() + " x " +
                item.getName() + " | Remaining: " + inv.getQuantity());
        return true;
    }

    public void restockLowItems() {
        for (InventoryItem item : stock.values()) {
            if (item.getQuantity() < 5) {
                item.add(10);
                System.out.println("   [INVENTORY] Restocked " + item.getName() +
                        " to " + item.getQuantity());
            }
        }
    }

    public void printInventory() {
        System.out.println("\n=== Current Inventory ===");
        stock.values().forEach(System.out::println);
    }
}
