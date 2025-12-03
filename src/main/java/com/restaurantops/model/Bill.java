package com.restaurantops.model;

import java.util.ArrayList;
import java.util.List;

public class Bill {

    private final int tableNumber;
    private final List<Order> orders = new ArrayList<>();
    private boolean paid = false;

    public Bill(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void addOrder(Order order) {
        if (order != null) {
            orders.add(order);
        }
    }

    public double getTotalAmount() {
        double total = 0;

        for (Order o : orders) {
            MenuItem item = o.getItem();
            if (item != null) {
                total += item.getPrice() * o.getQuantity();
            }
        }

        return total;
    }

    public boolean isPaid() {
        return paid;
    }

    public void markPaid() {
        paid = true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== BILL FOR TABLE ").append(tableNumber).append(" ===\n");

        if (orders.isEmpty()) {
            sb.append("No items.\n");
        } else {
            for (Order o : orders) {
                MenuItem i = o.getItem();
                sb.append(i.getName())
                        .append(" x ")
                        .append(o.getQuantity())
                        .append(" = ₹")
                        .append(i.getPrice() * o.getQuantity())
                        .append("\n");
            }
        }

        sb.append("TOTAL: ₹").append(getTotalAmount()).append("\n");
        sb.append("STATUS: ").append(paid ? "PAID" : "UNPAID");

        return sb.toString();
    }
}
