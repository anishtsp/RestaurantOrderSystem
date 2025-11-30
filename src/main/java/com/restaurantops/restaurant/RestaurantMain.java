package com.restaurantops.restaurant;

import com.restaurantops.core.RestaurantEngine;

public class RestaurantMain {
    public static void main(String[] args) {
        RestaurantEngine engine = RestaurantEngine.getInstance();
        engine.start(); // ensure backend is running

        RestaurantView view = new RestaurantView(engine);
        view.run();

        // After restaurant view decides to shut down:
        engine.stop();
    }
}
