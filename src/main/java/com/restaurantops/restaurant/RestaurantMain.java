package com.restaurantops.restaurant;

import com.restaurantops.core.RestaurantEngine;

public class RestaurantMain {
    public static void main(String[] args) {
        RestaurantEngine engine = RestaurantEngine.getInstance();
        engine.start(); // safe
        RestaurantView view = new RestaurantView(engine);
        view.run();
        engine.stop();
    }
}
