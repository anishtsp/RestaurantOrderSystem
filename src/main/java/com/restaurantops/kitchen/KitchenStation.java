package com.restaurantops.kitchen;

import com.restaurantops.model.Chef;
import com.restaurantops.model.Order;

public interface KitchenStation {
    String getName();
    void acceptOrder(Order order);
    void start();
    void stop();
    int queueSize();
    boolean isRunning();
    void assignChef(Chef chef);
    Chef getAssignedChef();
}
