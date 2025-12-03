package com.restaurantops.inventory;

import com.restaurantops.model.InventoryItem;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.model.Recipe;
import com.restaurantops.service.RecipeService;
import com.restaurantops.util.LoggerService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe inventory service responsible for:
 * - Reserving ingredients
 * - Removing expired stock
 * - Reporting low-stock conditions
 * - Restocking and updating expiries
 */
public class InventoryService {

    private static final int DEFAULT_THRESHOLD = 5;
    private static final int DEFAULT_REORDER_QTY = 20;

    private final Map<String, InventoryItem> inventory = new HashMap<>();
    private final Map<String, Integer> reorderThresholds = new HashMap<>();
    private final Map<String, Integer> reorderQuantities = new HashMap<>();

    private final RecipeService recipeService;
    private final LoggerService logger;

    public InventoryService(RecipeService recipeService, LoggerService logger) {
        this.recipeService = recipeService;
        this.logger = logger;
    }

    // Normalize ingredient names
    private String key(String name) {
        return name.toLowerCase().trim();
    }

    // ----------------------------
    //  ADD / RESTOCK
    // ----------------------------

    public synchronized void addItem(String name, int qty, long expiryMillis) {
        String k = key(name);
        InventoryItem item = inventory.get(k);

        if (item == null) {
            inventory.put(k, new InventoryItem(name, qty, expiryMillis));
        } else {
            item.increase(qty);
            if (expiryMillis > item.getExpiryTimestamp()) {
                item.setExpiry(expiryMillis);
            }
        }
    }

    public synchronized void restock(String name, int qty, long newExpiryMillis) {
        String k = key(name);
        InventoryItem existing = inventory.get(k);

        if (existing == null) {
            inventory.put(k, new InventoryItem(name, qty, newExpiryMillis));
        } else {
            existing.increase(qty);
            existing.setExpiry(newExpiryMillis);
        }

        logger.log("[INVENTORY] Restocked " + qty + " x " + name);
    }

    // ----------------------------
    //  RESERVATION LOGIC
    // ----------------------------

    public synchronized boolean reserveIngredients(Order order) {
        if (order == null || order.getItem() == null) return false;

        MenuItem item = order.getItem();
        Recipe recipe = recipeService.getRecipeForDish(key(item.getName()));

        if (recipe == null) {
            return reserveSingleItem(key(item.getName()), order.getQuantity());
        }

        return reserveRecipe(recipe, order.getQuantity());
    }

    private boolean reserveSingleItem(String key, int qty) {
        InventoryItem inv = inventory.get(key);
        if (inv == null || inv.isExpired()) return false;

        synchronized (inv) {
            if (inv.getQuantity() < qty) return false;
            inv.reduce(qty);
        }

        logger.log("[INVENTORY] Reserved " + qty + " x " + key);
        return true;
    }

    /**
     * Recipe-based reservation (atomic):
     * 1. Verify all ingredients
     * 2. Deduct all ingredients together
     */
    public synchronized boolean reserveRecipe(Recipe recipe, int servings) {
        Map<String, Integer> needed = new HashMap<>();

        // Phase 1 — Verify availability
        for (Map.Entry<String, Integer> e : recipe.getIngredients().entrySet()) {
            String ing = key(e.getKey());
            int perServing = e.getValue();
            int totalNeeded = perServing * Math.max(1, servings);

            InventoryItem inv = inventory.get(ing);
            if (inv == null) {
                logger.log("[INVENTORY] Missing ingredient: " + ing);
                return false;
            }
            if (inv.isExpired()) {
                logger.log("[INVENTORY] Ingredient expired: " + ing);
                return false;
            }
            if (inv.getQuantity() < totalNeeded) {
                logger.log("[INVENTORY] Not enough " + ing + " for " + recipe.getDishName());
                return false;
            }

            needed.put(ing, totalNeeded);
        }

        // Phase 2 — Deduct all ingredients
        for (Map.Entry<String, Integer> e : needed.entrySet()) {
            String ing = e.getKey();
            int qty = e.getValue();

            InventoryItem inv = inventory.get(ing);
            synchronized (inv) {
                inv.reduce(qty);
            }
            logger.log("[INVENTORY] Reserved " + qty + " x " + ing + " for " + recipe.getDishName());
        }

        return true;
    }

    // ----------------------------
    //  EXPIRY MANAGEMENT
    // ----------------------------

    public synchronized void refreshExpiries() {
        inventory.values().removeIf(InventoryItem::isExpired);
    }

    // ----------------------------
    //  LOW STOCK + REORDER RULES
    // ----------------------------

    public synchronized void setReorderThreshold(String ingredient, int threshold) {
        reorderThresholds.put(key(ingredient), threshold);
    }

    public synchronized int getThresholdFor(String ingredient) {
        return reorderThresholds.getOrDefault(key(ingredient), DEFAULT_THRESHOLD);
    }

    public synchronized void setReorderQuantity(String ingredient, int qty) {
        reorderQuantities.put(key(ingredient), qty);
    }

    public synchronized int getReorderQuantity(String ingredient) {
        return reorderQuantities.getOrDefault(key(ingredient), DEFAULT_REORDER_QTY);
    }

    public synchronized Map<String, InventoryItem> getLowStockItems() {
        Map<String, InventoryItem> low = new HashMap<>();

        for (Map.Entry<String, InventoryItem> e : inventory.entrySet()) {
            String ing = e.getKey();
            InventoryItem item = e.getValue();
            if (item.getQuantity() <= getThresholdFor(ing)) {
                low.put(ing, item);
            }
        }
        return low;
    }

    // ----------------------------
    //  UTILITIES
    // ----------------------------

    public void printInventory() {
        inventory.values().forEach(System.out::println);
    }

    public Map<String, InventoryItem> getInventory() {
        return Collections.unmodifiableMap(inventory);
    }
}
