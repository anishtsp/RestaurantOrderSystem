package com.restaurantops.kitchen;

import com.restaurantops.staff.Chef;
import com.restaurantops.model.Order;
import com.restaurantops.model.OrderStatus;
import com.restaurantops.billing.BillingService;
import com.restaurantops.inventory.InventoryService;
import com.restaurantops.tracking.OrderTracker;
import com.restaurantops.util.LoggerService;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;

public abstract class AbstractKitchenStation implements KitchenStation {

    protected final BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
    protected final InventoryService inventoryService;
    protected final BillingService billingService;
    protected final OrderTracker orderTracker;
    protected final LoggerService logger;
    private final StationContext context = new StationContext();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService workers;
    private final int workerCount;

    protected AbstractKitchenStation(InventoryService inventoryService,
                                     BillingService billingService,
                                     OrderTracker orderTracker,
                                     LoggerService logger,
                                     int workerCount) {
        this.inventoryService = inventoryService;
        this.billingService = billingService;
        this.orderTracker = orderTracker;
        this.logger = logger;
        this.workerCount = Math.max(1, workerCount);
    }

    public void assignChef(Chef chef) {
        context.assignChef(chef);
        logger.log("[CHEF] " + (chef == null ? "null" : chef.getName()) + " assigned to " + getName() + " (now " + context.chefCount() + ")");
    }

    public void unassignChef(Chef chef) {
        context.unassignChef(chef);
        logger.log("[CHEF] " + (chef == null ? "null" : chef.getName()) + " unassigned from " + getName() + " (now " + context.chefCount() + ")");
    }

    public Chef getAssignedChef() {
        return context.getAssignedChef();
    }

    public List<com.restaurantops.staff.Chef> getAssignedChefs() {
        return context.getAssignedChefs();
    }

    @Override
    public void acceptOrder(Order order) {
        try {
            queue.put(order);
            Chef c = getAssignedChef();
            String chefName = c == null ? "NoChef" : c.getName();
            logger.log("[" + getName() + "][" + chefName + "] Accepted Order#" + order.getOrderId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            workers = Executors.newFixedThreadPool(workerCount, r -> {
                Thread t = new Thread(r);
                t.setName(getName() + "-Worker");
                t.setDaemon(false);
                return t;
            });
            for (int i = 0; i < workerCount; i++) {
                workers.submit(this::workerLoop);
            }
            logger.log("[" + getName() + "] Station started with " + workerCount + " workers and " + context.chefCount() + " assigned chefs");
        }
    }

    private void workerLoop() {
        try {
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                Order order = queue.take();
                updateStatus(order, OrderStatus.ACCEPTED);
                boolean reserved = inventoryService.reserveIngredients(order);
                if (!reserved) {
                    updateStatus(order, OrderStatus.REJECTED);
                    logger.log("[" + getName() + "] Rejected Order#" + order.getOrderId());
                    continue;
                }
                updateStatus(order, OrderStatus.IN_PROGRESS);
                processOrder(order);
                updateStatus(order, OrderStatus.COMPLETED);
                billingService.addOrderToBill(order);
                logger.log("[" + getName() + "] Completed Order#" + order.getOrderId());
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            if (workers != null) {
                workers.shutdownNow();
                try {
                    workers.awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            logger.log("[" + getName() + "] Station stopped");
        }
    }

    @Override
    public int queueSize() {
        return queue.size();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    protected void updateStatus(Order order, OrderStatus status) {
        order.setStatus(status);
        orderTracker.notifyUpdate(order);
    }

    protected abstract void processOrder(Order order) throws InterruptedException;

    @Override
    public abstract String getName();
}
