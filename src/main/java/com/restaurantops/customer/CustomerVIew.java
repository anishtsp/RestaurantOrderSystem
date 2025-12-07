package com.restaurantops.customer;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.model.OrderStatus;
import com.restaurantops.service.MenuService;
import com.restaurantops.service.OrderService;
import com.restaurantops.service.ReservationService;
import com.restaurantops.service.TableService;
import com.restaurantops.tracking.OrderListener;
import com.restaurantops.tracking.OrderTracker;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class CustomerView {

    private final MenuService menuService;
    private final OrderService orderService;
    private final ReservationService reservationService;
    private final TableService tableService;
    private final OrderTracker orderTracker;
    private final Scanner scanner = new Scanner(System.in);

    private Integer lastOrderedTable = null;

    public CustomerView(RestaurantEngine engine) {
        this.menuService = engine.getMenuService();
        this.orderService = engine.getOrderService();
        this.reservationService = engine.getReservationService();
        this.tableService = engine.getTableService();
        this.orderTracker = engine.getOrderTracker();

        this.orderTracker.addListener(new OrderListener() {
            @Override
            public void onOrderUpdated(Order order) {
                handleOrderUpdate(order);
            }
        });
    }

    public void run() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== CUSTOMER VIEW ===");
            System.out.println("1. Browse Menu");
            System.out.println("2. Place Order");
            System.out.println("3. Reserve Table");
            System.out.println("4. Occupy Reserved Table (by Reservation ID)");
            System.out.println("5. Occupy Any Free Table");
            System.out.println("6. Exit");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> browseMenu();
                case "2" -> placeOrder();
                case "3" -> reserveTable();
                case "4" -> occupyReserved();
                case "5" -> occupyAnyFree();
                case "6" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void browseMenu() {
        System.out.println("\n=== MENU ===");
        List<MenuItem> items = menuService.getAllItems();
        items.forEach(System.out::println);
    }

    private void placeOrder() {
        try {
            System.out.print("Enter table number: ");
            int table = Integer.parseInt(scanner.nextLine());

            lastOrderedTable = table;

            browseMenu();

            System.out.print("Enter item id: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter quantity: ");
            int qty = Integer.parseInt(scanner.nextLine());

            MenuItem item = menuService.getById(id);
            if (item == null) {
                System.out.println("Invalid item.");
                return;
            }

            Order order = new Order(table, item, qty);
            orderService.placeOrder(order);

            System.out.println("Order placed: " + order);

        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    private void reserveTable() {
        try {
            System.out.print("Table number: ");
            int t = Integer.parseInt(scanner.nextLine());

            System.out.print("Start offset minutes: ");
            int offset = Integer.parseInt(scanner.nextLine());

            System.out.print("Duration minutes: ");
            int dur = Integer.parseInt(scanner.nextLine());

            LocalDateTime start = LocalDateTime.now().plusMinutes(offset);
            LocalDateTime end = start.plusMinutes(dur);

            var r = reservationService.reserveTable(t, start, end);

            if (r == null) {
                System.out.println("Reservation failed.");
            } else {
                System.out.println("Reservation created! ID: " + r.getReservationId());
            }

        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void occupyReserved() {
        try {
            System.out.print("Reservation ID: ");
            int id = Integer.parseInt(scanner.nextLine());

            boolean ok = tableService.occupyReservedTable(id);

            if (ok) System.out.println("Table occupied.");
            else System.out.println("Invalid reservation or table not available.");

        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void occupyAnyFree() {
        Integer t = tableService.occupyAnyFreeTable();
        if (t == null) {
            System.out.println("No free tables.");
        } else {
            System.out.println("You are seated at table " + t);
        }
    }

    private void handleOrderUpdate(Order order) {
        if (lastOrderedTable == null) return;
        if (order.getTableNumber() != lastOrderedTable) return;

        System.out.println();

        switch (order.getStatus()) {
            case ACCEPTED ->
                    System.out.println("📦 Order #" + order.getOrderId() + " accepted.");
            case IN_PROGRESS ->
                    System.out.println("⏳ Order #" + order.getOrderId() + " being prepared.");
            case COMPLETED ->
                    System.out.println("✅ Order #" + order.getOrderId() + " ready!");
            case REJECTED ->
                    System.out.println("❌ Order #" + order.getOrderId() + " was rejected.");
        }
    }
}
