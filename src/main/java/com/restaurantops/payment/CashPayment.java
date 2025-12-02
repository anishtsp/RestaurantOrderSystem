package com.restaurantops.payment;

import com.restaurantops.model.Bill;

public class CashPayment implements PaymentStrategy {

    @Override
    public boolean pay(Bill bill) {
        System.out.println("Processing CASH payment for Table "
                + bill.getTableNumber() + " | Amount: ₹" + bill.getTotalAmount());
        return true;
    }

    @Override
    public String getName() {
        return "CASH";
    }
}
