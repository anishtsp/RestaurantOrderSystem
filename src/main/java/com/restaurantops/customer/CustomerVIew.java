package com.restaurantops.customer;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.service.MenuService;
import com.restaurantops.service.OrderService;

import java.util.List;
import java.util.Scanner;

public class CustomerView {

    private final MenuService menuService;
    private final OrderService orderService;
    private final Scanner scanner = new Scanner(System.in);

    public CustomerView(RestaurantEngine engine) {
        this.menuService = engine.getMenuService();
        this.orderService = engine.getOrderService();
    }

    public void run() {
        System.out.println("=== Customer View ===");

        boolean running = true;
        while (running) {
            System.out.println("\n1. Browse Menu");
            System.out.println("2. Place Order");
            System.out.println("3. Exit Customer View");
            System.out.print("Choice: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1" -> browseMenu();
                case "2" -> placeOrder();
                case "3" -> {
                    running = false;
                    System.out.println("Exiting Customer View...");
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

            Order order = new Order(table, item, qty);
            orderService.placeOrder(order);
            System.out.println("Your order has been placed: " + order);

        } catch (NumberFormatException e) {
            System.out.println("Invalid number input.");
        }
    }
}
