package com.restaurantops.core;

import com.restaurantops.billing.BillingService;
import com.restaurantops.inventory.InventoryInitializer;
import com.restaurantops.inventory.InventoryService;
import com.restaurantops.staff.Chef;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.service.*;
import com.restaurantops.staff.StaffService;
import com.restaurantops.inventory.DeliveryWorkerThread;
import com.restaurantops.inventory.InventoryMonitorThread;
import com.restaurantops.thread.ReservationMonitorThread;
import com.restaurantops.tracking.OrderTracker;
import com.restaurantops.util.LoggerService;

import java.util.concurrent.PriorityBlockingQueue;

public class RestaurantEngine {

    private static final RestaurantEngine INSTANCE = new RestaurantEngine();

    private final LoggerService logger;
    private final OrderTracker orderTracker;
    private final RecipeService recipeService;
    private final MenuService menuService;
    private final InventoryService inventoryService;
    private final BillingService billingService;
    private final ReservationService reservationService;
    private final StaffService staffService;
    private final OrderService orderService;
    private final PriorityBlockingQueue<Order> priorityQueue;
    private KitchenRouterService routerService;

    private DispatchThread dispatchThread;
    private Thread dispatchWorker;

    private SupplierService supplierService;
    private Thread deliveryThread;

    private Thread inventoryThread;
    private Thread reservationThread;
    private Thread idleMonitorThread;

    private long lastOrderTime;
    private volatile boolean stationsPaused = false;
    private boolean started = false;

    private RestaurantEngine() {
        logger = new LoggerService();
        orderTracker = new OrderTracker();
        recipeService = new RecipeService();

        menuService = new MenuService(recipeService);
        inventoryService = new InventoryService(recipeService, logger);

        billingService = new BillingService();
        reservationService = new ReservationService();
        staffService = new StaffService(logger);

        supplierService = new SupplierService(logger);

        priorityQueue = new PriorityBlockingQueue<>(11, new OrderPriorityComparator());
        orderService = new OrderService(priorityQueue, logger);

        lastOrderTime = System.currentTimeMillis();
    }

    public static RestaurantEngine getInstance() {
        return INSTANCE;
    }


    public synchronized void start() {
        if (started) return;

        logger.log("[ENGINE] Starting...");

        java.util.List<MenuItem> menuList = menuService.getAllItems();
        InventoryInitializer.syncMenuToInventory(menuList, inventoryService);
        logger.log("[ENGINE] Inventory auto-loaded from menu");

        inventoryService.setReorderThreshold("dough", 5);
        inventoryService.setReorderThreshold("cheese", 5);
        inventoryService.setReorderThreshold("tomato_sauce", 5);
        inventoryService.setReorderThreshold("toppings", 5);
        inventoryService.setReorderThreshold("bun", 5);
        inventoryService.setReorderThreshold("patty", 5);
        inventoryService.setReorderThreshold("lettuce", 5);
        inventoryService.setReorderThreshold("lemon", 5);
        inventoryService.setReorderThreshold("sugar", 5);
        inventoryService.setReorderThreshold("water", 1);

        routerService = new KitchenRouterService(
                inventoryService,
                billingService,
                orderTracker,
                logger
        );
        routerService.startAllStations();

        staffService.addStaff(new Chef(1, "Ravi"));
        staffService.addStaff(new Chef(2, "Arjun"));
        staffService.addStaff(new Chef(3, "Meera"));

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

        inventoryThread = new Thread(
                new InventoryMonitorThread(
                        inventoryService,
                        supplierService,
                        logger
                ),
                "InventoryMonitor"
        );
        inventoryThread.start();


        deliveryThread = new Thread(
                new DeliveryWorkerThread(supplierService, inventoryService, logger),
                "DeliveryWorker"
        );
        deliveryThread.start();


        reservationThread = new Thread(
                new ReservationMonitorThread(reservationService, logger),
                "ReservationMonitor"
        );
        reservationThread.start();

        idleMonitorThread = new Thread(
                new IdleMonitorThread(
                        this,
                        5000,        // check every 5 seconds
                        300000,      // idle threshold: 5 minutes
                        logger
                ),
                "IdleMonitor"
        );
        idleMonitorThread.start();

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
