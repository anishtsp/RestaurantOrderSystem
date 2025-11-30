package com.restaurantops.restaurant;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.Order;
import com.restaurantops.payment.CashPayment;
import com.restaurantops.payment.CardPayment;
import com.restaurantops.payment.UpiPayment;
import com.restaurantops.service.BillingService;
import com.restaurantops.service.InventoryService;
import com.restaurantops.service.OrderService;

import java.util.List;
import java.util.Scanner;

public class RestaurantView {

    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final BillingService billingService;
    private final Scanner scanner = new Scanner(System.in);

    public RestaurantView(RestaurantEngine engine) {
        this.orderService = engine.getOrderService();
        this.inventoryService = engine.getInventoryService();
        this.billingService = engine.getBillingService();
    }

    public void run() {
        System.out.println("=== Restaurant View (Manager/Kitchen Dashboard) ===");

        boolean running = true;
        while (running) {
            System.out.println("\n1. View All Orders");
            System.out.println("2. View Inventory");
            System.out.println("3. View Bills");
            System.out.println("4. Process Payment");
            System.out.println("5. Exit Restaurant View (and stop backend)");
            System.out.print("Choice: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1" -> showOrders();
                case "2" -> inventoryService.printInventory();
                case "3" -> billingService.printAllBills();
                case "4" -> processPayment();
                case "5" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }

        System.out.println("Exiting Restaurant View...");
    }

    private void showOrders() {
        System.out.println("\n=== All Orders ===");
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            System.out.println("No orders yet.");
        } else {
            orders.forEach(System.out::println);
        }
    }

    private void processPayment() {
        try {
            System.out.print("Enter table number: ");
            int table = Integer.parseInt(scanner.nextLine());

            System.out.println("Select payment method:");
            System.out.println("1. Cash");
            System.out.println("2. Card");
            System.out.println("3. UPI");
            System.out.print("Choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> billingService.processPayment(table, new CashPayment());
                case "2" -> billingService.processPayment(table, new CardPayment());
                case "3" -> billingService.processPayment(table, new UpiPayment());
                default -> System.out.println("Invalid payment option.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        }
    }
}
