package com.restaurantops.kitchen.stations;

import com.restaurantops.kitchen.AbstractKitchenStation;
import com.restaurantops.model.Order;
import com.restaurantops.billing.BillingService;
import com.restaurantops.inventory.InventoryService;
import com.restaurantops.util.LoggerService;
import com.restaurantops.tracking.OrderTracker;

public class DessertStation extends AbstractKitchenStation {

    public DessertStation(InventoryService inventoryService,
                          BillingService billingService,
                          OrderTracker tracker,
                          LoggerService logger) {
        super(inventoryService, billingService, tracker, logger);
    }

    @Override
    protected void processOrder(Order order) throws InterruptedException {
        long millis = 800L + order.getQuantity() * 400L;
        logger.log("[DessertStation] Processing Order#" + order.getOrderId());
        Thread.sleep(millis);
    }

    @Override
    public String getName() {
        return "DessertStation";
    }
}
