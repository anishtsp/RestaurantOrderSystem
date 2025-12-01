package com.restaurantops.service;

import com.restaurantops.model.Chef;
import com.restaurantops.model.Staff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StaffService {

    private final List<Staff> staffList = Collections.synchronizedList(new ArrayList<>());

    public void addStaff(Staff s) {
        staffList.add(s);
        System.out.println("[STAFF] Added: " + s.getName() + " (" + s.getRole() + ")");
    }

    public List<Staff> getAllStaff() {
        return staffList;
    }

    public Chef findAvailableChef() {
        for (Staff s : staffList) {
            if (s instanceof Chef) return (Chef) s;
        }
        return null;
    }
}
