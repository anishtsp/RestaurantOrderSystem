package com.restaurantops.core;

import com.restaurantops.model.Order;
import com.restaurantops.service.KitchenRouterService;
import com.restaurantops.util.LoggerService;

import java.util.concurrent.PriorityBlockingQueue;

public class DispatchThread implements Runnable {

    private final PriorityBlockingQueue<Order> queue;
    private final KitchenRouterService router;
    private final LoggerService logger;

    public DispatchThread(PriorityBlockingQueue<Order> queue,
                          KitchenRouterService router,
                          LoggerService logger) {

        this.queue = queue;
        this.router = router;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {

                // Blocking until an order is ready
                Order order = queue.take();

                logger.log("[DISPATCH] Order#" + order.getOrderId() + " dispatched");

                // Forward to the correct kitchen station
                router.route(order);
            }

        } catch (InterruptedException ignored) {
            // Normal shutdown
        }
    }
}
