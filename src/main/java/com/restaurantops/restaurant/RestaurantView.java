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
            String choice = scanner.nextLine();
            switch (choice) {
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
                        Bill b = billingService.getOrCreateBill(t);
                        System.out.println(b);
                    } catch (Exception e) {
                        System.out.println("Invalid input.");
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
            System.out.println("3. Cancel Order");
            System.out.println("4. Back");
            System.out.print("Choice: ");
            switch (scanner.nextLine()) {
                case "1" -> viewAllOrders();
                case "2" -> viewActiveOrders();
                case "3" -> cancelOrder();
                case "4" -> loop = false;
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
            System.out.println("1. View Logs (last entries)");
            System.out.println("2. View Thread Status");
            System.out.println("3. Pause Stations");
            System.out.println("4. Resume Stations");
            System.out.println("5. Back");
            System.out.print("Choice: ");
            switch (scanner.nextLine()) {
                case "1" -> showLogs();
                case "2" -> showThreadStatus();
                case "3" -> {
                    engine.pauseStationsForIdle();
                    System.out.println("Stations paused.");
                }
                case "4" -> {
                    engine.resumeStationsOnActivity();
                    System.out.println("Stations resumed.");
                }
                case "5" -> loop = false;
                default -> System.out.println("Invalid.");
            }
        }
    }

    private void showStations() {
        System.out.println("\n=== KITCHEN STATIONS ===");
        Map<OrderCategory, ?> stations = routerService.getStations();
        if (stations == null || stations.isEmpty()) {
            System.out.println("No stations configured.");
            return;
        }
        stations.forEach((cat, st) -> {
            String name = st instanceof Object ? st.getClass().getSimpleName() : st.toString();
            System.out.println(cat + " -> " + name);
        });
    }

    private void showStaff() {
        System.out.println("\n=== STAFF ===");
        List<Staff> staff = staffService.getAllStaff();
        if (staff.isEmpty()) {
            System.out.println("No staff.");
            return;
        }
        staff.forEach(s -> System.out.println(s.getStaffId() + " | " + s.getName() + " | " + s.getRole()));
    }

    private void addStaff() {
        try {
            System.out.print("Staff id: ");
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Name: ");
            String name = scanner.nextLine();
            System.out.print("Role (CHEF/WAITER/MANAGER): ");
            String role = scanner.nextLine();
            staffService.addStaff(new Chef(id, name));
            System.out.println("Added.");
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    private void assignChefToStation() {
        try {
            System.out.print("Chef staff id: ");
            int id = Integer.parseInt(scanner.nextLine());
            Staff s = staffService.findAvailableChef();
            if (s == null) {
                System.out.println("Staff not found.");
                return;
            }
            System.out.print("Station category (GRILL/DESSERT/BEVERAGE): ");
            String cat = scanner.nextLine();
            OrderCategory oc;
            try {
                oc = OrderCategory.valueOf(cat.toUpperCase());
            } catch (Exception ex) {
                System.out.println("Invalid category.");
                return;
            }
            var stations = routerService.getStations();
            var station = stations.get(oc);
            if (station == null) {
                System.out.println("Station not found.");
                return;
            }
            if (s instanceof Chef) {
                station.assignChef((Chef) s);
                System.out.println("Assigned.");
            } else {
                System.out.println("Staff is not a chef.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    private void manualRestock() {
        try {
            System.out.print("Ingredient name: ");
            String name = scanner.nextLine();
            System.out.print("Quantity: ");
            int q = Integer.parseInt(scanner.nextLine());
            System.out.print("Expiry offset minutes: ");
            long mins = Long.parseLong(scanner.nextLine());
            long expiry = System.currentTimeMillis() + mins * 60_000L;
            inventoryService.restock(name, q, expiry);
            System.out.println("Restocked.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void showLowStock() {
        var low = inventoryService.getLowStockItems();
        if (low.isEmpty()) {
            System.out.println("No low stock items.");
            return;
        }
        low.forEach((k, v) -> System.out.println(k + " -> " + v));
    }

    private void processPayment() {
        try {
            System.out.print("Table number: ");
            int t = Integer.parseInt(scanner.nextLine());
            Bill b = billingService.getOrCreateBill(t);
            System.out.println(b);
            if (b.isPaid()) {
                System.out.println("Already paid.");
                return;
            }
            System.out.println("1. Cash  2. Card  3. UPI");
            System.out.print("Method: ");
            String m = scanner.nextLine();
            PaymentMethod pm = switch (m) {
                case "1" -> new CashPayment();
                case "2" -> new CardPayment();
                case "3" -> new UpiPayment();
                default -> null;
            };
            if (pm == null) {
                System.out.println("Invalid.");
                return;
            }
            billingService.processPayment(t, pm);
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void viewAllOrders() {
        List<Order> list = orderService.getAllOrders();
        if (list.isEmpty()) {
            System.out.println("No orders.");
            return;
        }
        list.forEach(System.out::println);
    }

    private void viewActiveOrders() {
        orderService.getAllOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.IN_PROGRESS || o.getStatus() == OrderStatus.ACCEPTED)
                .forEach(System.out::println);
    }

    private void cancelOrder() {
        try {
            System.out.print("Order id: ");
            int id = Integer.parseInt(scanner.nextLine());
            boolean found = false;
            for (Order o : orderService.getAllOrders()) {
                if (o.getOrderId() == id) {
                    o.setStatus(OrderStatus.REJECTED);
                    logger.log("[MANAGER] Order#" + id + " cancelled");
                    System.out.println("Cancelled.");
                    found = true;
                    break;
                }
            }
            if (!found) System.out.println("Order not found.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void showReservations() {
        Collection<Reservation> res = reservationService.getAllReservations();
        if (res.isEmpty()) {
            System.out.println("No reservations.");
            return;
        }
        res.forEach(System.out::println);
    }

    private void createReservation() {
        try {
            System.out.print("Table number: ");
            int t = Integer.parseInt(scanner.nextLine());
            System.out.print("Customer name: ");
            String n = scanner.nextLine();
            System.out.print("Start offset minutes: ");
            int so = Integer.parseInt(scanner.nextLine());
            System.out.print("Duration minutes: ");
            int du = Integer.parseInt(scanner.nextLine());
            var start = java.time.LocalDateTime.now().plusMinutes(so);
            var end = start.plusMinutes(du);
            Reservation r = reservationService.reserveTable(t, start, end);
            if (r == null) System.out.println("Unable to reserve.");
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
        Collection<Table> list = tableService.listTables();
        if (list.isEmpty()) {
            System.out.println("No tables.");
            return;
        }
        list.forEach(t -> System.out.println(t));
    }

    private void addTable() {
        try {
            System.out.print("Table number: ");
            int t = Integer.parseInt(scanner.nextLine());
            if (tableService.addTable(t)) System.out.println("Added.");
            else System.out.println("Exists.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void removeTable() {
        try {
            System.out.print("Table number: ");
            int t = Integer.parseInt(scanner.nextLine());
            if (tableService.removeTable(t)) System.out.println("Removed.");
            else System.out.println("Cannot remove.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void occupyTable() {
        try {
            System.out.print("Table number: ");
            int t = Integer.parseInt(scanner.nextLine());
            if (tableService.occupyTable(t)) System.out.println("Occupied.");
            else System.out.println("Failed.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void releaseTable() {
        try {
            System.out.print("Table number: ");
            int t = Integer.parseInt(scanner.nextLine());
            if (tableService.releaseTable(t)) System.out.println("Released.");
            else System.out.println("Failed.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void cleanTable() {
        try {
            System.out.print("Table number: ");
            int t = Integer.parseInt(scanner.nextLine());
            if (tableService.cleanTable(t)) System.out.println("Cleaned.");
            else System.out.println("Failed.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void viewWaiters() {
        List<Waiter> list = waiterService.getWaiters();
        if (list.isEmpty()) {
            System.out.println("No waiters.");
            return;
        }
        list.forEach(System.out::println);
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
            int t = Integer.parseInt(scanner.nextLine());
            System.out.print("Waiter id: ");
            int id = Integer.parseInt(scanner.nextLine());
            Waiter w = waiterService.getWaiter(id);
            if (w == null) {
                System.out.println("Waiter not found.");
                return;
            }
            if (tableService.assignWaiterToTable(t, w)) System.out.println("Assigned.");
            else System.out.println("Failed.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void unassignWaiter() {
        try {
            System.out.print("Table number: ");
            int t = Integer.parseInt(scanner.nextLine());
            if (tableService.unassignWaiterFromTable(t)) System.out.println("Unassigned.");
            else System.out.println("Failed.");
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
    }

    private void showLogs() {
        System.out.println("\n=== LOGS ===");
        List<String> logs = logger.getLogs();

        if (logs.isEmpty()) {
            System.out.println("(no logs recorded)");
            return;
        }

        for (int i = logs.size() - 1; i >= 0; i--) {
            System.out.println(logs.get(i));
        }
    }


    private void showThreadStatus() {
        System.out.println("\n=== THREAD STATUS ===");
        System.out.println("Engine started: " + engine.isStarted());
        System.out.println("Stations paused: " + engine.isStationsPaused());
    }
}
