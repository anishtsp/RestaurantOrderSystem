package com.restaurantops.gui.panels;

import com.restaurantops.billing.BillingService;
import com.restaurantops.billing.payment.CardPayment;
import com.restaurantops.billing.payment.CashPayment;
import com.restaurantops.billing.payment.UpiPayment;
import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.Bill;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class BillingPanel extends JPanel {

    private final RestaurantEngine engine;
    private final BillingService billingService;

    private JTable billTable;
    private DefaultTableModel billTableModel;

    private JTable billDetailTable;
    private DefaultTableModel billDetailModel;

    private JLabel lblTotal;
    private JLabel lblStatus;

    public BillingPanel() {
        this.engine = RestaurantEngine.getInstance();
        this.billingService = engine.getBillingService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTitleBar(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        refreshBills();
    }

    private JPanel buildTitleBar() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Billing & Payments");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        panel.add(title, BorderLayout.WEST);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshBills());
        panel.add(refresh, BorderLayout.EAST);

        return panel;
    }

    private JSplitPane buildMainContent() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.45);

        /* LEFT: Bills list */
        billTableModel = new DefaultTableModel(
                new Object[]{"Table", "Total", "Status", "Items"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        billTable = new JTable(billTableModel);
        billTable.getSelectionModel().addListSelectionListener(e -> showSelectedBill());

        JScrollPane billScroll = new JScrollPane(billTable);
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("All Bills"), BorderLayout.NORTH);
        left.add(billScroll, BorderLayout.CENTER);

        /* RIGHT: Bill Detail */
        JPanel right = buildBillDetailPanel();

        split.setLeftComponent(left);
        split.setRightComponent(right);

        return split;
    }

    private JPanel buildBillDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("Bill Details");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        panel.add(title, BorderLayout.NORTH);

        billDetailModel = new DefaultTableModel(
                new Object[]{"Item", "Qty", "Unit Price", "Total"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        billDetailTable = new JTable(billDetailModel);

        panel.add(new JScrollPane(billDetailTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());

        lblTotal = new JLabel("Total: ₹0.00");
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));

        lblStatus = new JLabel("Status: -");
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD, 16f));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.add(lblTotal);
        infoPanel.add(lblStatus);

        bottom.add(infoPanel, BorderLayout.NORTH);

        bottom.add(buildPaymentButtons(), BorderLayout.SOUTH);

        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildPaymentButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton cashBtn = new JButton("Pay Cash");
        JButton cardBtn = new JButton("Pay Card");
        JButton upiBtn = new JButton("Pay UPI");

        cashBtn.addActionListener(e -> processPayment(new CashPayment()));
        cardBtn.addActionListener(e -> processPayment(new CardPayment()));
        upiBtn.addActionListener(e -> processPayment(new UpiPayment()));

        p.add(cashBtn);
        p.add(cardBtn);
        p.add(upiBtn);

        return p;
    }

    private void processPayment(Object method) {
        int row = billTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a bill first.");
            return;
        }

        int tableNum = (int) billTable.getValueAt(row, 0);

        billingService.processPayment(tableNum, (com.restaurantops.billing.payment.PaymentMethod) method);

        refreshBills();
    }

    private void refreshBills() {
        billTableModel.setRowCount(0);

        Map<Integer, Bill> bills = billingService.getAllBills();
        for (Map.Entry<Integer, Bill> entry : bills.entrySet()) {
            int table = entry.getKey();
            Bill bill = entry.getValue();

            double total = bill.getTotalAmount();
            boolean paid = bill.isPaid();
            int itemCount = bill.toString().split("\n").length - 3; // quick count of BillLine entries

            billTableModel.addRow(new Object[]{
                    table,
                    String.format("₹%.2f", total),
                    paid ? "PAID" : "UNPAID",
                    itemCount
            });
        }

        clearDetails();
    }

    private void showSelectedBill() {
        int row = billTable.getSelectedRow();
        if (row == -1) {
            clearDetails();
            return;
        }

        int table = (int) billTable.getValueAt(row, 0);
        Bill bill = billingService.getBill(table);
        if (bill == null) {
            clearDetails();
            return;
        }

        billDetailModel.setRowCount(0);

        // Parse Bill.toString()
        String[] lines = bill.toString().split("\n");

        for (String line : lines) {
            if (line.contains(")")) {
                try {
                    String[] parts = line.split("\\) ")[1].split(" x| ₹");
                    String item = parts[0];
                    int qty = Integer.parseInt(parts[1]);
                    double price = Double.parseDouble(parts[2]);
                    double total = qty * price;

                    billDetailModel.addRow(new Object[]{item, qty, price, total});
                } catch (Exception ignored) {}
            }
        }

        lblTotal.setText(String.format("Total: ₹%.2f", bill.getTotalAmount()));
        lblStatus.setText("Status: " + (bill.isPaid() ? "PAID" : "UNPAID"));
        lblStatus.setForeground(bill.isPaid() ? new Color(0, 180, 0) : Color.RED);
    }

    private void clearDetails() {
        billDetailModel.setRowCount(0);
        lblTotal.setText("Total: ₹0.00");
        lblStatus.setText("Status: -");
        lblStatus.setForeground(Color.BLACK);
    }
}
