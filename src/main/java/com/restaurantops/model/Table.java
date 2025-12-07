package com.restaurantops.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Table {

    private final int tableNumber;
    private volatile TableState state;
    private volatile Waiter assignedWaiter;
    private volatile Reservation currentReservation;
    private volatile LocalDateTime occupiedSince;

    public Table(int tableNumber) {
        this.tableNumber = tableNumber;
        this.state = TableState.FREE;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public synchronized TableState getState() {
        return state;
    }

    public synchronized void setState(TableState state) {
        this.state = Objects.requireNonNull(state);
    }

    public synchronized Optional<Waiter> getAssignedWaiter() {
        return Optional.ofNullable(assignedWaiter);
    }

    public synchronized void assignWaiter(Waiter waiter) {
        this.assignedWaiter = waiter;
    }

    public synchronized void unassignWaiter() {
        this.assignedWaiter = null;
    }

    public synchronized Optional<Reservation> getCurrentReservation() {
        return Optional.ofNullable(currentReservation);
    }

    public synchronized void setReservation(Reservation reservation) {
        this.currentReservation = reservation;
        if (reservation != null) this.state = TableState.RESERVED;
    }

    public synchronized void clearReservation() {
        this.currentReservation = null;
        if (this.state == TableState.RESERVED) this.state = TableState.FREE;
    }

    public synchronized Optional<LocalDateTime> getOccupiedSince() {
        return Optional.ofNullable(occupiedSince);
    }

    public synchronized void markOccupiedNow() {
        this.occupiedSince = LocalDateTime.now();
        this.state = TableState.OCCUPIED;
    }

    public synchronized void markNeedsCleaning() {
        this.state = TableState.NEEDS_CLEANING;
        this.occupiedSince = null;
    }

    public synchronized void markFree() {
        this.state = TableState.FREE;
        this.currentReservation = null;
        this.assignedWaiter = null;
        this.occupiedSince = null;
    }

    @Override
    public String toString() {
        String w = assignedWaiter == null ? "None" : assignedWaiter.getName();
        String r = currentReservation == null ? "None" : currentReservation.toString();
        return "Table " + tableNumber + " | State=" + state + " | Waiter=" + w + " | Reservation=" + r;
    }
}
