package com.restaurantops.kitchen.stations;

import com.restaurantops.inventory.InventoryService;
import com.restaurantops.billing.BillingService;
import com.restaurantops.kitchen.AbstractKitchenStation;
import com.restaurantops.tracking.OrderTracker;
import com.restaurantops.util.LoggerService;
import com.restaurantops.model.Order;

public class DessertStation extends AbstractKitchenStation {

    public DessertStation(InventoryService inventoryService,
                          BillingService billingService,
                          OrderTracker orderTracker,
                          LoggerService logger,
                          int workerCount) {
        super(inventoryService, billingService, orderTracker, logger, workerCount);
    }

    @Override
    protected void processOrder(Order order) throws InterruptedException {
        Thread.sleep(2000);
    }

    @Override
    public String getName() {
        return "DessertStation";
    }
}
