package com.restaurantops.gui.customer;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.Order;
import com.restaurantops.model.OrderStatus;
import com.restaurantops.service.OrderService;
import com.restaurantops.service.TableService;
import com.restaurantops.tracking.OrderListener;
import com.restaurantops.tracking.OrderTracker;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerOrdersPanelTouch extends JPanel {

    private final OrderService orderService;
    private final TableService tableService;
    private final OrderTracker tracker;

    private JPanel ordersGrid;
    private JComboBox<Integer> tableSelector;

    public CustomerOrdersPanelTouch(RestaurantEngine engine) {

        this.orderService = engine.getOrderService();
        this.tableService = engine.getTableService();
        this.tracker = engine.getOrderTracker();

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildOrdersArea(), BorderLayout.CENTER);

        refreshOrders();

        // LIVE tracking of order status
        tracker.addListener(new OrderListener() {
            @Override
            public void onOrderUpdated(Order order) {
                SwingUtilities.invokeLater(() -> refreshOrders());
            }
        });
    }

    private JPanel buildHeader() {

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));

        JLabel title = new JLabel("My Orders");
        title.setFont(new Font("Arial", Font.BOLD, 28));

        header.add(title);

        // TABLE DROPDOWN (touchscreen friendly)
        var tables = tableService.listTables()
                .stream()
                .map(t -> t.getTableNumber())
                .toArray(Integer[]::new);

        tableSelector = new JComboBox<>(tables);
        tableSelector.setFont(new Font("Arial", Font.BOLD, 20));
        tableSelector.addActionListener(e -> refreshOrders());

        header.add(new JLabel("Table:", SwingConstants.RIGHT));
        header.add(tableSelector);

        return header;
    }

    private JScrollPane buildOrdersArea() {
        ordersGrid = new JPanel(new GridLayout(0, 2, 20, 20)); // 2 columns touchscreen layout
        return new JScrollPane(ordersGrid);
    }

    private void refreshOrders() {

        int selectedTable = (Integer) tableSelector.getSelectedItem();

        List<Order> myOrders = orderService.getAllOrders()
                .stream()
                .filter(o -> o.getTableNumber() == selectedTable)
                .collect(Collectors.toList());

        ordersGrid.removeAll();

        if (myOrders.isEmpty()) {
            JLabel empty = new JLabel("No orders yet.", SwingConstants.CENTER);
            empty.setFont(new Font("Arial", Font.PLAIN, 22));
            ordersGrid.add(empty);
        } else {
            for (Order o : myOrders) {
                ordersGrid.add(buildOrderCard(o));
            }
        }

        ordersGrid.revalidate();
        ordersGrid.repaint();
    }

    private JPanel buildOrderCard(Order order) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        card.setBackground(new Color(250, 250, 250));

        JLabel title = new JLabel(order.getItem().getName(), SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel qty = new JLabel("Qty: " + order.getQuantity(), SwingConstants.CENTER);

        JLabel id = new JLabel("Order #" + order.getOrderId(), SwingConstants.CENTER);

        JLabel status = new JLabel(order.getStatus().toString(), SwingConstants.CENTER);
        status.setFont(new Font("Arial", Font.BOLD, 18));
        status.setOpaque(true);
        status.setForeground(Color.WHITE);
        status.setBackground(statusColor(order.getStatus()));

        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(Box.createVerticalStrut(5));
        card.add(qty);
        card.add(id);
        card.add(Box.createVerticalStrut(10));
        card.add(status);
        card.add(Box.createVerticalStrut(10));

        return card;
    }

    private Color statusColor(OrderStatus s) {
        return switch (s) {
            case NEW -> new Color(120, 120, 120);
            case ACCEPTED -> new Color(0, 150, 200);
            case IN_PROGRESS -> new Color(255, 153, 0);
            case COMPLETED -> new Color(0, 180, 0);
            case REJECTED -> new Color(200, 0, 0);
        };
    }
}
