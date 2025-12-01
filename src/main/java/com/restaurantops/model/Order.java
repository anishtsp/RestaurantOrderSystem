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
    private final Customization customization;
    private OrderStatus status;
    private OrderCategory category = OrderCategory.UNKNOWN;
    private int priorityScore = 0;

    public Order(int tableNumber, MenuItem item, int quantity, Customization customization) {
        this.orderId = ID_GEN.getAndIncrement();
        this.tableNumber = tableNumber;
        this.item = item;
        this.quantity = Math.max(1, quantity);
        this.customization = customization;
        this.timestamp = LocalDateTime.now();
        this.status = OrderStatus.NEW;
    }

    public Order(int tableNumber, MenuItem item, int quantity, Customization customization, OrderCategory category) {
        this(tableNumber, item, quantity, customization);
        this.category = category == null ? OrderCategory.UNKNOWN : category;
    }

    public int getOrderId() { return orderId; }
    public int getTableNumber() { return tableNumber; }
    public MenuItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Customization getCustomization() { return customization; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public OrderCategory getCategory() { return category; }
    public void setCategory(OrderCategory category) { this.category = category; }

    public int getPriorityScore() { return priorityScore; }
    public void setPriorityScore(int priorityScore) { this.priorityScore = priorityScore; }

    @Override
    public String toString() {
        return "Order#" + orderId +
                " | Table " + tableNumber +
                " | " + item.getName() +
                " x" + quantity +
                (customization != null ? " " + customization : "") +
                " | Cat=" + category +
                " | Status: " + status;
    }
}
