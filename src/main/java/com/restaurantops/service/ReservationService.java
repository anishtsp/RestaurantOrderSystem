package com.restaurantops.service;

import com.restaurantops.model.Reservation;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReservationService {

    private final Map<Integer, Reservation> reservations = new ConcurrentHashMap<>();

    public Collection<Reservation> getAllReservations() {
        return reservations.values();
    }

    public Reservation getReservation(int reservationId) {
        return reservations.get(reservationId);
    }

    public synchronized Reservation reserveTable(int tableNumber,
                                                 LocalDateTime start,
                                                 LocalDateTime end) {

        for (Reservation r : reservations.values()) {
            if (r.getTableNumber() == tableNumber &&
                    r.overlaps(start, end)) {
                return null;
            }
        }

        Reservation r = new Reservation(tableNumber, start, end);
        reservations.put(r.getReservationId(), r);
        return r;
    }

    public Reservation getById(int id) {
        return reservations.get(id);
    }

    public boolean cancel(int reservationId) {
        reservations.remove(reservationId);
        return false;
    }



    public synchronized void clearPastReservations() {
        LocalDateTime now = LocalDateTime.now();
        reservations.values().removeIf(r -> r.getEnd().isBefore(now));
    }
}
