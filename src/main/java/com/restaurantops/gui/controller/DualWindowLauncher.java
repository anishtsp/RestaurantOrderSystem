package com.restaurantops.gui.controller;

import com.restaurantops.core.RestaurantEngine;
import javax.swing.*;

public class DualWindowLauncher {

    public static void main(String[] args) {

        RestaurantEngine engine = RestaurantEngine.getInstance();

        // 1) Start engine
        engine.start();

        // 2) Wait until routerService is ready (kitchen stations initialized)
        while (engine.getRouterService() == null) {
            try { Thread.sleep(50); } catch (Exception ignored) {}
        }

        // 3) Wait until tables & waiters are preloaded
        while (engine.getTableService().listTables().isEmpty()
                || engine.getWaiterService().getWaiters().isEmpty()) {
            try { Thread.sleep(50); } catch (Exception ignored) {}
        }

        // 4) Launch RestaurantWindow AFTER engine is 100% ready
        SwingUtilities.invokeLater(() -> new RestaurantWindow().setVisible(true));

        // 5) Launch CustomerWindow
        SwingUtilities.invokeLater(() -> new CustomerWindow(1));
    }
}
