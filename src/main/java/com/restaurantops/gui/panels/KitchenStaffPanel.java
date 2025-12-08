package com.restaurantops.gui.panels;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.kitchen.KitchenStation;
import com.restaurantops.model.OrderCategory;
import com.restaurantops.staff.Chef;
import com.restaurantops.staff.Staff;
import com.restaurantops.service.KitchenRouterService;
import com.restaurantops.staff.StaffService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class KitchenStaffPanel extends JPanel {

    private final RestaurantEngine engine;
    private final StaffService staffService;
    private final KitchenRouterService routerService;

    private JTable stationsTable;
    private JTable staffTable;

    private JTextField staffIdField;
    private JTextField staffNameField;

    public KitchenStaffPanel() {

        engine = RestaurantEngine.getInstance();
        staffService = engine.getStaffService();
        routerService = engine.getRouterService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildStationsSection(), BorderLayout.NORTH);
        add(buildStaffSection(), BorderLayout.CENTER);

        // AUTO-REFRESH when tab becomes visible
        addHierarchyListener(e -> {
            if (isShowing()) {
                SwingUtilities.invokeLater(this::refreshTables);
            }
        });

        SwingUtilities.invokeLater(this::refreshTables);
    }

    /* ===========================
            STATIONS SECTION
       =========================== */

    private JPanel buildStationsSection() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("Kitchen Stations");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        stationsTable = new JTable(new DefaultTableModel(
                new Object[]{"Category", "Station Name", "Chef Assigned"}, 0
        ));

        panel.add(new JScrollPane(stationsTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton startBtn = new JButton("Start All Stations");
        JButton stopBtn = new JButton("Stop All Stations");
        JButton assignBtn = new JButton("Assign Chef to Station");

        startBtn.addActionListener(e -> {
            routerService.startAllStations();
            JOptionPane.showMessageDialog(this, "All stations started.");
            SwingUtilities.invokeLater(this::refreshTables);
        });

        stopBtn.addActionListener(e -> {
            routerService.stopAllStations();
            JOptionPane.showMessageDialog(this, "All stations stopped.");
            SwingUtilities.invokeLater(this::refreshTables);
        });

        assignBtn.addActionListener(e -> assignChefUI());

        btnPanel.add(startBtn);
        btnPanel.add(stopBtn);
        btnPanel.add(assignBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void assignChefUI() {

        // 1) Choose station category
        OrderCategory category = (OrderCategory) JOptionPane.showInputDialog(
                this,
                "Select station category:",
                "Assign Chef",
                JOptionPane.PLAIN_MESSAGE,
                null,
                new OrderCategory[]{OrderCategory.GRILL, OrderCategory.DESSERT, OrderCategory.BEVERAGE},
                OrderCategory.GRILL
        );

        if (category == null) return;

        // 2) Choose chef explicitly
        var chefs = staffService.getAllStaff().stream()
                .filter(s -> s instanceof Chef)
                .map(s -> (Chef) s)
                .toList();

        if (chefs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No chefs available.");
            return;
        }

        Chef selectedChef = (Chef) JOptionPane.showInputDialog(
                this,
                "Select a chef:",
                "Choose Chef",
                JOptionPane.PLAIN_MESSAGE,
                null,
                chefs.toArray(),
                chefs.get(0)
        );

        if (selectedChef == null) return;

        // 3) Choose station
        KitchenStation station;

        if (category == OrderCategory.BEVERAGE) {
            Object choice = JOptionPane.showInputDialog(
                    this,
                    "Select beverage station:",
                    "Beverage Station Type",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new String[]{"Hot Beverage", "Cold Beverage"},
                    "Hot Beverage"
            );

            if (choice == null) return;

            station = choice.equals("Hot Beverage")
                    ? getHotBeverageStation()
                    : getColdBeverageStation();

        } else {
            station = routerService.getStations().get(category);
        }

        if (station == null) {
            JOptionPane.showMessageDialog(this, "Station not found.");
            return;
        }

        // 4) Assign chef
        station.assignChef(selectedChef);

        JOptionPane.showMessageDialog(
                this,
                "Chef " + selectedChef.getName() + " assigned to " + station.getName()
        );

        SwingUtilities.invokeLater(this::refreshTables);
    }

    /* Helper to reach cold beverage station */
    private KitchenStation getColdBeverageStation() {
        try {
            var field = routerService.getClass().getDeclaredField("coldBeverage");
            field.setAccessible(true);
            return (KitchenStation) field.get(routerService);
        } catch (Exception e) {
            return null;
        }
    }

    private KitchenStation getHotBeverageStation() {
        try {
            var field = routerService.getClass().getDeclaredField("hotBeverage");
            field.setAccessible(true);
            return (KitchenStation) field.get(routerService);
        } catch (Exception e) {
            return null;
        }
    }

    /* ===========================
            STAFF SECTION
       =========================== */

    private JPanel buildStaffSection() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("Staff Management");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        staffTable = new JTable(new DefaultTableModel(
                new Object[]{"ID", "Name", "Role"}, 0
        ));
        panel.add(new JScrollPane(staffTable), BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        staffIdField = new JTextField(5);
        staffNameField = new JTextField(12);
        JButton addBtn = new JButton("Add Chef");

        addBtn.addActionListener(e -> addStaff());

        addPanel.add(new JLabel("ID:"));
        addPanel.add(staffIdField);
        addPanel.add(new JLabel("Name:"));
        addPanel.add(staffNameField);
        addPanel.add(addBtn);

        panel.add(addPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addStaff() {
        try {
            int id = Integer.parseInt(staffIdField.getText());
            String name = staffNameField.getText();

            staffService.addStaff(new Chef(id, name));

            JOptionPane.showMessageDialog(this, "Chef added.");
            staffIdField.setText("");
            staffNameField.setText("");

            SwingUtilities.invokeLater(this::refreshTables);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }

    /* ===========================
            REFRESH TABLES
       =========================== */

    private void refreshTables() {

        if (routerService == null) return;

        // Stations
        DefaultTableModel stationModel = (DefaultTableModel) stationsTable.getModel();
        stationModel.setRowCount(0);

        for (Map.Entry<OrderCategory, KitchenStation> entry : routerService.getStations().entrySet()) {
            KitchenStation st = entry.getValue();
            stationModel.addRow(new Object[]{
                    entry.getKey(),
                    st.getName(),
                    (st.getAssignedChef() == null ? "None" : st.getAssignedChef().getName())
            });
        }

        KitchenStation hot = getHotBeverageStation();
        if (hot != null) {
            stationModel.addRow(new Object[]{
                    "BEVERAGE (Hot)",
                    hot.getName(),
                    (hot.getAssignedChef() == null ? "None" : hot.getAssignedChef().getName())
            });
        }

        KitchenStation cold = getColdBeverageStation();
        if (cold != null) {
            stationModel.addRow(new Object[]{
                    "BEVERAGE (Cold)",
                    cold.getName(),
                    (cold.getAssignedChef() == null ? "None" : cold.getAssignedChef().getName())
            });
        }

        // Staff
        DefaultTableModel staffModel = (DefaultTableModel) staffTable.getModel();
        staffModel.setRowCount(0);

        for (Staff s : staffService.getAllStaff()) {
            staffModel.addRow(new Object[]{
                    s.getStaffId(),
                    s.getName(),
                    s.getRole()
            });
        }
    }
}
