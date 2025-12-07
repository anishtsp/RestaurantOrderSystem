package com.restaurantops.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Waiter {

    private final int waiterId;
    private final String name;
    private volatile boolean available = true;
    private final Set<Integer> assignedTables = Collections.synchronizedSet(new HashSet<>());
    private volatile int currentLoad = 0;

    public Waiter(int waiterId, String name) {
        this.waiterId = waiterId;
        this.name = name;
    }

    public int getWaiterId() {
        return waiterId;
    }

    public String getName() {
        return name;
    }

    public synchronized boolean isAvailable() {
        return available;
    }

    public synchronized void setAvailable(boolean available) {
        this.available = available;
    }

    public void assignTable(int tableNumber) {
        assignedTables.add(tableNumber);
    }

    public void unassignTable(int tableNumber) {
        assignedTables.remove(tableNumber);
    }

    public Set<Integer> getAssignedTables() {
        return Collections.unmodifiableSet(assignedTables);
    }

    public synchronized int getCurrentLoad() {
        return currentLoad;
    }

    public synchronized void addLoad(int delta) {
        if (delta <= 0) return;
        currentLoad += delta;
    }

    public synchronized void reduceLoad(int delta) {
        if (delta <= 0) return;
        currentLoad = Math.max(0, currentLoad - delta);
    }

    @Override
    public String toString() {
        return waiterId + " | " + name + " | " + (available ? "AVAILABLE" : "BUSY") + " | load=" + currentLoad + " | tables=" + assignedTables.size();
    }
}
