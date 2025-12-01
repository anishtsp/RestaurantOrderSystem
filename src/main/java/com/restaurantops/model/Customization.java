package com.restaurantops.model;

public class Customization {
    private final boolean extraCheese;
    private final int spiceLevel; // 1-5
    private final String toppings;

    public Customization(boolean extraCheese, int spiceLevel, String toppings) {
        this.extraCheese = extraCheese;
        this.spiceLevel = Math.max(1, Math.min(5, spiceLevel));
        this.toppings = toppings == null ? "" : toppings;
    }

    public boolean isExtraCheese() { return extraCheese; }
    public int getSpiceLevel() { return spiceLevel; }
    public String getToppings() { return toppings; }

    @Override
    public String toString() {
        return "[extras: cheese=" + extraCheese +
                ", spice=" + spiceLevel +
                ", toppings=" + toppings + "]";
    }
}
