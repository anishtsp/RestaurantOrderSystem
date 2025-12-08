package com.restaurantops.gui.customer;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.billing.BillingService;
import com.restaurantops.billing.payment.CashPayment;
import com.restaurantops.billing.payment.CardPayment;
import com.restaurantops.model.Bill;

import javax.swing.*;
import java.awt.*;

public class MyBillingPanelTouch extends JPanel {

    private final RestaurantEngine engine;
    private final BillingService billingService;

    private final int tableNumber;
    private JTextArea billArea;

    public MyBillingPanelTouch(int tableNumber) {

        this.engine = RestaurantEngine.getInstance();
        this.billingService = engine.getBillingService();
        this.tableNumber = tableNumber;

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildTitle(), BorderLayout.NORTH);
        add(buildBillPane(), BorderLayout.CENTER);
        add(buildControls(), BorderLayout.SOUTH);

        refreshBill();
    }

    /* ============================
                TITLE
       ============================ */
    private JComponent buildTitle() {
        JLabel title = new JLabel("Your Bill", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        return title;
    }

    /* ============================
                BILL DISPLAY
       ============================ */
    private JComponent buildBillPane() {

        billArea = new JTextArea();
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 20));

        JScrollPane scroll = new JScrollPane(billArea);
        scroll.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        return scroll;
    }

    /* ============================
                BUTTONS
       ============================ */
    private JComponent buildControls() {

        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 20));

        JButton refreshBtn = bigButton("Refresh Bill");
        JButton payCashBtn = bigButton("Pay with Cash");
        JButton payCardBtn = bigButton("Pay with Card");

        refreshBtn.addActionListener(e -> refreshBill());
        payCashBtn.addActionListener(e -> processPayment("cash"));
        payCardBtn.addActionListener(e -> processPayment("card"));

        panel.add(refreshBtn);
        panel.add(payCashBtn);
        panel.add(payCardBtn);

        return panel;
    }

    private JButton bigButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 22));
        b.setBackground(new Color(70, 130, 180));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(200, 80));
        return b;
    }

    /* ============================
            BILL OPERATIONS
       ============================ */

    private void refreshBill() {

        Bill bill = billingService.getBill(tableNumber);

        if (bill == null) {
            billArea.setText("No bill available yet.\n\nPlace an order to begin.");
            return;
        }

        billArea.setText(bill.toString());
    }

    private void processPayment(String method) {

        Bill bill = billingService.getBill(tableNumber);
        if (bill == null) {
            JOptionPane.showMessageDialog(this, "No bill available.");
            return;
        }

        if (bill.isPaid()) {
            JOptionPane.showMessageDialog(this, "Bill already paid!");
            return;
        }

        boolean success;

        if (method.equals("cash")) {
            success = new CashPayment().process(bill.getTotalAmount());
        } else {
            success = new CardPayment().process(bill.getTotalAmount());
        }

        if (success) {
            bill.markPaid();
            JOptionPane.showMessageDialog(this, "Payment successful!");
        } else {
            JOptionPane.showMessageDialog(this, "Payment failed!");
        }

        refreshBill();
    }
}
