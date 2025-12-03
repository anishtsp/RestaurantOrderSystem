package com.restaurantops.kitchen;

import com.restaurantops.staff.Chef;
import com.restaurantops.model.Order;
import com.restaurantops.model.OrderStatus;
import com.restaurantops.billing.BillingService;
import com.restaurantops.inventory.InventoryService;
import com.restaurantops.util.LoggerService;
import com.restaurantops.tracking.OrderTracker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractKitchenStation implements KitchenStation, Runnable {

    protected final BlockingQueue<Order> queue = new LinkedBlockingQueue<>();

    protected final InventoryService inventoryService;
    protected final BillingService billingService;
    protected final OrderTracker orderTracker;
    protected final LoggerService logger;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread worker;

    private final StationContext context = new StationContext();

    protected AbstractKitchenStation(InventoryService inventoryService,
                                     BillingService billingService,
                                     OrderTracker orderTracker,
                                     LoggerService logger) {
        this.inventoryService = inventoryService;
        this.billingService = billingService;
        this.orderTracker = orderTracker;
        this.logger = logger;
    }

    @Override
    public void assignChef(Chef chef) {
        context.assignChef(chef);
        logger.log("[CHEF] " + chef.getName() + " assigned to " + getName());
    }

    @Override
    public Chef getAssignedChef() {
        return context.getAssignedChef();
    }

    @Override
    public void acceptOrder(Order order) {
        try {
            queue.put(order);
            Chef chef = getAssignedChef();
            logger.log("[" + getName() + "][" + (chef == null ? "NoChef" : chef.getName()) +
                    "] Accepted Order#" + order.getOrderId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            worker = new Thread(this, getName() + "-Worker");
            worker.start();
            logger.log("[" + getName() + "] Station started");
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            if (worker != null) worker.interrupt();
            logger.log("[" + getName() + "] Station stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int queueSize() {
        return queue.size();
    }

    @Override
    public void run() {
        try {
            while (running.get() && !Thread.currentThread().isInterrupted()) {

                Order order = queue.take();

                // === ACCEPTED ===
                updateStatus(order, OrderStatus.ACCEPTED);

                // === INGREDIENT CHECK ===
                boolean ok = inventoryService.reserveIngredients(order);
                if (!ok) {
                    updateStatus(order, OrderStatus.REJECTED);
                    logger.log("[" + getName() + "] Rejected Order#" + order.getOrderId());
                    continue;
                }

                // === IN PROGRESS ===
                updateStatus(order, OrderStatus.IN_PROGRESS);

                // actual cooking simulation
                processOrder(order);

                // === COMPLETED ===
                updateStatus(order, OrderStatus.COMPLETED);

                billingService.addOrderToBill(order);

                logger.log("[" + getName() + "] Completed Order#" + order.getOrderId());
            }

        } catch (InterruptedException ignored) {
        } finally {
            running.set(false);
        }
    }

    private void updateStatus(Order order, OrderStatus status) {
        order.setStatus(status);
        orderTracker.notifyUpdate(order);
    }

    protected abstract void processOrder(Order order) throws InterruptedException;

    @Override
    public abstract String getName();
}
