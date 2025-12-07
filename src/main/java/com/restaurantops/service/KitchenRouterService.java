package com.restaurantops.service;

import com.restaurantops.kitchen.stations.ColdBeverageStation;
import com.restaurantops.kitchen.stations.GrillStation;
import com.restaurantops.kitchen.stations.DessertStation;
import com.restaurantops.kitchen.stations.HotBeverageStation;
import com.restaurantops.kitchen.KitchenStation;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.model.OrderCategory;
import com.restaurantops.tracking.OrderTracker;
import com.restaurantops.util.LoggerService;
import com.restaurantops.inventory.InventoryService;
import com.restaurantops.billing.BillingService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KitchenRouterService {

    private final Map<OrderCategory, KitchenStation> stations = new HashMap<>();
    private final HotBeverageStation hotBeverage;
    private final ColdBeverageStation coldBeverage;
    private final LoggerService logger;

    public KitchenRouterService(InventoryService inventoryService,
                                BillingService billingService,
                                OrderTracker tracker,
                                LoggerService logger) {
        this.logger = logger;

        GrillStation grill = new GrillStation(inventoryService, billingService, tracker, logger, 2);
        DessertStation dessert = new DessertStation(inventoryService, billingService, tracker, logger, 1);
        hotBeverage = new HotBeverageStation(inventoryService, billingService, tracker, logger, 2);
        coldBeverage = new ColdBeverageStation(inventoryService, billingService, tracker, logger, 2);

        stations.put(OrderCategory.GRILL, grill);
        stations.put(OrderCategory.DESSERT, dessert);
        stations.put(OrderCategory.BEVERAGE, hotBeverage);
    }

    public void startAllStations() {
        stations.values().forEach(KitchenStation::start);
        hotBeverage.start();
        coldBeverage.start();
    }

    public void stopAllStations() {
        stations.values().forEach(KitchenStation::stop);
        hotBeverage.stop();
        coldBeverage.stop();
    }

    public OrderCategory categoryFor(MenuItem item) {
        String n = item.getName().toLowerCase();
        if (n.contains("pizza") || n.contains("burger") || n.contains("pasta") || n.contains("tikka") || n.contains("fish"))
            return OrderCategory.GRILL;

        if (n.contains("cake") || n.contains("dessert") || n.contains("pancake"))
            return OrderCategory.DESSERT;

        if (n.contains("tea") || n.contains("coffee") || n.contains("hot chocolate"))
            return OrderCategory.BEVERAGE;

        if (n.contains("iced") || n.contains("smoothie") || n.contains("lemonade") || n.contains("juice") || n.contains("cold"))
            return OrderCategory.BEVERAGE;

        return OrderCategory.UNKNOWN;
    }

    public void route(Order order) {
        OrderCategory cat = order.getCategory();
        if (cat == null || cat == OrderCategory.UNKNOWN) {
            cat = categoryFor(order.getItem());
            order.setCategory(cat);
        }

        KitchenStation station = stations.getOrDefault(cat, stations.get(OrderCategory.GRILL));

        if (cat == OrderCategory.BEVERAGE) {
            String n = order.getItem().getName().toLowerCase();

            if (n.contains("iced") || n.contains("smoothie") || n.contains("lemonade") || n.contains("juice") || n.contains("cold"))
                station = coldBeverage;
            else
                station = hotBeverage;
        }

        logger.log("[ROUTER] Routed Order#" + order.getOrderId() + " -> " + station.getName());
        station.acceptOrder(order);
    }

    public Map<OrderCategory, KitchenStation> getStations() {
        return Collections.unmodifiableMap(stations);
    }
}
