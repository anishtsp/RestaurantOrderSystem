package com.restaurantops.thread;

import com.restaurantops.service.LoggerService;
import com.restaurantops.service.ReservationService;

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
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(30000);
                reservationService.expireOldReservations();
                logger.log("[RESERVATION] Expired old reservations");
            }
        } catch (InterruptedException ignored) {
        }
    }
}
