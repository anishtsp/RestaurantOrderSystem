package com.restaurantops.core;

import com.restaurantops.billing.BillingService;
import com.restaurantops.inventory.DeliveryWorkerThread;
import com.restaurantops.inventory.InventoryInitializer;
import com.restaurantops.inventory.InventoryMonitorThread;
import com.restaurantops.inventory.InventoryService;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.model.Waiter;
import com.restaurantops.service.*;
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

    private TableService tableService;
    private WaiterService waiterService;

    private long lastOrderTime;
    private volatile boolean stationsPaused = false;
    private volatile boolean started = false;

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

        waiterService = new WaiterService(logger);
        tableService = new TableService(reservationService, waiterService, logger);

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
        staffService.addStaff(new Chef(4, "Pooja"));
        staffService.addStaff(new Chef(5, "John"));

        if (this.getTableService().listTables().isEmpty()) {
            for (int t = 1; t <= 10; t++) {
                this.getTableService().addTable(t);
            }
            logger.log("[ENGINE] Preloaded 10 tables");
        }

        if (this.getWaiterService().getWaiters().isEmpty()) {
            this.getWaiterService().addWaiter(101, "Amit");
            this.getWaiterService().addWaiter(102, "Priya");
            this.getWaiterService().addWaiter(103, "Karan");
            this.getWaiterService().addWaiter(104, "Kumar");
            this.getWaiterService().addWaiter(105, "Arya");
            logger.log("[ENGINE] Preloaded 5 waiters");
        }

        for (int t = 1; t <= 10; t++) {
            com.restaurantops.model.Waiter w = this.getWaiterService().assignNextAvailableWaiter();
            if (w != null) {
                this.getTableService().assignWaiterToTable(t, w);
                logger.log("[ASSIGN] Waiter " + w.getName() + " -> Table " + t);
            }
        }



        // Assign one chef per station in a round-robin manner (prevents duplicate-first-chef problem)
        var chefList = staffService.getAllStaff().stream()
                .filter(s -> s instanceof Chef)
                .map(s -> (Chef) s)
                .toList();

        var stationList = routerService.getStations().values().stream().toList();

        if (!chefList.isEmpty() && !stationList.isEmpty()) {
            for (int i = 0; i < stationList.size(); i++) {
                var st = stationList.get(i);
                var chef = chefList.get(i % chefList.size());
                st.assignChef(chef);
                logger.log("[ASSIGN] " + chef.getName() + " -> " + st.getName());
            }
        } else {
            logger.log("[ASSIGN] No chefs or no stations available for assignment.");
        }



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
                        5000,
                        TimeUnit.MINUTES.toMillis(5),
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

        if (dispatchWorker != null) dispatchWorker.interrupt();

        if (routerService != null) routerService.stopAllStations();

        if (inventoryThread != null) inventoryThread.interrupt();
        if (deliveryThread != null) deliveryThread.interrupt();
        if (reservationThread != null) reservationThread.interrupt();
        if (idleMonitorThread != null) idleMonitorThread.interrupt();

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

    public MenuService getMenuService() {
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

    public TableService getTableService() {
        return tableService;
    }

    public WaiterService getWaiterService() {
        return waiterService;
    }

    public boolean isStarted() {
        return started;
    }
}
