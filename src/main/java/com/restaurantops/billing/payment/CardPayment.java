package com.restaurantops.billing.payment;

public class CardPayment implements PaymentMethod {

    @Override
    public boolean process(double amount) {
        System.out.println("Processing CARD payment...");
        return true;
    }

    @Override
    public String getMethodName() {
        return "Card";
    }
}
