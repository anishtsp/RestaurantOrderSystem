package com.restaurantops.service;

import com.restaurantops.model.Bill;
import com.restaurantops.model.Order;
import com.restaurantops.payment.PaymentMethod;

import java.util.HashMap;
import java.util.Map;

public class BillingService {

    private final Map<Integer, Bill> bills = new HashMap<>();

    public Bill getOrCreateBill(int tableNumber) {
        Bill b = bills.get(tableNumber);
        if (b == null) {
            b = new Bill(tableNumber);
            bills.put(tableNumber, b);
        }
        return b;
    }

    public void addOrderToBill(Order order) {
        int table = order.getTableNumber();
        Bill bill = getOrCreateBill(table);
        bill.addOrder(order);
    }

    public void printBill(int tableNumber) {
        Bill bill = bills.get(tableNumber);
        if (bill == null) {
            System.out.println("No bill exists for table " + tableNumber);
            return;
        }
        System.out.println(bill);
    }

    public void printAllBills() {
        if (bills.isEmpty()) {
            System.out.println("No bills available.");
            return;
        }
        for (Bill b : bills.values()) {
            System.out.println(b);
        }
    }

    public void processPayment(int tableNumber, PaymentMethod method) {
        Bill bill = bills.get(tableNumber);
        if (bill == null) {
            System.out.println("No bill found for this table.");
            return;
        }

        if (bill.isPaid()) {
            System.out.println("Bill already paid.");
            return;
        }

        boolean ok = method.process(bill.getTotalAmount());
        if (ok) {
            bill.markPaid();
            System.out.println("Payment successful using: " + method.getMethodName());
            System.out.println("--- Final Bill ---");
            System.out.println(bill);
        } else {
            System.out.println("Payment failed.");
        }
    }
}
