package com.restaurantops.gui.panels;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.Order;
import com.restaurantops.model.OrderStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrdersPanel extends JPanel {

    private final RestaurantEngine engine;

    private JTable activeTable, allTable;
    private DefaultTableModel activeModel, allModel;

    private JLabel lblOrderId, lblTable, lblItem, lblQty, lblStatus, lblCategory, lblTime;

    private final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Timer autoRefreshTimer;

    public OrdersPanel() {
        engine = RestaurantEngine.getInstance();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        refreshTables();
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Orders");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JCheckBox autoRefresh = new JCheckBox("Auto Refresh (2s)");
        autoRefresh.addActionListener(e -> toggleAutoRefresh(autoRefresh.isSelected()));

        JPanel right = new JPanel();
        right.add(autoRefresh);

        top.add(title, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        return top;
    }

    private void toggleAutoRefresh(boolean enabled) {
        if (enabled) {
            autoRefreshTimer = new Timer(2000, e -> refreshTables());
            autoRefreshTimer.start();
        } else {
            if (autoRefreshTimer != null) autoRefreshTimer.stop();
        }
    }

    private JComponent buildBody() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.6);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Active Orders", buildActiveOrdersTable());
        tabs.addTab("All Orders", buildAllOrdersTable());

        JPanel detail = buildOrderDetailPanel();

        split.setLeftComponent(tabs);
        split.setRightComponent(detail);

        return split;
    }

    /* --------------------
          TABLES
       -------------------- */

    private JScrollPane buildActiveOrdersTable() {
        activeModel = new DefaultTableModel(
                new Object[]{"Order ID", "Table", "Item", "Qty", "Status"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        activeTable = new JTable(activeModel);
        activeTable.getSelectionModel().addListSelectionListener(e -> showSelectedOrder(activeTable));

        return new JScrollPane(activeTable);
    }

    private JScrollPane buildAllOrdersTable() {
        allModel = new DefaultTableModel(
                new Object[]{"Order ID", "Table", "Item", "Qty", "Status"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        allTable = new JTable(allModel);
        allTable.getSelectionModel().addListSelectionListener(e -> showSelectedOrder(allTable));

        return new JScrollPane(allTable);
    }

    /* --------------------
          ORDER DETAILS
       -------------------- */

    private JPanel buildOrderDetailPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Order Details");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        p.add(title);
        p.add(Box.createVerticalStrut(10));

        lblOrderId = addDetailLabel(p, "Order ID:");
        lblTable   = addDetailLabel(p, "Table:");
        lblItem    = addDetailLabel(p, "Item:");
        lblQty     = addDetailLabel(p, "Quantity:");
        lblStatus  = addDetailLabel(p, "Status:");
        lblCategory= addDetailLabel(p, "Category:");
        lblTime    = addDetailLabel(p, "Timestamp:");

        return p;
    }

    private JLabel addDetailLabel(JPanel parent, String title) {
        JPanel row = new JPanel(new BorderLayout());
        JLabel label = new JLabel(title);
        JLabel value = new JLabel("-");
        label.setPreferredSize(new Dimension(80, 20));
        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);

        parent.add(row);
        parent.add(Box.createVerticalStrut(6));
        return value;
    }

    private void showSelectedOrder(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;

        int orderId = (int) table.getValueAt(row, 0);

        Order selected = engine.getOrderService()
                .getAllOrders()
                .stream()
                .filter(o -> o.getOrderId() == orderId)
                .findFirst()
                .orElse(null);

        if (selected == null) return;

        lblOrderId.setText(String.valueOf(selected.getOrderId()));
        lblTable.setText(String.valueOf(selected.getTableNumber()));
        lblItem.setText(selected.getItem().getName());
        lblQty.setText(String.valueOf(selected.getQuantity()));
        lblStatus.setText(selected.getStatus().name());
        lblCategory.setText(selected.getCategory().name());
        lblTime.setText(selected.getTimestamp().format(fmt));
    }

    /* --------------------
         REFRESH LOGIC
       -------------------- */

    private void refreshTables() {
        refreshActiveOrders();
        refreshAllOrders();
    }

    private void refreshActiveOrders() {
        activeModel.setRowCount(0);

        List<Order> orders = engine.getOrderService().getAllOrders();

        for (Order o : orders) {
            if (o.getStatus() == OrderStatus.ACCEPTED ||
                    o.getStatus() == OrderStatus.IN_PROGRESS) {

                activeModel.addRow(new Object[]{
                        o.getOrderId(),
                        o.getTableNumber(),
                        o.getItem().getName(),
                        o.getQuantity(),
                        o.getStatus().name()
                });
            }
        }
    }

    private void refreshAllOrders() {
        allModel.setRowCount(0);

        for (Order o : engine.getOrderService().getAllOrders()) {
            allModel.addRow(new Object[]{
                    o.getOrderId(),
                    o.getTableNumber(),
                    o.getItem().getName(),
                    o.getQuantity(),
                    o.getStatus().name()
            });
        }
    }
}
