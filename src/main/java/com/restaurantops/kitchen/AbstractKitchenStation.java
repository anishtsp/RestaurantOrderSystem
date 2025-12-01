package com.restaurantops.kitchen;

import com.restaurantops.model.Chef;
import com.restaurantops.model.Order;
import com.restaurantops.model.OrderStatus;
import com.restaurantops.service.BillingService;
import com.restaurantops.service.InventoryService;
import com.restaurantops.service.LoggerService;
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

    private final StationContext context = new StationContext();
    private Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(false);

    protected AbstractKitchenStation(InventoryService inventoryService,
                                     BillingService billingService,
                                     OrderTracker orderTracker,
                                     LoggerService logger) {
        this.inventoryService = inventoryService;
        this.billingService = billingService;
        this.orderTracker = orderTracker;
        this.logger = logger;
    }

    public void assignChef(Chef chef) {
        context.assignChef(chef);
        logger.log("[CHEF] " + chef.getName() + " assigned to " + getName());
    }

    public Chef getAssignedChef() {
        return context.getAssignedChef();
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
    public int queueSize() {
        return queue.size();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Order order = queue.take();

                order.setStatus(OrderStatus.ACCEPTED);
                orderTracker.notifyUpdate(order);

                boolean reserved = inventoryService.reserveIngredients(order);
                if (!reserved) {
                    order.setStatus(OrderStatus.REJECTED);
                    orderTracker.notifyUpdate(order);
                    logger.log("[" + getName() + "] Rejected Order#" + order.getOrderId());
                    continue;
                }

                order.setStatus(OrderStatus.IN_PROGRESS);
                orderTracker.notifyUpdate(order);

                processOrder(order);

                order.setStatus(OrderStatus.COMPLETED);
                orderTracker.notifyUpdate(order);

                billingService.addItemToBill(order.getTableNumber(), order.getItem(), order.getQuantity());

                logger.log("[" + getName() + "] Completed Order#" + order.getOrderId());
            }
        } catch (InterruptedException ignored) {
        } finally {
            running.set(false);
        }
    }

    protected abstract void processOrder(Order order) throws InterruptedException;

    @Override
    public abstract String getName();
}
