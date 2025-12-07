package com.restaurantops.restaurant;

import com.restaurantops.billing.BillingService;
import com.restaurantops.billing.payment.CardPayment;
import com.restaurantops.billing.payment.CashPayment;
import com.restaurantops.billing.payment.PaymentMethod;
import com.restaurantops.billing.payment.UpiPayment;
import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.inventory.InventoryService;
import com.restaurantops.model.*;
import com.restaurantops.service.*;
import com.restaurantops.staff.Chef;
import com.restaurantops.staff.Staff;
import com.restaurantops.staff.StaffService;
import com.restaurantops.util.LoggerService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class RestaurantView {

    private final RestaurantEngine engine;
    private final MenuService menuService;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final BillingService billingService;
    private final ReservationService reservationService;
    private final StaffService staffService;
    private final KitchenRouterService routerService;
    private final TableService tableService;
    private final WaiterService waiterService;
    private final LoggerService logger;

    private final Scanner scanner = new Scanner(System.in);

    public RestaurantView(RestaurantEngine engine) {
        this.engine = engine;
        this.menuService = engine.getMenuService();
        this.orderService = engine.getOrderService();
        this.inventoryService = engine.getInventoryService();
        this.billingService = engine.getBillingService();
        this.reservationService = engine.getReservationService();
        this.staffService = engine.getStaffService();
        this.routerService = engine.getRouterService();
        this.tableService = engine.getTableService();
        this.waiterService = engine.getWaiterService();
        this.logger = engine.getLogger();
    }

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== RESTAURANT DASHBOARD ===");
            System.out.println("1. Kitchen & Staff");
            System.out.println("2. Inventory & Supplier");
            System.out.println("3. Billing & Payments");
            System.out.println("4. Orders");
            System.out.println("5. Reservations");
            System.out.println("6. Tables & Waiters");
            System.out.println("7. System Diagnostics");
            System.out.println("8. Exit");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> kitchenAndStaffMenu();
                case "2" -> inventoryMenu();
                case "3" -> billingMenu();
                case "4" -> ordersMenu();
                case "5" -> reservationsMenu();
                case "6" -> tablesAndWaitersMenu();
                case "7" -> diagnosticsMenu();
                case "8" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void kitchenAndStaffMenu() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== KITCHEN & STAFF ===");
            System.out.println("1. View Stations");
            System.out.println("2. Start All Stations");
            System.out.println("3. Stop All Stations");
            System.out.println("4. View Staff");
            System.out.println("5. Add Staff");
            System.out.println("6. Assign Chef to Station");
            System.out.println("7. Back");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> showStations();
                case "2" -> {
                    routerService.startAllStations();
                    System.out.println("Stations started.");
                }
                case "3" -> {
                    routerService.stopAllStations();
                    System.out.println("Stations stopped.");
                }
                case "4" -> showStaff();
                case "5" -> addStaff();
                case "6" -> assignChefToStation();
                case "7" -> loop = false;
                default -> System.out.println("Invalid.");
            }
        }
    }

    private void inventoryMenu() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== INVENTORY & SUPPLIER ===");
            System.out.println("1. View Inventory");
            System.out.println("2. Manual Restock");
            System.out.println("3. View Low Stock Items");
            System.out.println("4. Back");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> inventoryService.printInventory();
                case "2" -> manualRestock();
                case "3" -> showLowStock();
                case "4" -> loop = false;
                default -> System.out.println("Invalid.");
            }
        }
    }

    private void billingMenu() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== BILLING & PAYMENTS ===");
            System.out.println("1. View All Bills");
            System.out.println("2. View Bill by Table");
            System.out.println("3. Process Payment");
            System.out.println("4. Back");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> billingService.printAllBills();
                case "2" -> {
                    System.out.print("Table number: ");
                    try {
                        int t = Integer.parseInt(scanner.nextLine());
                        System.out.println(billingService.getOrCreateBill(t));
                    } catch (Exception e) {
                        System.out.println("Invalid.");
                    }
                }
                case "3" -> processPayment();
                case "4" -> loop = false;
                default -> System.out.println("Invalid.");
            }
        }
    }

    private void ordersMenu() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== ORDERS ===");
            System.out.println("1. View All Orders");
            System.out.println("2. View Active Orders");
            System.out.println("3. Back");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> viewAllOrders();
                case "2" -> viewActiveOrders();
                case "3" -> loop = false;
                default -> System.out.println("Invalid.");
            }
        }
    }

    private void reservationsMenu() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== RESERVATIONS ===");
            System.out.println("1. View Reservations");
            System.out.println("2. Create Reservation");
            System.out.println("3. Cancel Reservation");
            System.out.println("4. Back");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> showReservations();
                case "2" -> createReservation();
                case "3" -> cancelReservation();
                case "4" -> loop = false;
                default -> System.out.println("Invalid.");
            }
        }
    }

    private void tablesAndWaitersMenu() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== TABLES & WAITERS ===");
            System.out.println("1. View Tables");
            System.out.println("2. Add Table");
            System.out.println("3. Remove Table");
            System.out.println("4. Occupy Table");
            System.out.println("5. Release Table");
            System.out.println("6. Clean Table");
            System.out.println("7. View Waiters");
            System.out.println("8. Add Waiter");
            System.out.println("9. Remove Waiter");
            System.out.println("10. Assign Waiter to Table");
            System.out.println("11. Unassign Waiter from Table");
            System.out.println("12. Back");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> listTables();
                case "2" -> addTable();
                case "3" -> removeTable();
                case "4" -> occupyTable();
                case "5" -> releaseTable();
                case "6" -> cleanTable();
                case "7" -> viewWaiters();
                case "8" -> addWaiter();
                case "9" -> removeWaiter();
                case "10" -> assignWaiter();
                case "11" -> unassignWaiter();
                case "12" -> loop = false;
                default -> System.out.println("Invalid.");
            }
        }
    }

    private void diagnosticsMenu() {
        boolean loop = true;
        while (loop) {
            System.out.println("\n=== SYSTEM DIAGNOSTICS ===");
            System.out.println("1. View Logs");
            System.out.println("2. Thread Status");
            System.out.println("3. Pause Stations");
            System.out.println("4. Resume Stations");
            System.out.println("5. Back");
            System.out.print("Choice: ");

            switch (scanner.nextLine()) {
                case "1" -> showLogs();
                case "2" -> showThreadStatus();
                case "3" -> engine.pauseStationsForIdle();
                case "4" -> engine.resumeStationsOnActivity();
                case "5" -> loop = false;
                default -> System.out.println("Invalid.");
            }
        }
    }

    private void showStations() {
        System.out.println("\n=== KITCHEN STATIONS ===");
        var stations = routerService.getStations();
        stations.forEach((cat, station) ->
                System.out.println(cat + " → " + station.getName())
        );
    }

    private void showStaff() {
        var list = staffService.getAllStaff();
        if (list.isEmpty()) {
            System.out.println("No staff.");
            return;
        }
        list.forEach(s -> System.out.println(
                s.getStaffId() + " | " + s.getName() + " | " + s.getRole()
        ));
    }

    private void addStaff() {
        try {
            System.out.print("Staff id: ");
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Name: ");
            String name = scanner.nextLine();
            staffService.addStaff(new Chef(id, name));
            System.out.println("Added.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void assignChefToStation() {
        try {
            System.out.print("Chef staff id: ");
            int id = Integer.parseInt(scanner.nextLine());
            Chef chef = staffService.findAvailableChef();

            if (chef == null) {
                System.out.println("No such chef.");
                return;
            }

            System.out.print("Station category (GRILL/DESSERT/BEVERAGE/HOT_BEVERAGE/COLD_BEVERAGE): ");
            OrderCategory cat = OrderCategory.valueOf(scanner.nextLine().toUpperCase());

            var station = routerService.getStations().get(cat);
            if (station == null) {
                System.out.println("Invalid station.");
                return;
            }

            station.assignChef(chef);
            System.out.println("Chef assigned.");

        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void manualRestock() {
        try {
            System.out.print("Ingredient: ");
            String name = scanner.nextLine();

            System.out.print("Qty: ");
            int qty = Integer.parseInt(scanner.nextLine());

            System.out.print("Expiry (mins): ");
            long mins = Long.parseLong(scanner.nextLine());

            inventoryService.restock(name, qty, System.currentTimeMillis() + mins * 60000);
            System.out.println("Restocked.");

        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void showLowStock() {
        var low = inventoryService.getLowStockItems();
        if (low.isEmpty()) {
            System.out.println("No low stock.");
            return;
        }
        low.forEach((k, v) -> System.out.println(k + " -> " + v));
    }

    private void processPayment() {
        try {
            System.out.print("Table number: ");
            int t = Integer.parseInt(scanner.nextLine());

            Bill bill = billingService.getOrCreateBill(t);
            System.out.println(bill);

            if (bill.isPaid()) {
                System.out.println("Already paid.");
                return;
            }

            System.out.println("1. Cash  2. Card  3. UPI");
            System.out.print("Method: ");

            PaymentMethod method = switch (scanner.nextLine()) {
                case "1" -> new CashPayment();
                case "2" -> new CardPayment();
                case "3" -> new UpiPayment();
                default -> null;
            };

            if (method == null) {
                System.out.println("Invalid.");
                return;
            }

            billingService.processPayment(t, method);

        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void viewAllOrders() {
        orderService.getAllOrders().forEach(System.out::println);
    }

    private void viewActiveOrders() {
        orderService.getAllOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.ACCEPTED ||
                        o.getStatus() == OrderStatus.IN_PROGRESS)
                .forEach(System.out::println);
    }


    private void showReservations() {
        var res = reservationService.getAllReservations();
        if (res.isEmpty()) {
            System.out.println("None.");
            return;
        }
        res.forEach(System.out::println);
    }

    private void createReservation() {
        try {
            System.out.print("Table number: ");
            int table = Integer.parseInt(scanner.nextLine());

            System.out.print("Start offset mins: ");
            int so = Integer.parseInt(scanner.nextLine());

            System.out.print("Duration mins: ");
            int du = Integer.parseInt(scanner.nextLine());

            var start = java.time.LocalDateTime.now().plusMinutes(so);
            var end = start.plusMinutes(du);

            Reservation r = reservationService.reserveTable(table, start, end);
            if (r == null) System.out.println("Failed.");
            else System.out.println("Reserved: " + r);

        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void cancelReservation() {
        try {
            System.out.print("Reservation id: ");
            int id = Integer.parseInt(scanner.nextLine());
            if (reservationService.cancel(id)) System.out.println("Cancelled.");
            else System.out.println("Not found.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void listTables() {
        tableService.listTables().forEach(System.out::println);
    }

    private void addTable() {
        try {
            System.out.print("Table number: ");
            int table = Integer.parseInt(scanner.nextLine());
            if (tableService.addTable(table)) System.out.println("Added.");
            else System.out.println("Exists.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void removeTable() {
        try {
            System.out.print("Table: ");
            int t = Integer.parseInt(scanner.nextLine());
            if (tableService.removeTable(t)) System.out.println("Removed.");
            else System.out.println("Failed.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void occupyTable() {
        try {
            System.out.print("Table: ");
            int t = Integer.parseInt(scanner.nextLine());
            if (tableService.occupyTable(t)) System.out.println("Occupied.");
            else System.out.println("Failed.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void releaseTable() {
        try {
            System.out.print("Table: ");
            int t = Integer.parseInt(scanner.nextLine());
            if (tableService.releaseTable(t)) System.out.println("Released.");
            else System.out.println("Failed.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void cleanTable() {
        try {
            System.out.print("Table: ");
            int t = Integer.parseInt(scanner.nextLine());
            if (tableService.cleanTable(t)) System.out.println("Cleaned.");
            else System.out.println("Failed.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void viewWaiters() {
        var list = waiterService.getWaiters();
        if (list.isEmpty()) System.out.println("No waiters.");
        else list.forEach(System.out::println);
    }

    private void addWaiter() {
        try {
            System.out.print("Waiter id: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Name: ");
            String name = scanner.nextLine();

            if (waiterService.addWaiter(id, name)) System.out.println("Added.");
            else System.out.println("Exists.");

        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void removeWaiter() {
        try {
            System.out.print("Waiter id: ");
            int id = Integer.parseInt(scanner.nextLine());
            if (waiterService.removeWaiter(id)) System.out.println("Removed.");
            else System.out.println("Not found.");

        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void assignWaiter() {
        try {
            System.out.print("Table number: ");
            int table = Integer.parseInt(scanner.nextLine());

            System.out.print("Waiter id: ");
            int id = Integer.parseInt(scanner.nextLine());

            Waiter waiter = waiterService.getWaiter(id);

            if (waiter == null) {
                System.out.println("Waiter not found.");
                return;
            }

            if (tableService.assignWaiterToTable(table, waiter))
                System.out.println("Assigned.");
            else
                System.out.println("Failed.");

        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void unassignWaiter() {
        try {
            System.out.print("Table number: ");
            int t = Integer.parseInt(scanner.nextLine());

            if (tableService.unassignWaiterFromTable(t))
                System.out.println("Unassigned.");
            else
                System.out.println("Failed.");

        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void showLogs() {
        var logs = logger.getLogs();
        if (logs.isEmpty()) {
            System.out.println("(no logs)");
            return;
        }
        for (int i = logs.size() - 1; i >= 0; i--) {
            System.out.println(logs.get(i));
        }
    }

    private void showThreadStatus() {
        System.out.println("\n=== THREADS ===");
        System.out.println("Engine started: " + engine.isStarted());
        System.out.println("Stations paused: " + engine.isStationsPaused());
    }
}
