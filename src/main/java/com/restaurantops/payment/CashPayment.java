package com.restaurantops.payment;

public class CashPayment implements PaymentMethod {

    @Override
    public boolean process(double amount) {
        System.out.println("[PAYMENT] Cash payment processed: ₹" + amount);
        return true;
    }

    @Override
    public String getMethodName() {
        return "Cash";
    }
}
