package com.restaurantops.core;

import com.restaurantops.model.Order;
import com.restaurantops.service.*;
import com.restaurantops.thread.InventoryMonitorThread;
import com.restaurantops.thread.KitchenWorkerThread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RestaurantEngine {

    private static final RestaurantEngine INSTANCE = new RestaurantEngine();

    private final BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();

    private final MenuService menuService = new MenuService();
    private final InventoryService inventoryService = new InventoryService();
    private final BillingService billingService = new BillingService();
    private final OrderService orderService = new OrderService(orderQueue);
    private final KitchenService kitchenService =
            new KitchenService(orderQueue, inventoryService, billingService);

    private Thread kitchenThread;
    private Thread inventoryThread;
    private boolean started = false;

    private RestaurantEngine() {}

    public static RestaurantEngine getInstance() {
        return INSTANCE;
    }

    public synchronized void start() {
        if (started) return;
        System.out.println("[ENGINE] Starting kitchen and inventory threads...");
        kitchenThread = new Thread(new KitchenWorkerThread(kitchenService), "Kitchen-Thread");
        inventoryThread = new Thread(new InventoryMonitorThread(inventoryService), "InventoryMonitor-Thread");
        kitchenThread.start();
        inventoryThread.start();
        started = true;
    }

    public synchronized void stop() {
        if (!started) return;
        System.out.println("[ENGINE] Stopping backend threads...");
        if (kitchenThread != null) kitchenThread.interrupt();
        if (inventoryThread != null) inventoryThread.interrupt();
        started = false;
    }

    public MenuService getMenuService() { return menuService; }
    public InventoryService getInventoryService() { return inventoryService; }
    public BillingService getBillingService() { return billingService; }
    public OrderService getOrderService() { return orderService; }
    public KitchenService getKitchenService() { return kitchenService; }

    public boolean isStarted() { return started; }
}
