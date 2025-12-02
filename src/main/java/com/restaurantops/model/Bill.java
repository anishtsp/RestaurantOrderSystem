package com.restaurantops.model;

import java.util.ArrayList;
import java.util.List;

public class Bill {

    private final int tableNumber;
    private final List<Order> orders = new ArrayList<>();
    private double totalAmount;
    private boolean paid = false;

    public Bill(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public boolean isPaid() {
        return paid;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void addOrder(Order order) {
        orders.add(order);
        recalculateTotal();
    }

    private void recalculateTotal() {
        double sum = 0;
        for (Order o : orders) {
            sum += o.getItem().getPrice() * o.getQuantity();
        }
        this.totalAmount = sum;
    }

    public void markPaid() {
        this.paid = true;
    }

    public List<Order> getOrders() {
        return orders;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== BILL FOR TABLE ").append(tableNumber).append(" ===\n");

        for (Order o : orders) {
            sb.append(o.getItem().getName())
                    .append(" x ").append(o.getQuantity())
                    .append(" = ₹").append(o.getItem().getPrice() * o.getQuantity())
                    .append("\n");
        }

        sb.append("TOTAL: ₹").append(totalAmount).append("\n");
        sb.append("STATUS: ").append(paid ? "PAID" : "UNPAID").append("\n");

        return sb.toString();
    }
}
