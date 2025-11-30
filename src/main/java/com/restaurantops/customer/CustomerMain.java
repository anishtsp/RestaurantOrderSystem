package com.restaurantops.customer;

import com.restaurantops.core.RestaurantEngine;

public class CustomerMain {
    public static void main(String[] args) {
        RestaurantEngine engine = RestaurantEngine.getInstance();
        engine.start(); // ensure backend is running

        CustomerView view = new CustomerView(engine);
        view.run();
        // Do NOT stop engine here if restaurant view may still be running
    }
}
