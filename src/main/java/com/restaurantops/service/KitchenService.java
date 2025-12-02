package com.restaurantops.service;

import com.restaurantops.model.Order;
import com.restaurantops.model.OrderStatus;
import com.restaurantops.tracking.OrderTracker;

import java.util.concurrent.BlockingQueue;

public class KitchenService {

    private final BlockingQueue<Order> orderQueue;
    private final InventoryService inventoryService;
    private final BillingService billingService;
    private final OrderTracker orderTracker;

    public KitchenService(BlockingQueue<Order> orderQueue,
                          InventoryService inventoryService,
                          BillingService billingService,
                          OrderTracker orderTracker) {
        this.orderQueue = orderQueue;
        this.inventoryService = inventoryService;
        this.billingService = billingService;
        this.orderTracker = orderTracker;
    }

    public Order takeOrder() throws InterruptedException {
        return orderQueue.take();
    }

    public void prepareOrder(Order order) throws InterruptedException {
        System.out.println("   [KITCHEN] Received: " + order);
        order.setStatus(OrderStatus.ACCEPTED);
        orderTracker.notifyUpdate(order);

        if (!inventoryService.reserveIngredients(order)) {
            order.setStatus(OrderStatus.REJECTED);
            System.out.println("   [KITCHEN] Rejected (no stock): " + order);
            orderTracker.notifyUpdate(order);
            billingService.addOrderToBill(order);
            return;
        }

        order.setStatus(OrderStatus.IN_PROGRESS);
        orderTracker.notifyUpdate(order);
        System.out.println("   [KITCHEN] Preparing: " + order);

        Thread.sleep(1500); // Simulate cooking

        order.setStatus(OrderStatus.COMPLETED);
        orderTracker.notifyUpdate(order);
        System.out.println("   [KITCHEN] Completed: " + order);

        billingService.addItemToBill(order.getTableNumber(), order.getItem(), order.getQuantity());
    }
}
