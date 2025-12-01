package com.restaurantops.service;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public class OrderService {

    private final PriorityBlockingQueue<Order> orderQueue;
    private final List<Order> allOrders = Collections.synchronizedList(new ArrayList<>());

    public OrderService(PriorityBlockingQueue<Order> orderQueue) {
        this.orderQueue = orderQueue;
    }

    public void placeOrder(Order order) {
        try {
            allOrders.add(order);
            orderQueue.put(order);
            System.out.println("[ORDER] Placed: " + order);
            RestaurantEngine.getInstance().notifyNewOrder(order);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Order> getAllOrders() {
        return allOrders;
    }
}
