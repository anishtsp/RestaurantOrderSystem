package com.restaurantops.gui.customer;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.Reservation;
import com.restaurantops.model.Table;
import com.restaurantops.service.ReservationService;
import com.restaurantops.service.TableService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SeatingPanelTouch extends JPanel {

    private final TableService tableService;
    private final ReservationService reservationService;

    public SeatingPanelTouch(RestaurantEngine engine) {

        this.tableService = engine.getTableService();
        this.reservationService = engine.getReservationService();

        setLayout(new BorderLayout(30, 30));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildButtonsPanel(), BorderLayout.CENTER);
    }

    private JLabel buildHeader() {
        JLabel title = new JLabel("Seating", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        return title;
    }

    private JPanel buildButtonsPanel() {

        JPanel panel = new JPanel(new GridLayout(0, 1, 20, 20));

        JButton occupyFreeBtn = bigButton("Occupy Any Free Table");
        JButton occupyReservedBtn = bigButton("Occupy Reserved Table");
        JButton releaseBtn = bigButton("Release Table");
        JButton showMyTableBtn = bigButton("Show Table Status");

        occupyFreeBtn.addActionListener(e -> occupyAnyFreeTable());
        occupyReservedBtn.addActionListener(e -> occupyReservedTable());
        releaseBtn.addActionListener(e -> releaseTable());
        showMyTableBtn.addActionListener(e -> showTableStatus());

        panel.add(occupyFreeBtn);
        panel.add(occupyReservedBtn);
        panel.add(releaseBtn);
        panel.add(showMyTableBtn);

        return panel;
    }

    /** Utility for large touchscreen buttons */
    private JButton bigButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 22));
        b.setPreferredSize(new Dimension(350, 70));
        return b;
    }

    /* ============================
          ACTION HANDLERS
       ============================ */

    private void occupyAnyFreeTable() {

        Integer t = tableService.occupyAnyFreeTable();

        if (t == null) {
            JOptionPane.showMessageDialog(this, "No free tables available.");
        } else {
            JOptionPane.showMessageDialog(this,
                    "You have been seated at table " + t + "!");
        }
    }

    private void occupyReservedTable() {

        List<Reservation> reservations =
                reservationService.getAllReservations().stream().collect(Collectors.toList());

        if (reservations.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No active reservations.");
            return;
        }

        Integer[] ids = reservations.stream()
                .map(Reservation::getReservationId)
                .toArray(Integer[]::new);

        JComboBox<Integer> box = new JComboBox<>(ids);
        box.setFont(new Font("Arial", Font.BOLD, 18));

        int res = JOptionPane.showConfirmDialog(
                this,
                box,
                "Select Reservation ID",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (res != JOptionPane.OK_OPTION) return;

        int id = (Integer) box.getSelectedItem();

        boolean ok = tableService.occupyReservedTable(id);

        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Reservation seated successfully!");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Could not occupy reservation. It may be expired or invalid.");
        }
    }

    private void releaseTable() {

        // List only OCCUPIED tables
        List<Table> occupied = new ArrayList<>(tableService.listTables())
                .stream()
                .filter(t -> t.getState().toString().equals("OCCUPIED"))
                .collect(Collectors.toList());


        if (occupied.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No occupied tables to release.");
            return;
        }

        Integer[] ids = occupied.stream()
                .map(Table::getTableNumber)
                .toArray(Integer[]::new);

        JComboBox<Integer> box = new JComboBox<>(ids);
        box.setFont(new Font("Arial", Font.BOLD, 18));

        int res = JOptionPane.showConfirmDialog(
                this,
                box,
                "Select Table to Release",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (res != JOptionPane.OK_OPTION) return;

        int tableNum = (Integer) box.getSelectedItem();

        boolean ok = tableService.releaseTable(tableNum);

        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Table " + tableNum + " released and marked for cleaning.");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to release table.");
        }
    }

    private void showTableStatus() {

        List<Table> tables = new java.util.ArrayList<>(tableService.listTables());

        StringBuilder sb = new StringBuilder();

        for (Table t : tables) {
            sb.append("Table ")
                    .append(t.getTableNumber())
                    .append(" → State: ")
                    .append(t.getState())
                    .append("\n");
        }

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 16));
        area.setEditable(false);

        JOptionPane.showMessageDialog(this,
                new JScrollPane(area),
                "Table Status",
                JOptionPane.PLAIN_MESSAGE);
    }
}
