package com.restaurantops.kitchen;

import com.restaurantops.model.Chef;

public class StationContext {

    private Chef assignedChef;

    public void assignChef(Chef chef) {
        this.assignedChef = chef;
    }

    public Chef getAssignedChef() {
        return assignedChef;
    }
}
