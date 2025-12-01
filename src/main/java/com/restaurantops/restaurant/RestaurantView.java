package com.restaurantops.restaurant;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.kitchen.AbstractKitchenStation;
import com.restaurantops.kitchen.KitchenStation;
import com.restaurantops.model.Order;
import com.restaurantops.model.Staff;
import com.restaurantops.payment.CashPayment;
import com.restaurantops.payment.CardPayment;
import com.restaurantops.payment.UpiPayment;
import com.restaurantops.service.BillingService;
import com.restaurantops.service.InventoryService;
import com.restaurantops.service.OrderService;
import com.restaurantops.service.ReservationService;
import com.restaurantops.service.StaffService;
import com.restaurantops.service.KitchenRouterService;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class RestaurantView {

    private final RestaurantEngine engine;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final BillingService billingService;
    private final ReservationService reservationService;
    private final StaffService staffService;
    private final KitchenRouterService routerService;
    private final Scanner scanner = new Scanner(System.in);

    public RestaurantView(RestaurantEngine engine) {
        this.engine = engine;
        this.orderService = engine.getOrderService();
        this.inventoryService = engine.getInventoryService();
        this.billingService = engine.getBillingService();
        this.reservationService = engine.getReservationService();
        this.staffService = engine.getStaffService();
        this.routerService = engine.getRouterService();
    }

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Restaurant View (Manager/Kitchen Dashboard) ===");
            System.out.println("1. View All Orders");
            System.out.println("2. View Inventory");
            System.out.println("3. View Bills");
            System.out.println("4. Process Payment");
            System.out.println("5. View Reservations");
            System.out.println("6. View Staff");
            System.out.println("7. View Kitchen Stations Status");
            System.out.println("8. Exit Restaurant View");
            System.out.print("Choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> showOrders();
                case "2" -> inventoryService.printInventory();
                case "3" -> billingService.printAllBills();
                case "4" -> processPayment();
                case "5" -> showReservations();
                case "6" -> showStaff();
                case "7" -> showStations();
                case "8" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
        System.out.println("Exiting Restaurant View...");
    }

    private void showOrders() {
        List<Order> orders = orderService.getAllOrders();
        System.out.println("\n=== All Orders ===");
        if (orders.isEmpty()) {
            System.out.println("No orders yet.");
            return;
        }
        for (Order o : orders) {
            System.out.println(o);
        }
    }

    private void showReservations() {
        System.out.println("\n=== Reservations ===");
        reservationService.getAllReservations().forEach(System.out::println);
    }

    private void showStaff() {
        System.out.println("\n=== Staff ===");
        List<Staff> list = staffService.getAllStaff();
        if (list.isEmpty()) {
            System.out.println("No staff registered.");
            return;
        }
        for (Staff s : list) {
            System.out.println(s.getStaffId() + " | " + s.getName() + " | " + s.getRole());
        }
    }

    @SuppressWarnings("unchecked")
    private void showStations() {
        System.out.println("\n=== Kitchen Stations Status ===");
        try {
            Method m = routerService.getClass().getMethod("getStations");
            Object result = m.invoke(routerService);
            if (result instanceof Map) {
                Map<Object, Object> map = (Map<Object, Object>) result;
                for (Map.Entry<Object, Object> e : map.entrySet()) {
                    Object stationObj = e.getValue();
                    String name = stationObj instanceof KitchenStation
                            ? ((KitchenStation) stationObj).getName()
                            : stationObj.getClass().getSimpleName();
                    int q = -1;
                    boolean running = false;
                    String chefName = "None";
                    if (stationObj instanceof KitchenStation) {
                        KitchenStation ks = (KitchenStation) stationObj;
                        q = ks.queueSize();
                        running = ks.isRunning();
                        if (ks instanceof AbstractKitchenStation) {
                            AbstractKitchenStation aks = (AbstractKitchenStation) ks;
                            if (aks.getAssignedChef() != null)
                                chefName = aks.getAssignedChef().getName();
                        }
                    }
                    System.out.println("Station: " + name + " | Queue: " + q + " | Running: " + running + " | Chef: " + chefName);
                }
            } else {
                System.out.println("Stations unavailable.");
            }
        } catch (Exception ex) {
            System.out.println("Error displaying stations: " + ex.getMessage());
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
            String method = scanner.nextLine();
            switch (method) {
                case "1" -> billingService.processPayment(table, new CashPayment());
                case "2" -> billingService.processPayment(table, new CardPayment());
                case "3" -> billingService.processPayment(table, new UpiPayment());
                default -> System.out.println("Invalid payment option.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }
}
