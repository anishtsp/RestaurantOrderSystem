package com.restaurantops.customer;

import com.restaurantops.core.RestaurantEngine;

public class CustomerMain {
    public static void main(String[] args) {
        RestaurantEngine engine = RestaurantEngine.getInstance();
        engine.start(); // safe: repeated calls are protected
        CustomerView view = new CustomerView(engine);
        view.run();
    }
}
