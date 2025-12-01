package com.restaurantops.core;

public class IdleMonitorThread implements Runnable {

    private final RestaurantEngine engine;
    private final long checkIntervalMillis;
    private final long idleThresholdMillis;

    public IdleMonitorThread(RestaurantEngine engine, long checkIntervalMillis, long idleThresholdMillis) {
        this.engine = engine;
        this.checkIntervalMillis = checkIntervalMillis;
        this.idleThresholdMillis = idleThresholdMillis;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(checkIntervalMillis);
                long last = engine.getLastOrderTime();
                long now = System.currentTimeMillis();
                if (!engine.isStationsPaused() && now - last > idleThresholdMillis) {
                    engine.pauseStationsForIdle();
                }
            }
        } catch (InterruptedException ignored) {
        }
    }
}
