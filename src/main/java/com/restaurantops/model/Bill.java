package com.restaurantops.model;

import java.util.ArrayList;
import java.util.List;

public class Bill {
    private final int tableNumber;
    private final List<MenuItem> items = new ArrayList<>();
    private double total;
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    public Bill(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getTableNumber() { return tableNumber; }
    public List<MenuItem> getItems() { return items; }
    public double getTotal() { return total; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }

    public void addItem(MenuItem item, int quantity) {
        for (int i = 0; i < quantity; i++) {
            items.add(item);
            total += item.getPrice();
        }
    }

    public void markPaid() { paymentStatus = PaymentStatus.PAID; }
    public void markFailed() { paymentStatus = PaymentStatus.FAILED; }

    @Override
    public String toString() {
        return "Bill{table=" + tableNumber +
                ", items=" + items.size() +
                ", total=₹" + total +
                ", status=" + paymentStatus +
                '}';
    }
}
