package com.restaurantops.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class Table {

    private final int tableNumber;
    private final int capacity;
    private volatile TableState state;
    private volatile Waiter assignedWaiter;
    private volatile Reservation currentReservation;
    private volatile LocalDateTime occupiedSince;
    private final Set<Integer> mergedFrom = new TreeSet<>();
    private volatile boolean merged = false;

    public Table(int tableNumber, int capacity) {
        this.tableNumber = tableNumber;
        this.capacity = Math.max(1, capacity);
        this.state = TableState.FREE;
    }

    public Table(int tableNumber) {
        this(tableNumber, 4);
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getCapacity() {
        return capacity;
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
        this.merged = false;
        this.mergedFrom.clear();
    }

    public synchronized void mergeWith(Table other) {
        if (other == null) return;
        this.merged = true;
        this.mergedFrom.add(this.tableNumber);
        this.mergedFrom.add(other.getTableNumber());
        if (other.isMerged()) {
            this.mergedFrom.addAll(other.getMergedFrom());
        }
        this.state = TableState.MERGED;
    }

    public synchronized void addMergedTables(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        this.merged = true;
        this.mergedFrom.addAll(ids);
        this.mergedFrom.add(this.tableNumber);
        this.state = TableState.MERGED;
    }

    public synchronized void unmergeAll() {
        this.merged = false;
        this.mergedFrom.clear();
        if (this.state == TableState.MERGED) this.state = TableState.FREE;
    }

    public synchronized boolean isMerged() {
        return merged;
    }

    public synchronized Set<Integer> getMergedFrom() {
        return Set.copyOf(mergedFrom);
    }

    @Override
    public String toString() {
        String w = assignedWaiter == null ? "None" : assignedWaiter.getName();
        String r = currentReservation == null ? "None" : "Res#" + currentReservation.getReservationId();
        String mergedStr = "";
        if (merged && !mergedFrom.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Integer i : mergedFrom) {
                if (!first) sb.append("+");
                sb.append(i);
                first = false;
            }
            mergedStr = " | Merged: " + sb.toString();
        }
        return "Table " + tableNumber + " | cap=" + capacity + " | State=" + state + " | Waiter=" + w + " | Reservation=" + r + mergedStr;
    }
}
