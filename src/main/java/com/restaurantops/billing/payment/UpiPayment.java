package com.restaurantops.billing.payment;

public class UpiPayment implements PaymentMethod {

    @Override
    public boolean process(double amount) {
        System.out.println("Processing UPI payment...");
        return true;
    }

    @Override
    public String getMethodName() {
        return "UPI";
    }
}
