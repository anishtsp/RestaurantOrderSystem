package com.restaurantops.core;

import com.restaurantops.model.Chef;
import com.restaurantops.model.Order;
import com.restaurantops.service.*;
import com.restaurantops.thread.InventoryMonitorThread;
import com.restaurantops.thread.ReservationMonitorThread;
import com.restaurantops.tracking.OrderTracker;

import java.util.concurrent.PriorityBlockingQueue;

public class RestaurantEngine {

    private static final RestaurantEngine INSTANCE = new RestaurantEngine();

    private final PriorityBlockingQueue<Order> priorityQueue =
            new PriorityBlockingQueue<>(11, new OrderPriorityComparator());

    private DispatchThread dispatchThread;
    private Thread dispatchWorker;

    private final OrderTracker orderTracker = new OrderTracker();
    private final MenuService menuService = new MenuService();
    private final InventoryService inventoryService = new InventoryService();
    private final BillingService billingService = new BillingService();
    private final ReservationService reservationService = new ReservationService();
    private final OrderService orderService = new OrderService(priorityQueue);
    private final StaffService staffService = new StaffService();

    private KitchenRouterService routerService;

    private Thread inventoryThread;
    private Thread reservationThread;
    private Thread idleMonitorThread;

    private long lastOrderTime = System.currentTimeMillis();
    private boolean stationsPaused = false;
    private boolean started = false;

    private RestaurantEngine() {}

    public static RestaurantEngine getInstance() {
        return INSTANCE;
    }

    public synchronized void start() {
        if (started) return;

        routerService = new KitchenRouterService(inventoryService, billingService, orderTracker);

        Chef c1 = new Chef(1, "Ravi");
        Chef c2 = new Chef(2, "Arjun");
        Chef c3 = new Chef(3, "Meera");

        staffService.addStaff(c1);
        staffService.addStaff(c2);
        staffService.addStaff(c3);

        routerService.startAllStations();

        routerService.getStations().forEach((cat, station) -> {
            Chef chef = staffService.findAvailableChef();
            if (chef != null) station.assignChef(chef);
        });

        dispatchThread = new DispatchThread(priorityQueue, routerService);
        dispatchWorker = new Thread(dispatchThread, "Dispatch-Thread");
        dispatchWorker.start();

        inventoryThread = new Thread(new InventoryMonitorThread(inventoryService), "InventoryMonitor");
        reservationThread = new Thread(new ReservationMonitorThread(reservationService), "ReservationMonitor");

        inventoryThread.start();
        reservationThread.start();

        long checkInterval = 1000L * 30L;
        long idleThreshold = 1000L * 60L * 10L;
        idleMonitorThread = new Thread(new IdleMonitorThread(this, checkInterval, idleThreshold), "IdleMonitor");
        idleMonitorThread.start();

        started = true;
        System.out.println("[ENGINE] Started");
    }

    public synchronized void stop() {
        if (!started) return;

        routerService.stopAllStations();

        if (dispatchWorker != null) dispatchWorker.interrupt();
        if (inventoryThread != null) inventoryThread.interrupt();
        if (reservationThread != null) reservationThread.interrupt();
        if (idleMonitorThread != null) idleMonitorThread.interrupt();

        started = false;
        System.out.println("[ENGINE] Stopped");
    }

    public synchronized void pauseStationsForIdle() {
        if (stationsPaused) return;
        routerService.stopAllStations();
        stationsPaused = true;
    }

    public synchronized void resumeStationsOnActivity() {
        if (!stationsPaused) return;
        routerService.startAllStations();
        stationsPaused = false;
    }

    public void notifyNewOrder(Order order) {
        lastOrderTime = System.currentTimeMillis();
        if (stationsPaused) resumeStationsOnActivity();
    }

    public long getLastOrderTime() {
        return lastOrderTime;
    }

    public boolean isStationsPaused() {
        return stationsPaused;
    }

    public MenuService getMenuService() { return menuService; }
    public InventoryService getInventoryService() { return inventoryService; }
    public BillingService getBillingService() { return billingService; }
    public ReservationService getReservationService() { return reservationService; }
    public OrderService getOrderService() { return orderService; }
    public StaffService getStaffService() { return staffService; }
    public OrderTracker getOrderTracker() { return orderTracker; }
    public KitchenRouterService getRouterService() { return routerService; }

    public boolean isStarted() { return started; }
}
