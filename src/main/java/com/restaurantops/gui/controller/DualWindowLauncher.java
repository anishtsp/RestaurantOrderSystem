package com.restaurantops.gui.controller;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;

public class DualWindowLauncher {

    public static void main(String[] args) {

        // Apply modern UI look
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {

            CustomerWindow customer = new CustomerWindow();
            customer.setVisible(true);

            RestaurantWindow restaurant = new RestaurantWindow();
            restaurant.setVisible(true);

        });
    }
}
