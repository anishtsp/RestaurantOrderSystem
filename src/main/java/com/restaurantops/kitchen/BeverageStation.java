package com.restaurantops.kitchen;

import com.restaurantops.model.Order;
import com.restaurantops.service.BillingService;
import com.restaurantops.service.InventoryService;
import com.restaurantops.tracking.OrderTracker;

public class BeverageStation extends AbstractKitchenStation {

    public BeverageStation(InventoryService inventoryService,
                           BillingService billingService,
                           OrderTracker orderTracker) {
        super(inventoryService, billingService, orderTracker);
    }

    @Override
    protected void processOrder(Order order) throws InterruptedException {
        long millis = 500L + (order.getQuantity() * 200L);
        System.out.println("[BeverageStation] Making " + order + " for " + millis + "ms");
        Thread.sleep(millis);
    }

    @Override
    public String getName() {
        return "BeverageStation";
    }
}
