package com.restaurantops.service;

import com.restaurantops.model.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class OrderService {

    private final BlockingQueue<Order> orderQueue;
    private final List<Order> allOrders = Collections.synchronizedList(new ArrayList<>());

    public OrderService(BlockingQueue<Order> orderQueue) {
        this.orderQueue = orderQueue;
    }

    public void placeOrder(Order order) {
        try {
            allOrders.add(order);
            orderQueue.put(order);
            System.out.println("[ORDER] Placed: " + order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public List<Order> getAllOrders() {
        return allOrders;
    }
}
