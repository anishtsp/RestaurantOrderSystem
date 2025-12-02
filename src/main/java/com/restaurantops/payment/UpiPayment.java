package com.restaurantops.payment;

public class UpiPayment implements PaymentMethod {

    @Override
    public boolean process(double amount) {
        System.out.println("[PAYMENT] UPI payment processed: ₹" + amount);
        return true;
    }

    @Override
    public String getMethodName() {
        return "UPI";
    }
}
