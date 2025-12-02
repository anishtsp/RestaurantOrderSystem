package com.restaurantops.service;

import com.restaurantops.model.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InventoryInitializer {

    public static void syncMenuToInventory(List<MenuItem> menuItems,
                                           InventoryService inventoryService) {

        for (MenuItem m : menuItems) {
            if (m.getRecipe() == null) continue;

            for (Map.Entry<String, Integer> e : m.getRecipe().getIngredients().entrySet()) {
                String ingredient = e.getKey();
                int qty = 20; // initial stock
                long expiry = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 2;

                inventoryService.addItem(ingredient, qty, expiry);
            }
        }
    }
}
