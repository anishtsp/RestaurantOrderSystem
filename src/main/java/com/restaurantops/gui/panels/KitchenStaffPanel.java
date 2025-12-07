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

        refreshTables();
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
        });

        stopBtn.addActionListener(e -> {
            routerService.stopAllStations();
            JOptionPane.showMessageDialog(this, "All stations stopped.");
        });

        assignBtn.addActionListener(e -> assignChefUI());

        btnPanel.add(startBtn);
        btnPanel.add(stopBtn);
        btnPanel.add(assignBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void assignChefUI() {

        // Select category manually
        OrderCategory category = (OrderCategory) JOptionPane.showInputDialog(
                this,
                "Select station category to assign chef:",
                "Assign Chef",
                JOptionPane.PLAIN_MESSAGE,
                null,
                new OrderCategory[]{OrderCategory.GRILL, OrderCategory.DESSERT, OrderCategory.BEVERAGE},
                OrderCategory.GRILL
        );

        if (category == null) return;

        Chef chef = staffService.findAvailableChef();
        if (chef == null) {
            JOptionPane.showMessageDialog(this, "No available chef found.");
            return;
        }

        // Retrieve target station (BEVERAGE will choose HOT beverage by default)
        KitchenStation station = null;

        Map<OrderCategory, KitchenStation> stations = routerService.getStations();

        if (category == OrderCategory.BEVERAGE) {
            // Ask hot or cold
            Object choice = JOptionPane.showInputDialog(
                    this,
                    "Select beverage station:",
                    "Beverage Station",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new String[]{"Hot Beverage", "Cold Beverage"},
                    "Hot Beverage"
            );

            if (choice == null) return;

            if (choice.equals("Hot Beverage"))
                station = routerService.getStations().get(OrderCategory.BEVERAGE); // original beverage station = hot
            else
                station = getColdBeverageStation(); // getter implemented below

        } else {
            station = stations.get(category);
        }

        if (station == null) {
            JOptionPane.showMessageDialog(this, "Station not found.");
            return;
        }

        station.assignChef(chef);

        JOptionPane.showMessageDialog(this,
                "Chef " + chef.getName() + " assigned to " + station.getName());

        refreshTables();
    }

    /* Helper to reach cold beverage station */
    private KitchenStation getColdBeverageStation() {
        // Cold beverage station is not inside stations map, so pull via reflection:
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

            refreshTables();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }

    /* ===========================
            REFRESH TABLES
       =========================== */

    private void refreshTables() {

        DefaultTableModel stationModel = (DefaultTableModel) stationsTable.getModel();
        stationModel.setRowCount(0);

        /* ---- Add GRILL / DESSERT / BEVERAGE (Hot) stations ---- */
        for (Map.Entry<OrderCategory, KitchenStation> entry : routerService.getStations().entrySet()) {
            KitchenStation st = entry.getValue();
            stationModel.addRow(new Object[]{
                    entry.getKey(),
                    st.getName(),
                    st.getAssignedChef() == null ? "None" : st.getAssignedChef().getName()
            });
        }

        /* ---- Add HOT beverage station ---- */
        KitchenStation hot = getHotBeverageStation();
        if (hot != null) {
            stationModel.addRow(new Object[]{
                    "BEVERAGE (Hot)",
                    hot.getName(),
                    hot.getAssignedChef() == null ? "None" : hot.getAssignedChef().getName()
            });
        }

        /* ---- Add COLD beverage station ---- */
        KitchenStation cold = getColdBeverageStation();
        if (cold != null) {
            stationModel.addRow(new Object[]{
                    "BEVERAGE (Cold)",
                    cold.getName(),
                    cold.getAssignedChef() == null ? "None" : cold.getAssignedChef().getName()
            });
        }

        /* ---- Refresh Staff table ---- */
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
