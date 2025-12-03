package com.restaurantops.customer;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.Customization;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.model.OrderStatus;
import com.restaurantops.service.MenuService;
import com.restaurantops.service.OrderService;
import com.restaurantops.service.ReservationService;
import com.restaurantops.tracking.OrderListener;
import com.restaurantops.tracking.OrderTracker;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class CustomerView {

    private final MenuService menuService;
    private final OrderService orderService;
    private final ReservationService reservationService;
    private final OrderTracker orderTracker;

    private final Scanner scanner = new Scanner(System.in);
    private Integer lastTable = null;

    public CustomerView(RestaurantEngine engine) {
        this.menuService = engine.getMenuService();
        this.orderService = engine.getOrderService();
        this.reservationService = engine.getReservationService();
        this.orderTracker = engine.getOrderTracker();

        // auto subscribe to order updates (filtered per table)
        this.orderTracker.addListener(new OrderListener() {
            @Override
            public void onOrderUpdated(Order order) {
                handleOrderUpdate(order);
            }
        });
    }

    public void run() {
        boolean running = true;

        System.out.println("=== Customer View ===");

        while (running) {
            System.out.println("\n1. Browse Menu");
            System.out.println("2. Place Order");
            System.out.println("3. View My Order Status");
            System.out.println("4. Reserve Table");
            System.out.println("5. View My Reservations");
            System.out.println("6. Exit Customer View");
            System.out.print("Choice: ");

            String input = scanner.nextLine();
            switch (input) {
                case "1" -> browseMenu();
                case "2" -> placeOrder();
                case "3" -> viewOrderStatus();
                case "4" -> reserveTable();
                case "5" -> viewReservations();
                case "6" -> {
                    System.out.println("Exiting Customer View...");
                    running = false;
                }
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
            lastTable = table;

            browseMenu();
            System.out.print("Enter menu item id: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Quantity: ");
            int qty = Integer.parseInt(scanner.nextLine());

            MenuItem item = menuService.getById(id);
            if (item == null) {
                System.out.println("Invalid item id.");
                return;
            }

            System.out.print("Extra cheese? (yes/no): ");
            boolean cheese = scanner.nextLine().equalsIgnoreCase("yes");

            System.out.print("Spice level (1-5): ");
            int spice = Integer.parseInt(scanner.nextLine());

            System.out.print("Toppings (comma-separated): ");
            String toppings = scanner.nextLine();

            Customization c = new Customization(cheese, spice, toppings);

            Order order = new Order(table, item, qty, c);
            orderService.placeOrder(order);

            System.out.println("Order placed successfully!");
            System.out.println(order);

        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    private void viewOrderStatus() {
        if (lastTable == null) {
            System.out.println("You have not placed any orders yet.");
            return;
        }

        System.out.println("\n=== Your Orders (Table " + lastTable + ") ===");
        orderService.getAllOrders().stream()
                .filter(o -> o.getTableNumber() == lastTable)
                .forEach(o -> System.out.println(
                        "Order#" + o.getOrderId() +
                                " - " + o.getItem().getName() +
                                " - Status: " + o.getStatus()
                ));
    }

    private void reserveTable() {
        try {
            System.out.print("Enter table number (1–10): ");
            int table = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter your name: ");
            String name = scanner.nextLine();

            System.out.print("Start time offset (minutes from now): ");
            int offset = Integer.parseInt(scanner.nextLine());

            System.out.print("Duration (minutes): ");
            int duration = Integer.parseInt(scanner.nextLine());

            var start = LocalDateTime.now().plusMinutes(offset);
            var end = start.plusMinutes(duration);

            var r = reservationService.reserveTable(table, name, start, end);

            if (r == null)
                System.out.println("Table not available!");
            else
                System.out.println("Reservation confirmed:\n" + r);

        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    private void viewReservations() {
        System.out.println("\n=== My Reservations ===");
        reservationService.getAllReservations().forEach(System.out::println);
    }

    private void handleOrderUpdate(Order order) {
        if (lastTable == null) return;
        if (order.getTableNumber() != lastTable) return;

        System.out.println();

        if (order.getStatus() == OrderStatus.REJECTED) {
            System.out.println("❌ Your order #" + order.getOrderId() + " was REJECTED.");
            return;
        }

        switch (order.getStatus()) {
            case ACCEPTED -> System.out.println("📦 Order #" + order.getOrderId() + " accepted by kitchen.");
            case IN_PROGRESS -> System.out.println("⏳ Order #" + order.getOrderId() + " is being prepared.");
            case COMPLETED -> System.out.println("✅ Order #" + order.getOrderId() + " is ready!");
        }
    }
}
