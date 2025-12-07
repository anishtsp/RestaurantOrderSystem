package com.restaurantops.service;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.Order;
import com.restaurantops.model.Waiter;
import com.restaurantops.util.LoggerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public class OrderService {

    private final PriorityBlockingQueue<Order> orderQueue;
    private final List<Order> allOrders = Collections.synchronizedList(new ArrayList<>());
    private final LoggerService logger;

    private TableService tableService;
    private WaiterService waiterService;

    public OrderService(PriorityBlockingQueue<Order> orderQueue,
                        LoggerService logger) {
        this.orderQueue = orderQueue;
        this.logger = logger;
    }

    public void setTableService(TableService tableService) {
        this.tableService = tableService;
    }

    public void setWaiterService(WaiterService waiterService) {
        this.waiterService = waiterService;
    }

    public void placeOrder(Order order) {
        try {
            allOrders.add(order);
            orderQueue.put(order);
            logger.log("[ORDER] Placed Order#" + order.getOrderId());
            RestaurantEngine.getInstance().notifyNewOrder(order);
            if (tableService != null && waiterService != null) {
                tableService.getTable(order.getTableNumber()).ifPresent(t -> t.getAssignedWaiter().ifPresent(w -> {
                    waiterService.updateLoad(w.getWaiterId(), order.getQuantity());
                    waiterService.rebalanceWaiters();
                }));
            }
        } catch (Exception e) {
            logger.log("[ORDER] ERROR placing order: " + e.getMessage());
        }
    }

    public List<Order> getAllOrders() {
        return allOrders;
    }
}
