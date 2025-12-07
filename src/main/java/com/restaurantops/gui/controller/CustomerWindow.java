package com.restaurantops.gui.controller;

import javax.swing.*;
import java.awt.*;
import com.restaurantops.core.RestaurantEngine;

public class CustomerWindow extends JFrame {

    private RestaurantEngine engine;

    public CustomerWindow() {
        this.engine = RestaurantEngine.getInstance();

        setTitle("Customer View");
        setSize(480, 620);
        setLocation(100, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Modern UI layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Customer View", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22));

        mainPanel.add(title, BorderLayout.NORTH);

        // Placeholder for menu UI (Phase 7)
        JPanel centerPanel = new JPanel();
        centerPanel.add(new JLabel("Menu UI coming in Phase 7"));

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
    }
}
