package com.restaurantops.core;

import com.restaurantops.model.Order;
import com.restaurantops.service.KitchenRouterService;

import java.util.concurrent.PriorityBlockingQueue;

public class DispatchThread implements Runnable {

    private final PriorityBlockingQueue<Order> queue;
    private final KitchenRouterService router;

    public DispatchThread(PriorityBlockingQueue<Order> queue, KitchenRouterService router) {
        this.queue = queue;
        this.router = router;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Order order = queue.take();
                router.route(order);
            }
        } catch (InterruptedException ignored) {
        }
    }
}
