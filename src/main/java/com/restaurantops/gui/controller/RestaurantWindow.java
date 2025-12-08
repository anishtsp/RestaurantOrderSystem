package com.restaurantops.gui.controller;

import com.formdev.flatlaf.FlatLightLaf;
import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.gui.panels.*;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class RestaurantWindow extends JFrame {

    private final RestaurantEngine engine;

    private final JPanel sidebar;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    public RestaurantWindow() {
        // Ensure FlatLaf is set (safe to call multiple times)
        FlatLightLaf.setup();

        this.engine = RestaurantEngine.getInstance();

        setTitle("Restaurant Management — Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar
        sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // Content area (CardLayout)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        add(contentPanel, BorderLayout.CENTER);

// Delay panel creation until AFTER the frame shows
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.invokeLater(this::initContentPanels);
            switchTo("Dashboard");
        });

    }

    private JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(0, 1, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p.setPreferredSize(new Dimension(220, getHeight()));

        addNavButton(p, "Dashboard");
        addNavButton(p, "Kitchen & Staff");
        addNavButton(p, "Inventory");
        addNavButton(p, "Billing");
        addNavButton(p, "Menu");
        addNavButton(p, "Orders");
        addNavButton(p, "Reservations");
        addNavButton(p, "Tables & Waiters");
        addNavButton(p, "Diagnostics");

        return p;
    }

    private void addNavButton(JPanel container, String name) {
        JButton b = new JButton(name);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.addActionListener(e -> switchTo(name));
        b.setBackground(null);
        container.add(b);
        navButtons.put(name, b);
    }

    private void initContentPanels() {
        try {
            addCard("Dashboard", new RestaurantDashboardPanel());
            addCard("Kitchen & Staff", new KitchenStaffPanel());
            addCard("Inventory", new InventoryPanel());
            addCard("Billing", new BillingPanel());
            addCard("Menu", new MenuPanel());
            addCard("Orders", new OrdersPanel());
            addCard("Reservations", new ReservationsPanel());
            addCard("Tables & Waiters", new TablesWaitersPanel());
            addCard("Diagnostics", new DiagnosticsPanel());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Panel crashed: " + ex.getMessage());
        }
    }

    private void addCard(String name, JPanel panel) {
        String cardId = name.replace(" ", "_");
        contentPanel.add(panel, cardId);
    }

    private void switchTo(String name) {
        // highlight nav
        navButtons.forEach((k, btn) -> {
            if (k.equals(name)) {
                btn.setBackground(new Color(220, 230, 250));
            } else {
                btn.setBackground(null);
            }
        });

        String cardId = name.replace(" ", "_");
        cardLayout.show(contentPanel, cardId);

        // If panel exposes a refreshStatistics/refreshTables style method, try calling it:
        Component c = getCurrentCardComponent();
        try {
            // call common refresh method if exists (optional)
            c.getClass().getMethod("refreshStatistics").invoke(c);
        } catch (Exception ignored) {}
        try {
            c.getClass().getMethod("refreshTables").invoke(c);
        } catch (Exception ignored) {}
        try {
            c.getClass().getMethod("refreshAll").invoke(c);
        } catch (Exception ignored) {}
    }

    private Component getCurrentCardComponent() {
        for (Component comp : contentPanel.getComponents()) {
            if (comp.isVisible()) return comp;
        }
        return null;
    }

    // Convenience main for quick local run
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RestaurantWindow w = new RestaurantWindow();
            w.setVisible(true);
        });
    }
}
