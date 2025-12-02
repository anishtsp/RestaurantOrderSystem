package com.restaurantops.payment;

public interface PaymentMethod {

    boolean process(double amount);

    String getMethodName();
}
