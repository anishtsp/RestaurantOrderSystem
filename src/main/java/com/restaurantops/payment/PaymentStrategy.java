package com.restaurantops.payment;

import com.restaurantops.model.Bill;

public interface PaymentStrategy {
    boolean pay(Bill bill);
    String getName();
}
