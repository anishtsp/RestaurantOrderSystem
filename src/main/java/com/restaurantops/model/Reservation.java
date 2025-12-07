package com.restaurantops.model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class Reservation {

    private static final AtomicInteger GEN = new AtomicInteger(1);

    private final int reservationId;
    private final int tableNumber;
    private final LocalDateTime start;
    private final LocalDateTime end;

    public Reservation(int tableNumber,
                       LocalDateTime start,
                       LocalDateTime end) {
        this.reservationId = GEN.getAndIncrement();
        this.tableNumber = tableNumber;
        this.start = start;
        this.end = end;
    }

    public int getReservationId() {
        return reservationId;
    }

    public int getTableNumber() {
        return tableNumber;
    }



    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public boolean overlaps(LocalDateTime s, LocalDateTime e) {
        return !(e.isBefore(start) || s.isAfter(end));
    }

    @Override
    public String toString() {
        return "Reservation#" + reservationId +
                " | Table " + tableNumber +
                " | " + start + " → " + end;
    }
}
