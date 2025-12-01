package com.restaurantops.service;

import com.restaurantops.kitchen.BeverageStation;
import com.restaurantops.kitchen.DessertStation;
import com.restaurantops.kitchen.GrillStation;
import com.restaurantops.kitchen.KitchenStation;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.model.OrderCategory;
import com.restaurantops.tracking.OrderTracker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KitchenRouterService {

    private final Map<OrderCategory, KitchenStation> stations = new HashMap<>();
    private final LoggerService logger;

    public KitchenRouterService(InventoryService inventoryService,
                                BillingService billingService,
                                OrderTracker tracker,
                                LoggerService logger) {
        this.logger = logger;

        GrillStation grill = new GrillStation(inventoryService, billingService, tracker, logger);
        DessertStation dessert = new DessertStation(inventoryService, billingService, tracker, logger);
        BeverageStation beverage = new BeverageStation(inventoryService, billingService, tracker, logger);

        stations.put(OrderCategory.GRILL, grill);
        stations.put(OrderCategory.DESSERT, dessert);
        stations.put(OrderCategory.BEVERAGE, beverage);
    }

    public void startAllStations() {
        stations.values().forEach(KitchenStation::start);
    }

    public void stopAllStations() {
        stations.values().forEach(KitchenStation::stop);
    }

    public OrderCategory categoryFor(MenuItem item) {
        String name = item.getName().toLowerCase();
        if (name.contains("pizza") || name.contains("burger") || name.contains("pasta")) return OrderCategory.GRILL;
        if (name.contains("lemonade") || name.contains("juice") || name.contains("water")) return OrderCategory.BEVERAGE;
        if (name.contains("cake") || name.contains("dessert") || name.contains("gulab")) return OrderCategory.DESSERT;
        return OrderCategory.UNKNOWN;
    }

    public void route(Order order) {
        OrderCategory cat = order.getCategory();
        if (cat == null || cat == OrderCategory.UNKNOWN) {
            cat = categoryFor(order.getItem());
            order.setCategory(cat);
        }
        KitchenStation station = stations.getOrDefault(cat, stations.get(OrderCategory.GRILL));
        logger.log("[ROUTER] Routed Order#" + order.getOrderId() + " -> " + station.getName());
        station.acceptOrder(order);
    }

    public Map<OrderCategory, KitchenStation> getStations() {
        return Collections.unmodifiableMap(stations);
    }
}
