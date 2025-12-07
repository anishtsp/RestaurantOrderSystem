package com.restaurantops.gui.panels;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.gui.components.DashboardCard;
import com.restaurantops.model.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class RestaurantDashboardPanel extends JPanel {

    private final RestaurantEngine engine;

    private DashboardCard cardActiveOrders;
    private DashboardCard cardOccupiedTables;
    private DashboardCard cardLowStock;
    private DashboardCard cardRevenue;
    private DashboardCard cardStaff;
    private DashboardCard cardReservations;

    public RestaurantDashboardPanel() {

        this.engine = RestaurantEngine.getInstance();
        setLayout(new GridLayout(3, 2, 20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        cardActiveOrders    = new DashboardCard("Active Orders", "📦");
        cardOccupiedTables  = new DashboardCard("Occupied Tables", "🍽");
        cardLowStock        = new DashboardCard("Low Stock", "⚠️");
        cardRevenue         = new DashboardCard("Total Paid Revenue", "💵");
        cardStaff           = new DashboardCard("Staff Count", "🧍");
        cardReservations    = new DashboardCard("Reservations Today", "📅");

        add(cardActiveOrders);
        add(cardOccupiedTables);
        add(cardLowStock);
        add(cardRevenue);
        add(cardStaff);
        add(cardReservations);

        refreshStatistics();
    }

    public void refreshStatistics() {

        /* ---------------- ACTIVE ORDERS ---------------- */
        long activeOrders = engine.getOrderService().getAllOrders()
                .stream()
                .filter(o -> o.getStatus() != OrderStatus.COMPLETED &&
                        o.getStatus() != OrderStatus.REJECTED)
                .count();
        cardActiveOrders.setValue(String.valueOf(activeOrders));


        /* ---------------- OCCUPIED TABLES ---------------- */
        long occupiedTables = engine.getTableService().listTables()
                .stream()
                .filter(t -> t.getState() == TableState.OCCUPIED)
                .count();
        cardOccupiedTables.setValue(String.valueOf(occupiedTables));


        /* ---------------- LOW STOCK ---------------- */
        int lowStock = engine.getInventoryService().getLowStockItems().size();
        cardLowStock.setValue(String.valueOf(lowStock));


        /* ---------------- TOTAL PAID REVENUE ---------------- */
        double revenue = engine.getBillingService().getAllBills()
                .values()
                .stream()
                .filter(Bill::isPaid)
                .mapToDouble(Bill::getTotalAmount)
                .sum();
        cardRevenue.setValue(String.format("%.2f", revenue));


        /* ---------------- STAFF COUNT ---------------- */
        int staffCount = engine.getStaffService().getAllStaff().size();
        cardStaff.setValue(String.valueOf(staffCount));


        /* ---------------- RESERVATIONS TODAY ---------------- */
        long todayReservations = engine.getReservationService().getAllReservations()
                .stream()
                .filter(r -> r.getStart().toLocalDate().equals(LocalDate.now()))
                .count();
        cardReservations.setValue(String.valueOf(todayReservations));
    }
}
