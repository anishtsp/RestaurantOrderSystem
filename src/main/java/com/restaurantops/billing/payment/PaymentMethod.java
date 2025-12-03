package com.restaurantops.billing.payment;

public interface PaymentMethod {
    boolean process(double amount);
    String getMethodName();
}
