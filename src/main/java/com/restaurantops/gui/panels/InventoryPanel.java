package com.restaurantops.gui.panels;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.inventory.InventoryService;
import com.restaurantops.model.InventoryItem;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Map;

public class InventoryPanel extends JPanel {

    private final RestaurantEngine engine;
    private final InventoryService inventoryService;

    private JTable inventoryTable;
    private DefaultTableModel inventoryModel;

    private JTable lowStockTable;
    private DefaultTableModel lowStockModel;

    private JTextField restockNameField;
    private JSpinner restockQtySpinner;
    private JSpinner restockExpiryMinutesSpinner;

    private final SimpleDateFormat expiryFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public InventoryPanel() {
        this.engine = RestaurantEngine.getInstance();
        this.inventoryService = engine.getInventoryService();

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        initTopToolbar();
        initCenterSplit();

        refreshAll();
    }

    private void initTopToolbar() {
        JPanel top = new JPanel(new BorderLayout(8, 8));

        JLabel title = new JLabel("Inventory Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        top.add(title, BorderLayout.WEST);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh");
        JButton clearExpiredBtn = new JButton("Remove Expired");

        refreshBtn.addActionListener(e -> refreshAll());
        clearExpiredBtn.addActionListener(e -> {
            inventoryService.refreshExpiries();
            JOptionPane.showMessageDialog(this, "Expired items removed (if any).");
            refreshAll();
        });

        rightBtns.add(clearExpiredBtn);
        rightBtns.add(refreshBtn);

        top.add(rightBtns, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
    }

    private void initCenterSplit() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.65);

        /* =====================
              INVENTORY TABLE
           ===================== */

        inventoryModel = new DefaultTableModel(
                new Object[]{"Ingredient", "Quantity", "Expiry", "Threshold", "Reorder Qty"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int col) {
                // Allow editing only on Threshold & Reorder Qty
                return col == 3 || col == 4;
            }
        };

        inventoryTable = new JTable(inventoryModel);
        inventoryTable.putClientProperty("terminateEditOnFocusLost", true);

        // Listener for inline editing
        inventoryModel.addTableModelListener(event -> {
            if (event.getType() == TableModelEvent.UPDATE) {
                int row = event.getFirstRow();
                int col = event.getColumn();

                String ingredient = (String) inventoryModel.getValueAt(row, 0);

                if (col == 3) { // Threshold changed
                    try {
                        int newThreshold = Integer.parseInt(
                                inventoryModel.getValueAt(row, col).toString()
                        );
                        inventoryService.setReorderThreshold(ingredient, newThreshold);
                    } catch (Exception ignored) {
                        JOptionPane.showMessageDialog(this, "Invalid threshold value.");
                    }
                }

                if (col == 4) { // Reorder Qty changed
                    try {
                        int newQty = Integer.parseInt(
                                inventoryModel.getValueAt(row, col).toString()
                        );
                        inventoryService.setReorderQuantity(ingredient, newQty);
                    } catch (Exception ignored) {
                        JOptionPane.showMessageDialog(this, "Invalid reorder quantity.");
                    }
                }

                refreshLowStockTable();
            }
        });

        JScrollPane invScroll = new JScrollPane(inventoryTable);

        JPanel invPanel = new JPanel(new BorderLayout(8, 8));
        invPanel.add(new JLabel("All Inventory"), BorderLayout.NORTH);
        invPanel.add(invScroll, BorderLayout.CENTER);

        invPanel.add(buildRestockPanel(), BorderLayout.SOUTH);

        /* =====================
             LOW STOCK TABLE
           ===================== */

        lowStockModel = new DefaultTableModel(
                new Object[]{"Ingredient", "Quantity", "Threshold"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        lowStockTable = new JTable(lowStockModel);
        JScrollPane lowScroll = new JScrollPane(lowStockTable);

        JPanel lowPanel = new JPanel(new BorderLayout(8, 8));
        lowPanel.add(new JLabel("Low Stock Items"), BorderLayout.NORTH);
        lowPanel.add(lowScroll, BorderLayout.CENTER);

        split.setTopComponent(invPanel);
        split.setBottomComponent(lowPanel);

        add(split, BorderLayout.CENTER);
    }

    private JPanel buildRestockPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.setBorder(BorderFactory.createTitledBorder("Manual Restock"));

        restockNameField = new JTextField(18);
        restockQtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
        restockExpiryMinutesSpinner = new JSpinner(new SpinnerNumberModel(60, 1, 999999, 1));

        JButton restockBtn = new JButton("Restock");

        p.add(new JLabel("Ingredient:"));
        p.add(restockNameField);
        p.add(new JLabel("Qty:"));
        p.add(restockQtySpinner);
        p.add(new JLabel("Expiry (mins):"));
        p.add(restockExpiryMinutesSpinner);
        p.add(restockBtn);

        restockBtn.addActionListener(e -> {
            String name = restockNameField.getText().trim();
            int qty = (Integer) restockQtySpinner.getValue();
            int mins = (Integer) restockExpiryMinutesSpinner.getValue();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingredient name required.");
                return;
            }

            long expiryMillis = System.currentTimeMillis() + mins * 60_000L;
            inventoryService.restock(name, qty, expiryMillis);

            JOptionPane.showMessageDialog(this, "Restocked " + qty + " x " + name);

            restockNameField.setText("");
            restockQtySpinner.setValue(1);
            restockExpiryMinutesSpinner.setValue(60);

            refreshAll();
        });

        return p;
    }

    private void refreshAll() {
        refreshInventoryTable();
        refreshLowStockTable();
    }

    private void refreshInventoryTable() {
        inventoryModel.setRowCount(0);

        for (Map.Entry<String, InventoryItem> entry : inventoryService.getInventory().entrySet()) {
            String name = entry.getKey();
            InventoryItem item = entry.getValue();

            String expiry = "-";
            try {
                long ts = item.getExpiryTimestamp();
                expiry = expiryFmt.format(new java.util.Date(ts));
            } catch (Exception ignored) {}

            int threshold = inventoryService.getThresholdFor(name);
            int reorderQty = inventoryService.getReorderQuantity(name);

            inventoryModel.addRow(new Object[]{
                    name,
                    item.getQuantity(),
                    expiry,
                    threshold,
                    reorderQty
            });
        }
    }

    private void refreshLowStockTable() {
        lowStockModel.setRowCount(0);

        for (Map.Entry<String, InventoryItem> entry : inventoryService.getLowStockItems().entrySet()) {
            String name = entry.getKey();
            InventoryItem item = entry.getValue();

            lowStockModel.addRow(new Object[]{
                    name,
                    item.getQuantity(),
                    inventoryService.getThresholdFor(name)
            });
        }
    }
}
