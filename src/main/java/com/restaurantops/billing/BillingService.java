package com.restaurantops.billing;

import com.restaurantops.model.Bill;
import com.restaurantops.model.Order;
import com.restaurantops.billing.payment.PaymentMethod;

import java.util.HashMap;
import java.util.Map;

public class BillingService {

    private final Map<Integer, Bill> bills = new HashMap<>();

    public Bill getOrCreateBill(int tableNumber) {
        return bills.computeIfAbsent(tableNumber, Bill::new);
    }

    public void addOrderToBill(Order order) {
        Bill bill = getOrCreateBill(order.getTableNumber());
        bill.addOrder(order);
        System.out.println("[BILLING] Added Order#" + order.getOrderId() +
                " to table " + order.getTableNumber());
    }

    public Bill getBill(int tableNumber) {
        return bills.get(tableNumber);
    }

    public void printAllBills() {
        if (bills.isEmpty()) {
            System.out.println("No bills available.");
            return;
        }
        bills.values().forEach(System.out::println);
    }

    public Map<Integer, Bill> getAllBills() {
        return bills;
    }


    public void processPayment(int tableNumber, PaymentMethod method) {
        Bill bill = bills.get(tableNumber);

        if (bill == null) {
            System.out.println("No bill found for table " + tableNumber);
            return;
        }

        if (bill.isPaid()) {
            System.out.println("This bill is already paid.");
            return;
        }

        double amount = bill.getTotalAmount();
        boolean success = method.process(amount);

        if (success) {
            bill.markPaid();
            System.out.println("Payment SUCCESSFUL via " + method.getMethodName());
            System.out.println(bill);
        } else {
            System.out.println("Payment FAILED via " + method.getMethodName());
        }
    }
}
