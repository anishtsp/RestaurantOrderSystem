package com.restaurantops.tracking;

import com.restaurantops.model.Order;

public interface OrderListener {
    void onOrderUpdated(Order order);
}
