package com.restaurantops.service;

import com.restaurantops.model.*;
import com.restaurantops.util.LoggerService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TableService {

    private final Map<Integer, Table> tables = new ConcurrentHashMap<>();
    private final ReservationService reservationService;
    private final WaiterService waiterService;
    private final LoggerService logger;

    public TableService(ReservationService reservationService,
                        WaiterService waiterService,
                        LoggerService logger) {
        this.reservationService = reservationService;
        this.waiterService = waiterService;
        this.logger = logger;
    }

    public synchronized boolean addTable(int tableNumber, int capacity) {
        if (tables.containsKey(tableNumber)) return false;
        tables.put(tableNumber, new Table(tableNumber, capacity));
        logger.log("[TABLES] Added table " + tableNumber + " cap=" + capacity);
        return true;
    }

    public synchronized boolean addTable(int tableNumber) {
        return addTable(tableNumber, 4);
    }

    public synchronized boolean removeTable(int tableNumber) {
        Table t = tables.get(tableNumber);
        if (t == null) return false;
        if (t.getState() != TableState.FREE) return false;
        tables.remove(tableNumber);
        logger.log("[TABLES] Removed table " + tableNumber);
        return true;
    }

    public Optional<Table> getTable(int tableNumber) {
        return Optional.ofNullable(tables.get(tableNumber));
    }

    public Collection<Table> listTables() {
        return Collections.unmodifiableCollection(tables.values());
    }

    public synchronized Reservation reserveTable(int tableNumber, String customerName, LocalDateTime start, LocalDateTime end) {
        Table t = tables.get(tableNumber);
        if (t == null) return null;
        Reservation r = reservationService.reserveTable(tableNumber, start, end);
        if (r == null) return null;
        t.setReservation(r);
        logger.log("[TABLES] Reserved table " + tableNumber);
        return r;
    }

    public synchronized Reservation reserveTableForParty(int partySize, String customerName, LocalDateTime start, LocalDateTime end) {
        int tableNumber = assignTableForPartySize(partySize);
        if (tableNumber == -1) {
            int mergedBase = mergeTablesForParty(partySize);
            if (mergedBase == -1) return null;
            tableNumber = mergedBase;
        }
        Reservation r = reservationService.reserveTable(tableNumber, start, end);
        if (r == null) return null;
        Table t = tables.get(tableNumber);
        if (t != null) t.setReservation(r);
        logger.log("[TABLES] Reserved table " + tableNumber + " for party size " + partySize);
        return r;
    }

    public synchronized int assignTableForPartySize(int partySize) {
        Optional<Table> best = tables.values().stream()
                .filter(t -> t.getState() == TableState.FREE && !t.isMerged())
                .sorted(Comparator.comparingInt(Table::getCapacity))
                .filter(t -> t.getCapacity() >= partySize)
                .findFirst();
        return best.map(Table::getTableNumber).orElse(-1);
    }

    private synchronized int mergeTablesForParty(int partySize) {
        List<Table> free = new ArrayList<>();
        for (Table t : tables.values()) {
            if (t.getState() == TableState.FREE && !t.isMerged()) free.add(t);
        }
        free.sort(Comparator.comparingInt(Table::getCapacity));
        int total = 0;
        int base = -1;
        Set<Integer> mergedIds = new TreeSet<>();
        for (Table t : free) {
            if (base == -1) base = t.getTableNumber();
            mergedIds.add(t.getTableNumber());
            total += t.getCapacity();
            if (total >= partySize) break;
        }
        if (total < partySize || base == -1) return -1;
        Table baseTable = tables.get(base);
        baseTable.addMergedTables(mergedIds);
        baseTable.setState(TableState.RESERVED);
        for (Integer id : mergedIds) {
            if (id == base) continue;
            Table other = tables.get(id);
            if (other != null) {
                other.mergeWith(baseTable);
                other.setState(TableState.MERGED);
            }
        }
        logger.log("[TABLES] Merged tables " + mergedIds + " -> base " + base);
        return base;
    }

    public synchronized boolean occupyReservedTable(int reservationId) {
        Reservation r = reservationService.getById(reservationId);
        if (r == null) return false;
        int tableNumber = r.getTableNumber();
        Table table = tables.get(tableNumber);
        if (table == null) return false;
        if (table.getState() == TableState.OCCUPIED) return false;
        table.markOccupiedNow();
        reservationService.cancel(reservationId);
        if (table.getAssignedWaiter().isEmpty()) {
            Waiter w = waiterService.assignNextAvailableWaiter();
            if (w != null) {
                table.assignWaiter(w);
                w.assignTable(tableNumber);
                waiterService.updateLoad(w.getWaiterId(), 1);
            }
        }
        logger.log("[TABLES] Occupied reserved table " + tableNumber);
        return true;
    }

    public synchronized Integer occupyAnyFreeTable() {
        for (Table t : tables.values()) {
            if (t.getState() == TableState.FREE && !t.isMerged()) {
                t.markOccupiedNow();
                int tableNumber = t.getTableNumber();
                if (t.getAssignedWaiter().isEmpty()) {
                    Waiter w = waiterService.assignNextAvailableWaiter();
                    if (w != null) {
                        t.assignWaiter(w);
                        w.assignTable(tableNumber);
                        waiterService.updateLoad(w.getWaiterId(), 1);
                    }
                }
                logger.log("[TABLES] Occupied free table " + tableNumber);
                return tableNumber;
            }
        }
        return null;
    }

    public synchronized boolean occupyTable(int tableNumber) {
        Table t = tables.get(tableNumber);
        if (t == null) return false;
        var res = t.getCurrentReservation();
        if (res.isPresent()) {
            LocalDateTime now = LocalDateTime.now();
            Reservation r = res.get();
            if (now.isBefore(r.getStart()) || now.isAfter(r.getEnd())) return false;
        }
        if (t.getAssignedWaiter().isEmpty()) {
            Waiter w = waiterService.findAvailableWaiter();
            if (w != null) {
                t.assignWaiter(w);
                w.assignTable(t.getTableNumber());
                waiterService.markBusy(w.getWaiterId());
            }
        }
        t.markOccupiedNow();
        logger.log("[TABLES] Table " + tableNumber + " OCCUPIED");
        return true;
    }

    public synchronized boolean releaseTable(int tableNumber) {
        Table t = tables.get(tableNumber);
        if (t == null) return false;
        t.markNeedsCleaning();
        t.getAssignedWaiter().ifPresent(w -> {
            w.unassignTable(tableNumber);
            waiterService.updateLoad(w.getWaiterId(), -1);
            waiterService.markAvailable(w.getWaiterId());
        });
        logger.log("[TABLES] Table " + tableNumber + " NEEDS CLEANING");
        return true;
    }

    public synchronized boolean cleanTable(int tableNumber) {
        Table t = tables.get(tableNumber);
        if (t == null) return false;
        if (t.isMerged()) {
            Set<Integer> merged = t.getMergedFrom();
            for (Integer id : merged) {
                Table other = tables.get(id);
                if (other != null) {
                    other.unmergeAll();
                    other.setState(TableState.FREE);
                }
            }
            t.unmergeAll();
        }
        t.markFree();
        logger.log("[TABLES] Table " + tableNumber + " CLEANED/FREE");
        return true;
    }

    public synchronized boolean assignWaiterToTable(int tableNumber, Waiter waiter) {
        Table t = tables.get(tableNumber);
        if (t == null) return false;
        t.assignWaiter(waiter);
        waiter.assignTable(tableNumber);
        waiterService.updateLoad(waiter.getWaiterId(), 1);
        waiterService.markBusy(waiter.getWaiterId());
        logger.log("[TABLES] Assigned waiter " + waiter.getName() + " -> table " + tableNumber);
        return true;
    }

    public synchronized boolean unassignWaiterFromTable(int tableNumber) {
        Table t = tables.get(tableNumber);
        if (t == null) return false;
        t.getAssignedWaiter().ifPresent(w -> {
            w.unassignTable(tableNumber);
            waiterService.updateLoad(w.getWaiterId(), -1);
            waiterService.markAvailable(w.getWaiterId());
        });
        t.unassignWaiter();
        logger.log("[TABLES] Unassigned waiter from table " + tableNumber);
        return true;
    }

    public List<Table> findTablesByState(TableState state) {
        List<Table> out = new ArrayList<>();
        for (Table t : tables.values()) {
            if (t.getState() == state) out.add(t);
        }
        return out;
    }

    public Optional<Table> findTableForWaiter(Waiter waiter) {
        return tables.values().stream()
                .filter(t -> t.getAssignedWaiter()
                        .map(w -> w.getWaiterId() == waiter.getWaiterId())
                        .orElse(false))
                .findFirst();
    }
}
