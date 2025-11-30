package com.restaurantops.thread;

import com.restaurantops.model.Order;
import com.restaurantops.service.KitchenService;

public class KitchenWorkerThread implements Runnable {

    private final KitchenService kitchenService;

    public KitchenWorkerThread(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Order order = kitchenService.takeOrder();
                kitchenService.prepareOrder(order);
            }
        } catch (InterruptedException ignored) {}
    }
}
