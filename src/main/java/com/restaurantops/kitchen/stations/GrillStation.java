package com.restaurantops.kitchen.stations;

import com.restaurantops.billing.BillingService;
import com.restaurantops.inventory.InventoryService;
import com.restaurantops.kitchen.AbstractKitchenStation;
import com.restaurantops.model.Order;
import com.restaurantops.tracking.OrderTracker;
import com.restaurantops.util.LoggerService;

public class GrillStation extends AbstractKitchenStation {

    public GrillStation(InventoryService inv,
                        BillingService billing,
                        OrderTracker tracker,
                        LoggerService logger) {
        super(inv, billing, tracker, logger);
    }

    @Override
    protected void processOrder(Order order) throws InterruptedException {
        Thread.sleep(1500);  // simulate grill cooking
    }

    @Override
    public String getName() {
        return "GrillStation";
    }
}
