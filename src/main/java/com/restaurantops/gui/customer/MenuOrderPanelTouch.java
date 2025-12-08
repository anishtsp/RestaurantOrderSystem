package com.restaurantops.gui.customer;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.service.MenuService;
import com.restaurantops.service.OrderService;
import com.restaurantops.service.TableService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MenuOrderPanelTouch extends JPanel {

    private final MenuService menuService;
    private final OrderService orderService;
    private final TableService tableService;

    public MenuOrderPanelTouch(RestaurantEngine engine) {

        this.menuService = engine.getMenuService();
        this.orderService = engine.getOrderService();
        this.tableService = engine.getTableService();

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Browse Menu", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 20, 20)); // 2 columns, auto rows
        add(new JScrollPane(grid), BorderLayout.CENTER);

        List<MenuItem> items = menuService.getAllItems();

        for (MenuItem m : items) {
            grid.add(buildItemCard(m));
        }
    }

    private JPanel buildItemCard(MenuItem m) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        card.setBackground(new Color(245, 245, 245));

        JLabel name = new JLabel(m.getName(), SwingConstants.CENTER);
        name.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel price = new JLabel("₹" + m.getPrice(), SwingConstants.CENTER);
        price.setFont(new Font("Arial", Font.PLAIN, 18));

        JLabel cal = new JLabel(m.getCalories() + " kcal", SwingConstants.CENTER);

        JButton orderBtn = new JButton("ORDER");
        orderBtn.setFont(new Font("Arial", Font.BOLD, 18));
        orderBtn.setBackground(new Color(0, 150, 0));
        orderBtn.setForeground(Color.WHITE);

        orderBtn.addActionListener(e -> openOrderDialog(m));

        card.add(Box.createVerticalStrut(10));
        card.add(name);
        card.add(price);
        card.add(cal);
        card.add(Box.createVerticalStrut(10));
        card.add(orderBtn);
        card.add(Box.createVerticalStrut(10));

        return card;
    }

    private void openOrderDialog(MenuItem m) {
        JPanel p = new JPanel(new GridLayout(3, 2, 10, 10));

        // TABLE DROPDOWN
        var tables = tableService.listTables().stream()
                .map(t -> t.getTableNumber())
                .toArray(Integer[]::new);

        JComboBox<Integer> tableBox = new JComboBox<>(tables);
        tableBox.setFont(new Font("Arial", Font.BOLD, 18));

        // QUANTITY PICKER (no typing)
        Integer[] qty = {1,2,3,4,5,6,7,8,9,10};
        JComboBox<Integer> qtyBox = new JComboBox<>(qty);
        qtyBox.setFont(new Font("Arial", Font.BOLD, 18));

        p.add(new JLabel("Table:", SwingConstants.RIGHT));
        p.add(tableBox);
        p.add(new JLabel("Quantity:", SwingConstants.RIGHT));
        p.add(qtyBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                p,
                "Order " + m.getName(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {

            int table = (Integer) tableBox.getSelectedItem();
            int quantity = (Integer) qtyBox.getSelectedItem();

            Order order = new Order(table, m, quantity);
            orderService.placeOrder(order);

            JOptionPane.showMessageDialog(this,
                    "Order placed! Order #" + order.getOrderId(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}
