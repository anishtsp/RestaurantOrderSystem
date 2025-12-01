package com.restaurantops.thread;

import com.restaurantops.service.ReservationService;

public class ReservationMonitorThread implements Runnable {

    private final ReservationService reservationService;

    public ReservationMonitorThread(ReservationService rs) {
        this.reservationService = rs;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(30000); // check every 30 sec
                reservationService.expireOldReservations();
            }
        } catch (InterruptedException ignored) {}
    }
}
