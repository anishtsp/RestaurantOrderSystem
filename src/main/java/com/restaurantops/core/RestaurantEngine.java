package com.restaurantops.core;

import com.restaurantops.model.Order;
import com.restaurantops.service.*;
import com.restaurantops.thread.InventoryMonitorThread;
import com.restaurantops.thread.KitchenWorkerThread;
import com.restaurantops.thread.ReservationMonitorThread;
import com.restaurantops.tracking.OrderTracker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RestaurantEngine {

    private static final RestaurantEngine INSTANCE = new RestaurantEngine();

    private final BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();

    private final OrderTracker orderTracker = new OrderTracker();
    private final MenuService menuService = new MenuService();
    private final InventoryService inventoryService = new InventoryService();
    private final BillingService billingService = new BillingService();
    private final ReservationService reservationService = new ReservationService();
    private final OrderService orderService = new OrderService(orderQueue);
    private final KitchenService kitchenService =
            new KitchenService(orderQueue, inventoryService, billingService, orderTracker);

    private Thread kitchenThread;
    private Thread inventoryThread;
    private Thread reservationThread;
    private boolean started = false;

    private RestaurantEngine() {}

    public static RestaurantEngine getInstance() {
        return INSTANCE;
    }

    public synchronized void start() {
        if (started) return;
        System.out.println("[ENGINE] Starting kitchen, inventory and reservation threads...");
        kitchenThread = new Thread(new KitchenWorkerThread(kitchenService), "Kitchen-Thread");
        inventoryThread = new Thread(new InventoryMonitorThread(inventoryService), "InventoryMonitor-Thread");
        reservationThread = new Thread(new ReservationMonitorThread(reservationService), "ReservationMonitor-Thread");
        kitchenThread.start();
        inventoryThread.start();
        reservationThread.start();
        started = true;
    }

    public synchronized void stop() {
        if (!started) return;
        System.out.println("[ENGINE] Stopping backend threads...");
        if (kitchenThread != null) kitchenThread.interrupt();
        if (inventoryThread != null) inventoryThread.interrupt();
        if (reservationThread != null) reservationThread.interrupt();
        started = false;
    }

    public OrderTracker getOrderTracker() { return orderTracker; }
    public MenuService getMenuService() { return menuService; }
    public InventoryService getInventoryService() { return inventoryService; }
    public BillingService getBillingService() { return billingService; }
    public ReservationService getReservationService() { return reservationService; }
    public OrderService getOrderService() { return orderService; }
    public KitchenService getKitchenService() { return kitchenService; }

    public boolean isStarted() { return started; }
}
