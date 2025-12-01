package com.restaurantops.thread;

import com.restaurantops.service.InventoryService;
import com.restaurantops.service.LoggerService;

public class InventoryMonitorThread implements Runnable {

    private final InventoryService inventoryService;
    private final LoggerService logger;

    public InventoryMonitorThread(InventoryService inventoryService,
                                  LoggerService logger) {
        this.inventoryService = inventoryService;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(30000);
                inventoryService.refreshExpiries();
                logger.log("[INVENTORY] Expiry check performed");
            }
        } catch (InterruptedException ignored) {
        }
    }
}
