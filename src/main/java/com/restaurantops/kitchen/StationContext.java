package com.restaurantops.kitchen;

import com.restaurantops.staff.Chef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StationContext {

    private final List<Chef> assignedChefs = Collections.synchronizedList(new ArrayList<>());

    public void assignChef(Chef chef) {
        if (chef == null) return;
        synchronized (assignedChefs) {
            if (!assignedChefs.contains(chef)) assignedChefs.add(chef);
        }
    }

    public void unassignChef(Chef chef) {
        if (chef == null) return;
        synchronized (assignedChefs) {
            assignedChefs.remove(chef);
        }
    }

    public List<Chef> getAssignedChefs() {
        synchronized (assignedChefs) {
            return new ArrayList<>(assignedChefs);
        }
    }

    public Chef getAssignedChef() {
        synchronized (assignedChefs) {
            return assignedChefs.isEmpty() ? null : assignedChefs.get(0);
        }
    }

    public int chefCount() {
        synchronized (assignedChefs) {
            return assignedChefs.size();
        }
    }
}
