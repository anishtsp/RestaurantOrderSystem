package com.restaurantops.payment;

public class CardPayment implements PaymentMethod {

    @Override
    public boolean process(double amount) {
        System.out.println("[PAYMENT] Card payment processed: ₹" + amount);
        return true;
    }

    @Override
    public String getMethodName() {
        return "Card";
    }
}
