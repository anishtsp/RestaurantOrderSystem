package com.restaurantops.kitchen.stations;

import com.restaurantops.kitchen.AbstractKitchenStation;
import com.restaurantops.model.Order;
import com.restaurantops.billing.BillingService;
import com.restaurantops.inventory.InventoryService;
import com.restaurantops.util.LoggerService;
import com.restaurantops.tracking.OrderTracker;

public class BeverageStation extends AbstractKitchenStation {

    public BeverageStation(InventoryService inventoryService,
                           BillingService billingService,
                           OrderTracker tracker,
                           LoggerService logger) {
        super(inventoryService, billingService, tracker, logger);
    }

    @Override
    protected void processOrder(Order order) throws InterruptedException {
        long millis = 500L + order.getQuantity() * 200L;
        logger.log("[BeverageStation] Processing Order#" + order.getOrderId());
        Thread.sleep(millis);
    }

    @Override
    public String getName() {
        return "BeverageStation";
    }
}
