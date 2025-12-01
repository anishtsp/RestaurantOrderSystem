package com.restaurantops.model;

public abstract class Staff {

    private final int staffId;
    private final String name;
    private final String role;

    public Staff(int staffId, String name, String role) {
        this.staffId = staffId;
        this.name = name;
        this.role = role;
    }

    public int getStaffId() {
        return staffId;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}
