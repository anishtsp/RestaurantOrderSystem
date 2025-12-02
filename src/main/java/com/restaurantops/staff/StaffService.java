package com.restaurantops.staff;

import com.restaurantops.util.LoggerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StaffService {

    private final List<Staff> staffList = Collections.synchronizedList(new ArrayList<>());
    private final LoggerService logger;

    public StaffService(LoggerService logger) {
        this.logger = logger;
    }

    public void addStaff(Staff s) {
        staffList.add(s);
        logger.log("[STAFF] Added: " + s.getName() + " (" + s.getRole() + ")");
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
