package com.restaurantops.gui.controller;

import com.formdev.flatlaf.FlatLightLaf;
import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.gui.panels.RestaurantDashboardPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class RestaurantWindow extends JFrame {

    private final RestaurantEngine engine;

    // Navigation sidebar buttons
    private JButton btnDashboard;
    private JButton btnKitchenStaff;
    private JButton btnInventory;
    private JButton btnBilling;
    private JButton btnOrders;
    private JButton btnReservations;
    private JButton btnTablesWaiters;
    private JButton btnDiagnostics;

    // Card layout for main content
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Mapping button names to card IDs
    private final Map<String, String> cardNames = new HashMap<>();

    public RestaurantWindow() {
        engine = RestaurantEngine.getInstance();

        setTitle("Restaurant Management Dashboard");
        setSize(1100, 700);
        setLocation(650, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        FlatLightLaf.setup(); // already called in launcher

        initSidebar();
        initContentPanels();
    }

    /** -------------------------
     *  LEFT SIDEBAR NAVIGATION
     *  -------------------------
     */
    private void initSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(10, 1, 5, 5));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));

        btnDashboard = createNavButton("Dashboard");
        btnKitchenStaff = createNavButton("Kitchen & Staff");
        btnInventory = createNavButton("Inventory");
        btnBilling = createNavButton("Billing");
        btnOrders = createNavButton("Orders");
        btnReservations = createNavButton("Reservations");
        btnTablesWaiters = createNavButton("Tables & Waiters");
        btnDiagnostics = createNavButton("Diagnostics");

        sidebar.add(btnDashboard);
        sidebar.add(btnKitchenStaff);
        sidebar.add(btnInventory);
        sidebar.add(btnBilling);
        sidebar.add(btnOrders);
        sidebar.add(btnReservations);
        sidebar.add(btnTablesWaiters);
        sidebar.add(btnDiagnostics);

        add(sidebar, BorderLayout.WEST);
    }

    private JButton createNavButton(String name) {
        JButton btn = new JButton(name);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.addActionListener(e -> switchPanel(name));
        return btn;
    }

    /** -------------------------------
     *  MAIN CONTENT (CardLayout setup)
     *  -------------------------------
     */
    private void initContentPanels() {

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Placeholder panels — will be replaced with full modules
        addContentPanel("Dashboard", new RestaurantDashboardPanel());
        addContentPanel("Kitchen & Staff", createPlaceholder("Kitchen & Staff Management"));
        addContentPanel("Inventory", createPlaceholder("Inventory System"));
        addContentPanel("Billing", createPlaceholder("Billing & Payment Processing"));
        addContentPanel("Orders", createPlaceholder("Order Management"));
        addContentPanel("Reservations", createPlaceholder("Reservations Control"));
        addContentPanel("Tables & Waiters", createPlaceholder("Table + Waiter Management"));
        addContentPanel("Diagnostics", createPlaceholder("System Diagnostics"));

        add(contentPanel, BorderLayout.CENTER);
    }

    private void addContentPanel(String name, JPanel panel) {
        String cardId = name.replace(" ", "_");
        contentPanel.add(panel, cardId);
        cardNames.put(name, cardId);
    }

    private JPanel createPlaceholder(String labelText) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 26));
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    /** Switch screen */
    private void switchPanel(String name) {
        cardLayout.show(contentPanel, cardNames.get(name));
    }
}
