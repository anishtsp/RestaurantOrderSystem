package com.restaurantops.core;

import com.restaurantops.service.LoggerService;

public class IdleMonitorThread implements Runnable {

    private final RestaurantEngine engine;
    private final long checkIntervalMillis;
    private final long idleThresholdMillis;
    private final LoggerService logger;

    public IdleMonitorThread(RestaurantEngine engine,
                             long checkIntervalMillis,
                             long idleThresholdMillis,
                             LoggerService logger) {
        this.engine = engine;
        this.checkIntervalMillis = checkIntervalMillis;
        this.idleThresholdMillis = idleThresholdMillis;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(checkIntervalMillis);
                long now = System.currentTimeMillis();
                if (!engine.isStationsPaused() &&
                        now - engine.getLastOrderTime() > idleThresholdMillis) {
                    logger.log("[IDLE] Kitchen idle, pausing stations");
                    engine.pauseStationsForIdle();
                }
            }
        } catch (InterruptedException ignored) {
        }
    }
}
