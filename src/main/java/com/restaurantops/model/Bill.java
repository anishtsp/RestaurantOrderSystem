package com.restaurantops.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Bill {

    private final int tableNumber;
    private boolean paid = false;

    // aggregated items: key = item name
    private final Map<String, BillLine> lines = new LinkedHashMap<>();

    public Bill(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void addOrder(Order order) {
        String key = order.getItem().getName();

        BillLine line = lines.get(key);
        if (line == null) {
            lines.put(key, new BillLine(
                    order.getItem().getName(),
                    order.getItem().getPrice(),
                    order.getQuantity()
            ));
        } else {
            line.addQuantity(order.getQuantity());
        }
    }

    public double getTotalAmount() {
        return lines.values().stream()
                .mapToDouble(BillLine::getTotal)
                .sum();
    }

    public void markPaid() {
        this.paid = true;
    }

    public boolean isPaid() {
        return paid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== BILL FOR TABLE ").append(tableNumber).append(" ===\n");

        int index = 1;
        for (BillLine l : lines.values()) {
            sb.append(String.format("%d) %-18s x%-3d ₹%.2f%n",
                    index++,
                    l.name,
                    l.quantity,
                    l.getTotal()
            ));
        }

        sb.append("--------------------------------\n");
        sb.append(String.format("TOTAL: ₹%.2f%n", getTotalAmount()));
        sb.append("STATUS: ").append(paid ? "PAID" : "UNPAID");

        return sb.toString();
    }

    private static class BillLine {
        private final String name;
        private final double unitPrice;
        private int quantity;

        BillLine(String name, double unitPrice, int quantity) {
            this.name = name;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        void addQuantity(int q) {
            this.quantity += q;
        }

        double getTotal() {
            return unitPrice * quantity;
        }
    }
}
