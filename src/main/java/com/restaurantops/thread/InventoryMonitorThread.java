package com.restaurantops.thread;

import com.restaurantops.service.InventoryService;

public class InventoryMonitorThread implements Runnable {

    private final InventoryService inventoryService;

    public InventoryMonitorThread(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(5000);
                inventoryService.restockLowItems();
            }
        } catch (InterruptedException ignored) {}
    }
}
