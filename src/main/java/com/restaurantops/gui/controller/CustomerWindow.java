package com.restaurantops.gui.controller;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.gui.customer.MenuOrderPanelTouch;
import com.restaurantops.gui.customer.CustomerOrdersPanelTouch;
import com.restaurantops.gui.customer.ReservationPanelTouch;
import com.restaurantops.gui.customer.SeatingPanelTouch;
import com.restaurantops.gui.customer.MyBillingPanelTouch;
import com.restaurantops.gui.utils.NotificationBubble;
import com.restaurantops.model.Order;

import javax.swing.*;
import java.awt.*;

public class CustomerWindow extends JFrame {

    private final int tableNumber;
    private final RestaurantEngine engine;

    private final JLayeredPane layeredPane;
    private final JPanel notificationLayer;

    public CustomerWindow(int tableNumber) {
        this.tableNumber = tableNumber;
        this.engine = RestaurantEngine.getInstance();

        setTitle("Customer Touch Interface — Table " + tableNumber);
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        add(layeredPane);

        // Main UI tabs
        JTabbedPane tabs = buildTabs();
        tabs.setBounds(0, 0, 1100, 720);
        layeredPane.add(tabs, JLayeredPane.DEFAULT_LAYER);

        // Notification overlay
        notificationLayer = new JPanel(null);
        notificationLayer.setOpaque(false);
        notificationLayer.setBounds(0, 0, 1100, 720);
        layeredPane.add(notificationLayer, JLayeredPane.PALETTE_LAYER);

        registerOrderListener();

        setVisible(true);
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 22));

        tabs.addTab("Menu", new MenuOrderPanelTouch(engine));
        tabs.addTab("My Orders", new CustomerOrdersPanelTouch(engine));
        tabs.addTab("Reservations", new ReservationPanelTouch(engine));
        tabs.addTab("Seating", new SeatingPanelTouch(engine));
        tabs.addTab("Billing", new MyBillingPanelTouch(tableNumber));

        return tabs;
    }

    /* -------------------------------
       REAL-TIME ORDER TRACKING
       ------------------------------- */

    private void registerOrderListener() {
        engine.getOrderTracker().addListener(this::handleOrderUpdate);
    }

    private void handleOrderUpdate(Order order) {
        if (order.getTableNumber() != tableNumber) return;

        SwingUtilities.invokeLater(() -> {
            switch (order.getStatus()) {

                case ACCEPTED -> showBubble("Order #" + order.getOrderId() + " Accepted!");

                case IN_PROGRESS -> showBubble("Order #" + order.getOrderId() + " is Being Prepared");

                case COMPLETED -> showBubble("Order Ready! #" + order.getOrderId());

                case REJECTED -> showBubble("Order #" + order.getOrderId() + " Rejected.");
            }
        });
    }

    /* -------------------------------
       NOTIFICATION BUBBLES
       ------------------------------- */

    private void showBubble(String text) {
        NotificationBubble bubble = new NotificationBubble(text);
        bubble.setSize(420, 60);

        int x = (getWidth() - 420) / 2;
        int y = getHeight() - 150;

        bubble.setLocation(x, y);

        notificationLayer.add(bubble);
        notificationLayer.revalidate();
        notificationLayer.repaint();
    }

    /* -------------------------------
       Static launcher
       ------------------------------- */

    public static void launch(int table) {
        RestaurantEngine engine = RestaurantEngine.getInstance();
        engine.start();

        SwingUtilities.invokeLater(() -> new CustomerWindow(table));
    }
}
