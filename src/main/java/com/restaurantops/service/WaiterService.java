package com.restaurantops.service;

import com.restaurantops.model.Waiter;
import com.restaurantops.util.LoggerService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WaiterService {

    private final Map<Integer, Waiter> waiters = new ConcurrentHashMap<>();
    private final LoggerService logger;
    private volatile int maxTablesPerWaiter = 6;

    public WaiterService(LoggerService logger) {
        this.logger = logger;
    }

    public boolean addWaiter(int id, String name) {
        if (waiters.containsKey(id)) return false;
        waiters.put(id, new Waiter(id, name));
        logger.log("[WAITERS] Added waiter " + name);
        return true;
    }

    public boolean removeWaiter(int id) {
        if (!waiters.containsKey(id)) return false;
        waiters.remove(id);
        logger.log("[WAITERS] Removed waiter " + id);
        return true;
    }

    public Waiter getWaiter(int id) {
        return waiters.get(id);
    }

    public List<Waiter> getWaiters() {
        return new ArrayList<>(waiters.values());
    }

    public synchronized Waiter findAvailableWaiter() {
        return waiters.values().stream().filter(Waiter::isAvailable).findFirst().orElse(null);
    }

    public synchronized Waiter getLeastLoadedWaiter() {
        return waiters.values().stream().min(Comparator.comparingInt(Waiter::getCurrentLoad)).orElse(null);
    }

    public synchronized boolean markBusy(int id) {
        Waiter w = waiters.get(id);
        if (w == null) return false;
        w.setAvailable(false);
        return true;
    }

    public synchronized boolean markAvailable(int id) {
        Waiter w = waiters.get(id);
        if (w == null) return false;
        w.setAvailable(true);
        return true;
    }

    public synchronized Waiter assignNextAvailableWaiter() {
        Waiter w = findAvailableWaiter();
        if (w == null) return null;
        markBusy(w.getWaiterId());
        return w;
    }

    public synchronized Waiter assignWaiterToTableBestFit() {
        Waiter candidate = getLeastLoadedWaiter();
        if (candidate == null) return null;
        return candidate;
    }

    public synchronized void updateLoad(int waiterId, int delta) {
        Waiter w = waiters.get(waiterId);
        if (w == null) return;
        if (delta > 0) w.addLoad(delta);
        else if (delta < 0) w.reduceLoad(-delta);
    }

    public synchronized void rebalanceWaiters() {
        if (waiters.size() < 2) return;
        List<Waiter> sorted = getWaiters();
        sorted.sort(Comparator.comparingInt(Waiter::getCurrentLoad));
        Waiter lowest = sorted.get(0);
        Waiter highest = sorted.get(sorted.size() - 1);
        if (highest.getCurrentLoad() - lowest.getCurrentLoad() > 8) {
            int shift = (highest.getCurrentLoad() - lowest.getCurrentLoad()) / 2;
            highest.reduceLoad(shift);
            lowest.addLoad(shift);
            logger.log("[WAITERS] Rebalanced load: moved " + shift + " from " + highest.getName() + " to " + lowest.getName());
        }
    }

    public synchronized void setMaxTablesPerWaiter(int max) {
        this.maxTablesPerWaiter = Math.max(1, max);
    }

    public int getMaxTablesPerWaiter() {
        return maxTablesPerWaiter;
    }
}
