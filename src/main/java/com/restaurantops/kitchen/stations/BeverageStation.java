package com.restaurantops.kitchen.stations;

import com.restaurantops.billing.BillingService;
import com.restaurantops.inventory.InventoryService;
import com.restaurantops.kitchen.AbstractKitchenStation;
import com.restaurantops.model.Order;
import com.restaurantops.tracking.OrderTracker;
import com.restaurantops.util.LoggerService;

public class BeverageStation extends AbstractKitchenStation {

    public BeverageStation(InventoryService inv,
                           BillingService billing,
                           OrderTracker tracker,
                           LoggerService logger) {
        super(inv, billing, tracker, logger);
    }

    @Override
    protected void processOrder(Order order) throws InterruptedException {
        Thread.sleep(800);  // beverages are faster
    }

    @Override
    public String getName() {
        return "BeverageStation";
    }
}
