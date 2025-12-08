package com.restaurantops.gui.utils;

import javax.swing.*;
import java.awt.*;

public class NotificationBubble extends JPanel {

    private final Timer fadeTimer;
    private float opacity = 1.0f;
    private final String message;

    public NotificationBubble(String message) {
        this.message = message;

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        add(label, BorderLayout.CENTER);

        setBackground(new Color(50, 50, 50, 200));

        // Initialize timer BEFORE using it — always assigned once.
        fadeTimer = new Timer(40, e -> fadeOut());
        fadeTimer.setInitialDelay(2000);  // bubble stays visible for 2 seconds
        fadeTimer.start();
    }

    private void fadeOut() {
        opacity -= 0.05f;
        if (opacity <= 0) {
            opacity = 0;
            fadeTimer.stop();

            Container parent = getParent();
            if (parent != null) {
                parent.remove(this);
                parent.revalidate();
                parent.repaint();
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        g2.dispose();

        super.paintComponent(g);
    }
}
