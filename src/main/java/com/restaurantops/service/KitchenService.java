package com.restaurantops.service;

import com.restaurantops.model.Order;
import com.restaurantops.model.OrderStatus;

import java.util.concurrent.BlockingQueue;

public class KitchenService {

    private final BlockingQueue<Order> orderQueue;
    private final InventoryService inventoryService;
    private final BillingService billingService;

    public KitchenService(BlockingQueue<Order> orderQueue,
                          InventoryService inventoryService,
                          BillingService billingService) {
        this.orderQueue = orderQueue;
        this.inventoryService = inventoryService;
        this.billingService = billingService;
    }

    public Order takeOrder() throws InterruptedException {
        return orderQueue.take();
    }

    public void prepareOrder(Order order) throws InterruptedException {
        System.out.println("   [KITCHEN] Received: " + order);

        if (!inventoryService.reserveIngredients(order)) {
            order.setStatus(OrderStatus.REJECTED);
            System.out.println("   [KITCHEN] Rejected (no stock): " + order);
            return;
        }

        order.setStatus(OrderStatus.IN_PROGRESS);
        System.out.println("   [KITCHEN] Preparing: " + order);

        Thread.sleep(1500); // Simulate cooking

        order.setStatus(OrderStatus.COMPLETED);
        System.out.println("   [KITCHEN] Completed: " + order);

        billingService.addItemToBill(order.getTableNumber(), order.getItem(), order.getQuantity());
    }
}
