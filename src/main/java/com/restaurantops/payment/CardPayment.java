package com.restaurantops.payment;

import com.restaurantops.model.Bill;

import java.util.Random;

public class CardPayment implements PaymentStrategy {

    private final Random random = new Random();

    @Override
    public boolean pay(Bill bill) {
        System.out.println("Processing CARD payment for Table "
                + bill.getTableNumber() + " | Amount: ₹" + bill.getTotal());
        return random.nextInt(10) != 0; // 10% fail chance
    }

    @Override
    public String getName() {
        return "CARD";
    }
}
