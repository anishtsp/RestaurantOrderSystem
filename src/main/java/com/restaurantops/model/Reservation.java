package com.restaurantops.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Reservation {

    private final String id;
    private final int tableNumber;
    private final String customerName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private boolean active;

    public Reservation(int tableNumber, String customerName,
                       LocalDateTime startTime, LocalDateTime endTime) {

        this.id = UUID.randomUUID().toString();
        this.tableNumber = tableNumber;
        this.customerName = customerName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = true;
    }

    public String getId() { return id; }
    public int getTableNumber() { return tableNumber; }
    public String getCustomerName() { return customerName; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public boolean isActive() { return active; }

    public void expire() {
        this.active = false;
    }

    @Override
    public String toString() {
        return "[Reservation " + id.substring(0, 6) +
                "] Table " + tableNumber +
                " | " + customerName +
                " | " + startTime + " to " + endTime +
                " | active=" + active;
    }
}
