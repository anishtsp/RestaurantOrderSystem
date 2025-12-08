package com.restaurantops.gui.customer;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.Reservation;
import com.restaurantops.service.ReservationService;
import com.restaurantops.service.TableService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationPanelTouch extends JPanel {

    private final ReservationService reservationService;
    private final TableService tableService;

    private JPanel reservationGrid;

    public ReservationPanelTouch(RestaurantEngine engine) {

        this.reservationService = engine.getReservationService();
        this.tableService = engine.getTableService();

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildReservationsDisplay(), BorderLayout.CENTER);

        refreshReservations();
    }

    private JPanel buildHeader() {

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));

        JLabel title = new JLabel("Reservations");
        title.setFont(new Font("Arial", Font.BOLD, 28));

        JButton createBtn = new JButton("Create Reservation");
        createBtn.setFont(new Font("Arial", Font.BOLD, 20));
        createBtn.addActionListener(e -> openCreateReservationDialog());

        header.add(title);
        header.add(createBtn);

        return header;
    }

    private JScrollPane buildReservationsDisplay() {

        reservationGrid = new JPanel(new GridLayout(0, 2, 20, 20)); // 2 column touchscreen layout
        return new JScrollPane(reservationGrid);
    }

    private void refreshReservations() {

        reservationGrid.removeAll();

        List<Reservation> reservations =
                new java.util.ArrayList<>(reservationService.getAllReservations());

        if (reservations.isEmpty()) {
            JLabel empty = new JLabel("No reservations.", SwingConstants.CENTER);
            empty.setFont(new Font("Arial", Font.PLAIN, 20));
            reservationGrid.add(empty);
        } else {
            for (Reservation r : reservations) {
                reservationGrid.add(buildReservationCard(r));
            }
        }

        reservationGrid.revalidate();
        reservationGrid.repaint();
    }


    private JPanel buildReservationCard(Reservation r) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        card.setBackground(new Color(250, 250, 250));

        JLabel id = new JLabel("Reservation #" + r.getReservationId(), SwingConstants.CENTER);
        id.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel table = new JLabel("Table: " + r.getTableNumber(), SwingConstants.CENTER);
        JLabel time = new JLabel("From: " + r.getStart(), SwingConstants.CENTER);
        JLabel endTime = new JLabel("To: " + r.getEnd(), SwingConstants.CENTER);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 18));
        cancelBtn.setBackground(new Color(200, 0, 0));
        cancelBtn.setForeground(Color.WHITE);

        cancelBtn.addActionListener(e -> {
            if (reservationService.cancel(r.getReservationId())) {
                JOptionPane.showMessageDialog(this, "Reservation cancelled.");
                refreshReservations();
            } else {
                JOptionPane.showMessageDialog(this, "Could not cancel reservation.");
            }
        });

        card.add(Box.createVerticalStrut(10));
        card.add(id);
        card.add(table);
        card.add(time);
        card.add(endTime);
        card.add(Box.createVerticalStrut(10));
        card.add(cancelBtn);
        card.add(Box.createVerticalStrut(10));

        return card;
    }

    private void openCreateReservationDialog() {

        JPanel p = new JPanel(new GridLayout(4, 2, 10, 10));

        // TABLE PICKER (touch dropdown)
        var tables = tableService.listTables()
                .stream()
                .map(t -> t.getTableNumber())
                .toArray(Integer[]::new);

        JComboBox<Integer> tableBox = new JComboBox<>(tables);
        tableBox.setFont(new Font("Arial", Font.BOLD, 18));

        // START OFFSET PICKER
        Integer[] offsets = {0, 5, 10, 15, 30, 45, 60, 90, 120};
        JComboBox<Integer> offsetBox = new JComboBox<>(offsets);
        offsetBox.setFont(new Font("Arial", Font.BOLD, 18));

        // DURATION PICKER
        Integer[] durations = {30, 45, 60, 90, 120, 150};
        JComboBox<Integer> durationBox = new JComboBox<>(durations);
        durationBox.setFont(new Font("Arial", Font.BOLD, 18));

        p.add(new JLabel("Table:", SwingConstants.RIGHT));
        p.add(tableBox);
        p.add(new JLabel("Start in (mins):", SwingConstants.RIGHT));
        p.add(offsetBox);
        p.add(new JLabel("Duration (mins):", SwingConstants.RIGHT));
        p.add(durationBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                p,
                "Create Reservation",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION)
            return;

        try {
            int table = (Integer) tableBox.getSelectedItem();
            int offset = (Integer) offsetBox.getSelectedItem();
            int duration = (Integer) durationBox.getSelectedItem();

            LocalDateTime start = LocalDateTime.now().plusMinutes(offset);
            LocalDateTime end = start.plusMinutes(duration);

            Reservation r = reservationService.reserveTable(table, start, end);

            if (r != null) {
                JOptionPane.showMessageDialog(this,
                        "Reservation created!\nID: " + r.getReservationId());
                refreshReservations();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to reserve table.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }
}
