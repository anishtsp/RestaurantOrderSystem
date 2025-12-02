package com.restaurantops.model;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class SupplierOrder implements Delayed {

    private final String ingredient;
    private final int quantity;
    private final long deliveryTimeMillis;

    public SupplierOrder(String ingredient, int quantity, long delayMillis) {
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.deliveryTimeMillis = System.currentTimeMillis() + delayMillis;
    }

    public String getIngredient() {
        return ingredient;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = deliveryTimeMillis - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        SupplierOrder other = (SupplierOrder) o;
        return Long.compare(this.deliveryTimeMillis, other.deliveryTimeMillis);
    }
}
