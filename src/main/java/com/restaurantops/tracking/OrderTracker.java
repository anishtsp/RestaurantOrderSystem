package com.restaurantops.tracking;

import com.restaurantops.model.Order;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrderTracker {

    private final List<OrderListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(OrderListener l) {
        listeners.add(l);
    }

    public void removeListener(OrderListener l) {
        listeners.remove(l);
    }

    public void notifyUpdate(Order order) {
        for (OrderListener l : listeners) {
            l.onOrderUpdated(order);
        }
    }
}
