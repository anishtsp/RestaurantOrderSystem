package com.restaurantops.kitchen;

import com.restaurantops.model.Order;
import com.restaurantops.service.BillingService;
import com.restaurantops.service.InventoryService;
import com.restaurantops.tracking.OrderTracker;

public class GrillStation extends AbstractKitchenStation {

    public GrillStation(InventoryService inventoryService,
                        BillingService billingService,
                        OrderTracker orderTracker) {
        super(inventoryService, billingService, orderTracker);
    }

    @Override
    protected void processOrder(Order order) throws InterruptedException {
        long millis = 1000L + (order.getQuantity() * 700L);
        System.out.println("[GrillStation] Cooking " + order + " for " + millis + "ms");
        Thread.sleep(millis);
    }

    @Override
    public String getName() {
        return "GrillStation";
    }
}
