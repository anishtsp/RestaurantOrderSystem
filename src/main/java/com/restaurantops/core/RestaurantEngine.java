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

    private final LoggerService logger = new LoggerService();
    private final OrderTracker orderTracker = new OrderTracker();
    private final RecipeService recipeService = new RecipeService();
    private final MenuService menuService = new MenuService(recipeService);
    private final InventoryService inventoryService = new InventoryService();
    private final BillingService billingService = new BillingService();
    private final ReservationService reservationService = new ReservationService();
    private final StaffService staffService = new StaffService(logger);
    private final OrderService orderService = new OrderService(priorityQueue, logger);

    private KitchenRouterService routerService;

    private Thread inventoryThread;
    private Thread reservationThread;
    private Thread idleMonitorThread;

    private long lastOrderTime = System.currentTimeMillis();
    private volatile boolean stationsPaused = false;

    private boolean started = false;

    private RestaurantEngine() {}

    public static RestaurantEngine getInstance() {
        return INSTANCE;
    }

    public synchronized void start() {
        if (started) return;

        logger.log("[ENGINE] Starting...");

        java.util.List<com.restaurantops.model.MenuItem> menuList = menuService.getAllItems();
        com.restaurantops.service.InventoryInitializer.syncMenuToInventory(menuList, inventoryService);
        logger.log("[ENGINE] Inventory auto-loaded from menu");

        routerService = new KitchenRouterService(inventoryService, billingService, orderTracker, logger);

        // add staff, assign chefs
        staffService.addStaff(new Chef(1, "Ravi"));
        // ... add other staff

        routerService.startAllStations();

        routerService.getStations().forEach((cat, station) -> {
            Chef chef = staffService.findAvailableChef();
            if (chef != null) {
                station.assignChef(chef);
                logger.log("[ASSIGN] " + chef.getName() + " -> " + station.getName());
            }
        });

        dispatchThread = new DispatchThread(priorityQueue, routerService, logger);
        dispatchWorker = new Thread(dispatchThread, "Dispatch-Thread");
        dispatchWorker.start();

        inventoryThread = new Thread(new InventoryMonitorThread(inventoryService, logger), "InventoryMonitor");
        inventoryThread.start();

        reservationThread = new Thread(new ReservationMonitorThread(reservationService, logger), "ReservationMonitor");
        reservationThread.start();

        // idle monitor...
        started = true;
        logger.log("[ENGINE] Started");
    }


    public synchronized void stop() {
        if (!started) return;

        routerService.stopAllStations();

        if (dispatchWorker != null) dispatchWorker.interrupt();
        if (inventoryThread != null) inventoryThread.interrupt();
        if (reservationThread != null) reservationThread.interrupt();
        if (idleMonitorThread != null) idleMonitorThread.interrupt();

        started = false;
        logger.log("[ENGINE] Stopped");
    }

    public void notifyNewOrder(Order order) {
        lastOrderTime = System.currentTimeMillis();
        if (stationsPaused) resumeStationsOnActivity();
    }

    public synchronized void pauseStationsForIdle() {
        if (stationsPaused) return;
        routerService.stopAllStations();
        stationsPaused = true;
        logger.log("[ENGINE] Stations paused due to idle");
    }

    public synchronized void resumeStationsOnActivity() {
        if (!stationsPaused) return;
        routerService.startAllStations();
        stationsPaused = false;
        logger.log("[ENGINE] Stations resumed on activity");
    }

    public long getLastOrderTime() {
        return lastOrderTime;
    }

    public boolean isStationsPaused() {
        return stationsPaused;
    }

    public LoggerService getLogger() { return logger; }

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
