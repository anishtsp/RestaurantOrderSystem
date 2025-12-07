package com.restaurantops.inventory;

import com.restaurantops.model.MenuItem;

import java.util.List;

public class InventoryInitializer {

    public static void syncMenuToInventory(List<MenuItem> menu, InventoryService inventory) {
        add(inventory, "dough", 40);
        add(inventory, "tomato_sauce", 40);
        add(inventory, "cheese", 60);
        add(inventory, "toppings", 40);

        add(inventory, "pepperoni", 40);

        add(inventory, "bun", 40);
        add(inventory, "patty", 40);
        add(inventory, "chicken_patty", 40);
        add(inventory, "lettuce", 40);

        add(inventory, "pasta", 40);
        add(inventory, "cream", 40);
        add(inventory, "chili", 40);

        add(inventory, "lemon", 40);
        add(inventory, "sugar", 60);
        add(inventory, "water", 200);
        add(inventory, "tea", 40);
        add(inventory, "ice", 200);
        add(inventory, "coffee", 80);
        add(inventory, "milk", 80);

        add(inventory, "flour", 40);
        add(inventory, "egg", 40);
        add(inventory, "cocoa", 40);
        add(inventory, "khoya", 40);
        add(inventory, "sugar_syrup", 40);
    }

    private static void add(InventoryService inv, String ingredient, int qty) {
        inv.addItem(ingredient, qty, System.currentTimeMillis() + 86400000);
    }
}
