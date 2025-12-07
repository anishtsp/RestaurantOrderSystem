package com.restaurantops.kitchen.stations;

import com.restaurantops.kitchen.AbstractKitchenStation;
import com.restaurantops.model.Order;
import com.restaurantops.tracking.OrderTracker;
import com.restaurantops.util.LoggerService;
import com.restaurantops.inventory.InventoryService;
import com.restaurantops.billing.BillingService;

public class HotBeverageStation extends AbstractKitchenStation {

    public HotBeverageStation(InventoryService inventoryService,
                              BillingService billingService,
                              OrderTracker orderTracker,
                              LoggerService logger,
                              int workerCount) {
        super(inventoryService, billingService, orderTracker, logger, workerCount);
    }

    @Override
    protected void processOrder(Order order) throws InterruptedException {
        Thread.sleep(800);
    }

    @Override
    public String getName() {
        return "HotBeverageStation";
    }
}
