package com.restaurantops.thread;

import com.restaurantops.service.ReservationService;
import com.restaurantops.util.LoggerService;

public class ReservationMonitorThread implements Runnable {

    private final ReservationService reservationService;
    private final LoggerService logger;

    public ReservationMonitorThread(ReservationService reservationService,
                                    LoggerService logger) {
        this.reservationService = reservationService;
        this.logger = logger;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                reservationService.clearPastReservations();
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
