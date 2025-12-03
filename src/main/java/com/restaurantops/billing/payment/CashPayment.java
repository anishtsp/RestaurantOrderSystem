package com.restaurantops.billing.payment;

public class CashPayment implements PaymentMethod {

    @Override
    public boolean process(double amount) {
        System.out.println("Processing CASH payment...");
        return true;
    }

    @Override
    public String getMethodName() {
        return "Cash";
    }
}
