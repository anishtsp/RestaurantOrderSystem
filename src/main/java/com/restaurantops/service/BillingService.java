package com.restaurantops.service;

import com.restaurantops.model.Bill;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.PaymentStatus;
import com.restaurantops.payment.PaymentStrategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BillingService {

    private final Map<Integer, Bill> bills = new ConcurrentHashMap<>();

    public void addItemToBill(int tableNumber, MenuItem item, int quantity) {
        Bill bill = bills.computeIfAbsent(tableNumber, Bill::new);
        bill.addItem(item, quantity);
        System.out.println("   [BILLING] Added " + quantity + " x " + item.getName() +
                " to Table " + tableNumber + " | Total now: ₹" + bill.getTotal());
    }

    public Bill getBillForTable(int tableNumber) {
        return bills.get(tableNumber);
    }

    public void printAllBills() {
        System.out.println("\n=== Bills Summary ===");
        if (bills.isEmpty()) {
            System.out.println("No bills generated.");
        } else {
            bills.values().forEach(System.out::println);
        }
    }

    public void processPayment(int tableNumber, PaymentStrategy strategy) {
        Bill bill = bills.get(tableNumber);
        if (bill == null) {
            System.out.println("No bill found for table " + tableNumber);
            return;
        }
        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            System.out.println("Table " + tableNumber + " already paid.");
            return;
        }

        System.out.println("\n=== Payment for Table " + tableNumber +
                " using " + strategy.getName() + " ===");
        boolean success = strategy.pay(bill);
        if (success) {
            bill.markPaid();
            System.out.println("Payment SUCCESS for Table " + tableNumber);
        } else {
            bill.markFailed();
            System.out.println("Payment FAILED for Table " + tableNumber);
        }
    }
}
