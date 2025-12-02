package com.restaurantops.inventory;

import com.restaurantops.model.SupplierOrder;
import com.restaurantops.util.LoggerService;
import com.restaurantops.service.SupplierService;

public class DeliveryWorkerThread implements Runnable {

    private final SupplierService supplierService;
    private final InventoryService inventoryService;
    private final LoggerService logger;

    public DeliveryWorkerThread(SupplierService supplierService,
                                InventoryService inventoryService,
                                LoggerService logger) {
        this.supplierService = supplierService;
        this.inventoryService = inventoryService;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                SupplierOrder order = supplierService.getQueue().take();

                inventoryService.restock(
                        order.getIngredient(),
                        order.getQuantity(),
                        System.currentTimeMillis() + 86400000
                );

                logger.log("[DELIVERY] Arrived: " +
                        order.getQuantity() + " x " + order.getIngredient());
            }
        } catch (InterruptedException ignored) { }
    }
}
