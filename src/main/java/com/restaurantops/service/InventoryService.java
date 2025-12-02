package com.restaurantops.service;

import com.restaurantops.model.InventoryItem;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.model.Recipe;

import java.util.HashMap;
import java.util.Map;

public class InventoryService {

    private final Map<String, InventoryItem> inventory = new HashMap<>();

    private final Map<String, Integer> reorderThresholds = new HashMap<>();

    private final RecipeService recipeService;
    private final LoggerService logger;

    public InventoryService(RecipeService recipeService, LoggerService logger) {
        this.recipeService = recipeService;
        this.logger = logger;
    }


    private long existingExpiry(InventoryItem item) {
        try {
            var field = InventoryItem.class.getDeclaredField("expiryTimestamp");
            field.setAccessible(true);
            return field.getLong(item);
        } catch (Exception e) {
            return 0;
        }
    }


    public synchronized void addItem(String name, int qty, long expiryMillis) {
        String key = name.toLowerCase();
        InventoryItem existing = inventory.get(key);

        if (existing == null) {
            inventory.put(key, new InventoryItem(name, qty, expiryMillis));
        } else {
            existing.increase(qty);
            if (expiryMillis > existingExpiry(existing)) {
                existing.setExpiry(expiryMillis);
            }
        }
    }

    public synchronized void restock(String name, int qty, long newExpiryMillis) {
        String key = name.toLowerCase();
        InventoryItem item = inventory.get(key);

        if (item == null) {
            inventory.put(key, new InventoryItem(name, qty, newExpiryMillis));
        } else {
            item.increase(qty);
            item.setExpiry(newExpiryMillis);
        }

        logger.log("[INVENTORY] Restocked " + qty + " x " + name);
    }


    public synchronized boolean reserveIngredients(Order order) {
        if (order == null || order.getItem() == null) return false;

        MenuItem item = order.getItem();
        String dish = item.getName().toLowerCase();

        Recipe recipe = recipeService.getRecipeForDish(dish);
        if (recipe == null) {
            return reserveSingleItem(dish, order.getQuantity());
        } else {
            return reserveRecipe(recipe, order.getQuantity());
        }
    }

    private boolean reserveSingleItem(String key, int qty) {
        InventoryItem inv = inventory.get(key);

        if (inv == null) return false;
        if (inv.isExpired()) return false;

        synchronized (inv) {
            if (inv.getQuantity() < qty) return false;
            inv.reduce(qty);
            logger.log("[INVENTORY] Reserved " + qty + " x " + key);
            return true;
        }
    }

    public synchronized boolean reserveRecipe(Recipe recipe, int servings) {
        Map<String, Integer> needed = new HashMap<>();

        for (Map.Entry<String, Integer> e : recipe.getIngredients().entrySet()) {
            String key = e.getKey().toLowerCase();
            int perServing = e.getValue() == null ? 0 : e.getValue();
            int totalNeeded = perServing * Math.max(1, servings);

            InventoryItem inv = inventory.get(key);
            if (inv == null) {
                logger.log("[INVENTORY] Missing ingredient " + key + " for " + recipe.getDishName());
                return false;
            }
            if (inv.isExpired()) {
                logger.log("[INVENTORY] Ingredient expired: " + key);
                return false;
            }
            if (inv.getQuantity() < totalNeeded) {
                logger.log("[INVENTORY] Not enough " + key + " for " + recipe.getDishName());
                return false;
            }
            needed.put(key, totalNeeded);
        }

        for (Map.Entry<String, Integer> e : needed.entrySet()) {
            String k = e.getKey();
            int q = e.getValue();
            InventoryItem inv = inventory.get(k);
            synchronized (inv) {
                inv.reduce(q);
            }
            logger.log("[INVENTORY] Reserved " + q + " x " + k + " for " + recipe.getDishName());
        }

        return true;
    }


    public void refreshExpiries() {
        inventory.values().removeIf(InventoryItem::isExpired);
    }


    public synchronized void setReorderThreshold(String ingredient, int threshold) {
        reorderThresholds.put(ingredient.toLowerCase(), threshold);
    }

    public synchronized int getThresholdFor(String ingredient) {
        return reorderThresholds.getOrDefault(ingredient.toLowerCase(), 5); // default threshold = 5
    }

    public synchronized Map<String, InventoryItem> getLowStockItems() {
        Map<String, InventoryItem> low = new HashMap<>();

        for (Map.Entry<String, InventoryItem> e : inventory.entrySet()) {
            String key = e.getKey();
            InventoryItem item = e.getValue();

            int threshold = getThresholdFor(key);
            if (item.getQuantity() <= threshold) {
                low.put(key, item);
            }
        }
        return low;
    }


    public void printInventory() {
        for (InventoryItem i : inventory.values()) {
            System.out.println(i);
        }
    }

    public Map<String, InventoryItem> getInventory() {
        return inventory;
    }
}
