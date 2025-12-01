package com.restaurantops.kitchen;

import com.restaurantops.model.Order;
import com.restaurantops.service.BillingService;
import com.restaurantops.service.InventoryService;
import com.restaurantops.service.LoggerService;
import com.restaurantops.tracking.OrderTracker;

public class GrillStation extends AbstractKitchenStation {

    public GrillStation(InventoryService inventoryService,
                        BillingService billingService,
                        OrderTracker tracker,
                        LoggerService logger) {
        super(inventoryService, billingService, tracker, logger);
    }

    @Override
    protected void processOrder(Order order) throws InterruptedException {
        long millis = 1000L + order.getQuantity() * 700L;
        logger.log("[GrillStation] Processing Order#" + order.getOrderId());
        Thread.sleep(millis);
    }

    @Override
    public String getName() {
        return "GrillStation";
    }
}
