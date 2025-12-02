package com.restaurantops.inventory;

import com.restaurantops.model.InventoryItem;
import com.restaurantops.util.LoggerService;
import com.restaurantops.service.SupplierService;

import java.util.Map;

public class InventoryMonitorThread implements Runnable {

    private final InventoryService inventoryService;
    private final SupplierService supplierService;
    private final LoggerService logger;

    public InventoryMonitorThread(InventoryService inventoryService,
                                  SupplierService supplierService,
                                  LoggerService logger) {
        this.inventoryService = inventoryService;
        this.supplierService = supplierService;
        this.logger = logger;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                inventoryService.refreshExpiries();

                Map<String, InventoryItem> low = inventoryService.getLowStockItems();
                if (!low.isEmpty()) {
                    logger.log("[INVENTORY] Low stock detected: " + low.keySet());
                    low.forEach((name, item) -> supplierService.placeOrder(name, inventoryService.getReorderQuantity(name)));
                }

                Thread.sleep(3000);

            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
