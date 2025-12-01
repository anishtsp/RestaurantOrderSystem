package com.restaurantops.service;

import com.restaurantops.model.Reservation;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReservationService {

    private final Map<Integer, Reservation> reservations =
            new ConcurrentHashMap<>();

    public boolean isTableAvailable(int tableNumber, LocalDateTime start, LocalDateTime end) {
        Reservation r = reservations.get(tableNumber);
        if (r == null || !r.isActive()) return true;

        // Overlap check
        boolean overlap =
                !(end.isBefore(r.getStartTime()) || start.isAfter(r.getEndTime()));

        return !overlap;
    }

    public Reservation reserveTable(int tableNumber, String customerName,
                                    LocalDateTime start, LocalDateTime end) {

        if (!isTableAvailable(tableNumber, start, end))
            return null;

        Reservation r = new Reservation(tableNumber, customerName, start, end);
        reservations.put(tableNumber, r);
        return r;
    }

    public void expireOldReservations() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        for (Reservation r : reservations.values()) {
            if (r.isActive() && r.getEndTime().isBefore(now)) {
                r.expire();
                System.out.println("[RESERVATION] Expired: " + r);
            }
        }
    }

    public Collection<Reservation> getAllReservations() {
        return reservations.values();
    }
}
