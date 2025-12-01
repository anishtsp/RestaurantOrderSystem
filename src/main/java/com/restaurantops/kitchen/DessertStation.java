package com.restaurantops.kitchen;

import com.restaurantops.model.Order;
import com.restaurantops.service.BillingService;
import com.restaurantops.service.InventoryService;
import com.restaurantops.tracking.OrderTracker;

public class DessertStation extends AbstractKitchenStation {

    public DessertStation(InventoryService inventoryService,
                          BillingService billingService,
                          OrderTracker orderTracker) {
        super(inventoryService, billingService, orderTracker);
    }

    @Override
    protected void processOrder(Order order) throws InterruptedException {
        long millis = 800L + (order.getQuantity() * 400L);
        System.out.println("[DessertStation] Preparing " + order + " for " + millis + "ms");
        Thread.sleep(millis);
    }

    @Override
    public String getName() {
        return "DessertStation";
    }
}
