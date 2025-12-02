package com.restaurantops.service;

import com.restaurantops.model.SupplierOrder;

import java.util.concurrent.DelayQueue;

public class SupplierService {

    private final DelayQueue<SupplierOrder> queue = new DelayQueue<>();
    private final LoggerService logger;

    public SupplierService(LoggerService logger) {
        this.logger = logger;
    }

    public void placeOrder(String ingredient, int quantity) {
        long delay = 2000 + (long) (Math.random() * 3000);
        SupplierOrder order = new SupplierOrder(ingredient, quantity, delay);
        queue.put(order);
        logger.log("[SUPPLIER] Order placed: " + quantity + " x " + ingredient + " (arrives in " + delay + "ms)");
    }

    public DelayQueue<SupplierOrder> getQueue() {
        return queue;
    }
}
