package com.restaurantops.model;

public class ChefAssignment {

    private final Chef chef;
    private final String stationName;

    public ChefAssignment(Chef chef, String stationName) {
        this.chef = chef;
        this.stationName = stationName;
    }

    public Chef getChef() {
        return chef;
    }

    public String getStationName() {
        return stationName;
    }
}
