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

    // needed to filter updates for the current customer
    private Integer lastOrderedTable = null;

    public CustomerView(RestaurantEngine engine) {
        this.menuService = engine.getMenuService();
        this.orderService = engine.getOrderService();
        this.reservationService = engine.getReservationService();
        this.orderTracker = engine.getOrderTracker();

        // ⭐ Auto-subscribe for real-time status updates
        this.orderTracker.addListener(new OrderListener() {
            @Override
            public void onOrderUpdated(Order order) {
                handleOrderUpdate(order);
            }
        });
    }

    public void run() {
        System.out.println("=== Customer View ===");

        boolean running = true;
        while (running) {
            System.out.println("\n1. Browse Menu");
            System.out.println("2. Place Order");
            System.out.println("3. Exit Customer View");
            System.out.println("4. Reserve Table");
            System.out.println("5. Subscribe to Order Updates (temporary)");
            System.out.print("Choice: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1" -> browseMenu();
                case "2" -> placeOrder();
                case "3" -> {
                    running = false;
                    System.out.println("Exiting Customer View...");
                }
                case "4" -> reserveTable();
                case "5" -> subscribeTemp();
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

            // keep this so the listener can filter updates
            lastOrderedTable = table;

            browseMenu();
            System.out.print("Enter menu item id: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter quantity: ");
            int qty = Integer.parseInt(scanner.nextLine());

            MenuItem item = menuService.getById(id);
            if (item == null) {
                System.out.println("Invalid menu item id.");
                return;
            }

            System.out.print("Extra cheese? (yes/no): ");
            boolean cheese = scanner.nextLine().equalsIgnoreCase("yes");

            System.out.print("Spice level (1-5): ");
            int spice = Integer.parseInt(scanner.nextLine());

            System.out.print("Toppings (comma separated): ");
            String toppings = scanner.nextLine();

            Customization c = new Customization(cheese, spice, toppings);
            Order order = new Order(table, item, qty, c);
            orderService.placeOrder(order);
            System.out.println("Your order has been placed: " + order);

        } catch (NumberFormatException e) {
            System.out.println("Invalid number input.");
        }
    }

    private void reserveTable() {
        try {
            System.out.print("Enter table number (1-10): ");
            int table = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter your name: ");
            String name = scanner.nextLine();

            System.out.print("Start time offset (minutes from now): ");
            int startOffset = Integer.parseInt(scanner.nextLine());

            System.out.print("Duration (minutes): ");
            int duration = Integer.parseInt(scanner.nextLine());

            var start = LocalDateTime.now().plusMinutes(startOffset);
            var end = start.plusMinutes(duration);

            var r = reservationService.reserveTable(table, name, start, end);

            if (r == null) System.out.println("Table not available!");
            else System.out.println("Reservation confirmed:\n" + r);

        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    private void subscribeTemp() {
        System.out.print("Enter order id to subscribe (or press enter to subscribe all): ");
        String s = scanner.nextLine();
        OrderListener l = new OrderListener() {
            @Override
            public void onOrderUpdated(Order order) {
                System.out.println("\n[TRACKING] Update: " + order);
            }
        };
        orderTracker.addListener(l);
        System.out.println("Subscribed to order updates (temporary for this session).");
    }

    // ⭐ NEW — automatically shows REJECTED, IN_PROGRESS, ACCEPTED, COMPLETED
    private void handleOrderUpdate(Order order) {
        // filter updates — only show updates for the customer's own table
        if (lastOrderedTable == null) return;
        if (order.getTableNumber() != lastOrderedTable) return;

        System.out.println(); // spacing

        if (order.getStatus() == OrderStatus.REJECTED) {
            System.out.println("❌ Your order #" + order.getOrderId() + " was REJECTED.");
            System.out.println("Reason: item out of stock / expired.");
            return;
        }

        if (order.getStatus() == OrderStatus.ACCEPTED) {
            System.out.println("📦 Order #" + order.getOrderId() + " has been accepted by the kitchen.");
        }

        if (order.getStatus() == OrderStatus.IN_PROGRESS) {
            System.out.println("⏳ Order #" + order.getOrderId() + " is now being prepared.");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            System.out.println("✅ Order #" + order.getOrderId() + " is READY! Please collect.");
        }
    }
}
