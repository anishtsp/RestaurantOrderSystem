package com.restaurantops.model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class Order {
    private static final AtomicInteger ID_GEN = new AtomicInteger(1);

    private final int orderId;
    private final int tableNumber;
    private final MenuItem item;
    private final int quantity;
    private final LocalDateTime timestamp;
    private OrderStatus status;

    public Order(int tableNumber, MenuItem item, int quantity) {
        this.orderId = ID_GEN.getAndIncrement();
        this.tableNumber = tableNumber;
        this.item = item;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
        this.status = OrderStatus.NEW;
    }

    public int getOrderId() { return orderId; }
    public int getTableNumber() { return tableNumber; }
    public MenuItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Order#" + orderId +
                " | Table " + tableNumber +
                " | " + item.getName() +
                " x" + quantity +
                " | Status: " + status;
    }
}
