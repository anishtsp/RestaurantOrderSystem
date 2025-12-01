package com.restaurantops.service;

import com.restaurantops.model.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InventoryInitializer {

    private static final Random random = new Random();

    // Overload #1 (List version)
    public static void syncMenuToInventory(List<MenuItem> menu,
                                           InventoryService inventory) {

        if (menu == null || inventory == null) return;

        long expiry = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 3);

        for (MenuItem item : menu) {
            if (item == null) continue;
            String key = item.getName().toLowerCase();
            int qty = 10 + random.nextInt(31);
            inventory.addItem(key, qty, expiry);
        }
    }

    // Overload #2 (Map version)
    public static void syncMenuToInventory(Map<Integer, MenuItem> menu,
                                           InventoryService inventory) {

        if (menu == null) return;

        List<MenuItem> list = new ArrayList<>(menu.values());
        syncMenuToInventory(list, inventory);
    }
}
