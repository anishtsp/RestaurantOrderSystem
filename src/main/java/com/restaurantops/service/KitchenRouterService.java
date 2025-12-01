package com.restaurantops.service;

import com.restaurantops.kitchen.*;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.model.OrderCategory;
import com.restaurantops.tracking.OrderTracker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KitchenRouterService {

    private final Map<OrderCategory, KitchenStation> stations = new HashMap<>();

    public KitchenRouterService(InventoryService inventoryService,
                                BillingService billingService,
                                OrderTracker tracker) {
        GrillStation grill = new GrillStation(inventoryService, billingService, tracker);
        DessertStation dessert = new DessertStation(inventoryService, billingService, tracker);
        BeverageStation beverage = new BeverageStation(inventoryService, billingService, tracker);

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
        else if (name.contains("lemonade") || name.contains("juice") || name.contains("water")) return OrderCategory.BEVERAGE;
        else if (name.contains("gulab") || name.contains("dessert") || name.contains("cake")) return OrderCategory.DESSERT;
        else return OrderCategory.UNKNOWN;
    }

    public void route(Order order) {
        OrderCategory cat = order.getCategory();
        if (cat == null || cat == OrderCategory.UNKNOWN) {
            cat = categoryFor(order.getItem());
            order.setCategory(cat);
        }
        KitchenStation station = stations.getOrDefault(cat, stations.get(OrderCategory.GRILL));
        station.acceptOrder(order);
    }

    public Map<OrderCategory, KitchenStation> getStations() {
        return Collections.unmodifiableMap(stations);
    }
}
