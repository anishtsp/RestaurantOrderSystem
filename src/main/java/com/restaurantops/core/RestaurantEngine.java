package com.restaurantops.core;

import com.restaurantops.billing.BillingService;
import com.restaurantops.inventory.DeliveryWorkerThread;
import com.restaurantops.inventory.InventoryInitializer;
import com.restaurantops.inventory.InventoryMonitorThread;
import com.restaurantops.inventory.InventoryService;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.service.RecipeService;
import com.restaurantops.core.DispatchThread;
import com.restaurantops.service.KitchenRouterService;
import com.restaurantops.service.OrderPriorityComparator;
import com.restaurantops.service.OrderService;
import com.restaurantops.service.ReservationService;
import com.restaurantops.service.SupplierService;
import com.restaurantops.staff.Chef;
import com.restaurantops.staff.StaffService;
import com.restaurantops.thread.ReservationMonitorThread;
import com.restaurantops.tracking.OrderTracker;
import com.restaurantops.util.LoggerService;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RestaurantEngine {

    private static final RestaurantEngine INSTANCE = new RestaurantEngine();

    private final LoggerService logger;
    private final OrderTracker orderTracker;
    private final RecipeService recipeService;
    private final com.restaurantops.service.MenuService menuService;
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
    private volatile boolean started = false;

    private RestaurantEngine() {
        logger = new LoggerService();
        orderTracker = new OrderTracker();
        recipeService = new RecipeService();

        menuService = new com.restaurantops.service.MenuService(recipeService);
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

        List<MenuItem> menuList = menuService.getAllItems();
        InventoryInitializer.syncMenuToInventory(menuList, inventoryService);
        logger.log("[ENGINE] Inventory auto-loaded from menu");

        // sensible defaults
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
                        TimeUnit.MINUTES.toMillis(5), // idle threshold: 5 minutes
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

        logger.log("[ENGINE] Stopping...");

        // stop accepting new work
        if (dispatchWorker != null) dispatchWorker.interrupt();

        // stop stations first (prevents new billing)
        if (routerService != null) routerService.stopAllStations();

        // interrupt background threads
        if (inventoryThread != null) inventoryThread.interrupt();
        if (deliveryThread != null) deliveryThread.interrupt();
        if (reservationThread != null) reservationThread.interrupt();
        if (idleMonitorThread != null) idleMonitorThread.interrupt();

        // attempt graceful joins (best-effort)
        try {
            if (dispatchWorker != null) dispatchWorker.join(500);
            if (inventoryThread != null) inventoryThread.join(500);
            if (deliveryThread != null) deliveryThread.join(500);
            if (reservationThread != null) reservationThread.join(500);
            if (idleMonitorThread != null) idleMonitorThread.join(500);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        started = false;
        logger.log("[ENGINE] Stopped");
    }

    /**
     * Called when a new order arrives into system (used by OrderService)
     */
    public void notifyNewOrder(Order order) {
        Objects.requireNonNull(order);
        lastOrderTime = System.currentTimeMillis();
        if (stationsPaused) resumeStationsOnActivity();
    }

    public synchronized void pauseStationsForIdle() {
        if (stationsPaused) return;
        if (routerService != null) routerService.stopAllStations();
        stationsPaused = true;
        logger.log("[ENGINE] Stations paused due to idle");
    }

    public synchronized void resumeStationsOnActivity() {
        if (!stationsPaused) return;
        if (routerService != null) routerService.startAllStations();
        stationsPaused = false;
        logger.log("[ENGINE] Stations resumed on activity");
    }

    public long getLastOrderTime() {
        return lastOrderTime;
    }

    public boolean isStationsPaused() {
        return stationsPaused;
    }

    public LoggerService getLogger() {
        return logger;
    }

    public com.restaurantops.service.MenuService getMenuService() {
        return menuService;
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    public BillingService getBillingService() {
        return billingService;
    }

    public ReservationService getReservationService() {
        return reservationService;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public StaffService getStaffService() {
        return staffService;
    }

    public OrderTracker getOrderTracker() {
        return orderTracker;
    }

    public KitchenRouterService getRouterService() {
        return routerService;
    }

    public boolean isStarted() {
        return started;
    }
}
