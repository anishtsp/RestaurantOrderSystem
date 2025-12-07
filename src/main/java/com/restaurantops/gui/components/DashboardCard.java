package com.restaurantops.gui.components;

import javax.swing.*;
import java.awt.*;

public class DashboardCard extends JPanel {

    private JLabel titleLabel;
    private JLabel valueLabel;
    private JLabel iconLabel;

    public DashboardCard(String title, String iconEmoji) {

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 245, 245));
        setPreferredSize(new Dimension(250, 130));

        // Rounded edges
        setOpaque(false);

        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        valueLabel = new JLabel("0");
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));

        iconLabel = new JLabel(iconEmoji);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 40));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(iconLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Draw rounded card background
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
        super.paintComponent(g);
    }
}
