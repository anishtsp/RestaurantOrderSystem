package com.restaurantops.service;

import com.restaurantops.model.Order;

import java.util.Comparator;

public class OrderPriorityComparator implements Comparator<Order> {

    @Override
    public int compare(Order a, Order b) {
        int qCompare = Integer.compare(b.getQuantity(), a.getQuantity());
        if (qCompare != 0) return qCompare;
        return a.getTimestamp().compareTo(b.getTimestamp());
    }
}
