package com.restaurantops.gui.panels;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.Reservation;
import com.restaurantops.service.ReservationService;
import com.restaurantops.service.TableService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReservationsPanel extends JPanel {

    private final RestaurantEngine engine;
    private final ReservationService reservationService;
    private final TableService tableService;

    private JTable table;
    private DefaultTableModel model;

    private JSpinner tableNumberField;
    private JSpinner startOffsetField;
    private JSpinner durationField;

    private final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReservationsPanel() {

        engine = RestaurantEngine.getInstance();
        reservationService = engine.getReservationService();
        tableService = engine.getTableService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildMainArea(), BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Reservations");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshTable());

        p.add(title, BorderLayout.WEST);
        p.add(refresh, BorderLayout.EAST);

        return p;
    }

    private JComponent buildMainArea() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.7);

        split.setTopComponent(buildReservationTable());
        split.setBottomComponent(buildCreateForm());

        return split;
    }

    /* -------------------------
         TABLE OF RESERVATIONS
       ------------------------- */

    private JScrollPane buildReservationTable() {

        model = new DefaultTableModel(
                new Object[]{"ID", "Table", "Start", "End"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem cancel = new JMenuItem("Cancel Reservation");
        JMenuItem occupy = new JMenuItem("Occupy Now");

        cancel.addActionListener(e -> cancelSelected());
        occupy.addActionListener(e -> occupySelected());

        menu.add(cancel);
        menu.add(occupy);

        table.setComponentPopupMenu(menu);

        return new JScrollPane(table);
    }

    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a reservation.");
            return;
        }

        int id = (int) model.getValueAt(row, 0);

        boolean ok = reservationService.cancel(id);
        JOptionPane.showMessageDialog(this,
                ok ? "Reservation cancelled." : "Could not cancel reservation.");

        refreshTable();
    }

    private void occupySelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a reservation.");
            return;
        }

        int id = (int) model.getValueAt(row, 0);

        boolean ok = tableService.occupyReservedTable(id);
        JOptionPane.showMessageDialog(this,
                ok ? "Table occupied." : "Failed to occupy table.");

        refreshTable();
    }

    /* -------------------------
            CREATE FORM
       ------------------------- */

    private JPanel buildCreateForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Create Reservation"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5,5,5,5);
        gc.anchor = GridBagConstraints.WEST;

        tableNumberField = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        startOffsetField = new JSpinner(new SpinnerNumberModel(10, 0, 10000, 5));
        durationField = new JSpinner(new SpinnerNumberModel(30, 5, 10000, 5));

        JButton submit = new JButton("Create Reservation");
        submit.addActionListener(e -> createReservation());

        gc.gridx = 0; gc.gridy = 0;
        p.add(new JLabel("Table Number:"), gc);
        gc.gridx = 1;
        p.add(tableNumberField, gc);

        gc.gridx = 0; gc.gridy = 1;
        p.add(new JLabel("Start Offset (mins):"), gc);
        gc.gridx = 1;
        p.add(startOffsetField, gc);

        gc.gridx = 0; gc.gridy = 2;
        p.add(new JLabel("Duration (mins):"), gc);
        gc.gridx = 1;
        p.add(durationField, gc);

        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        p.add(submit, gc);

        return p;
    }

    private void createReservation() {
        try {
            int table = (int) tableNumberField.getValue();
            int offset = (int) startOffsetField.getValue();
            int duration = (int) durationField.getValue();

            LocalDateTime start = LocalDateTime.now().plusMinutes(offset);
            LocalDateTime end = start.plusMinutes(duration);

            Reservation r = reservationService.reserveTable(table, start, end);

            if (r == null) {
                JOptionPane.showMessageDialog(this, "Failed to reserve table.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Reservation created! ID = " + r.getReservationId());
            }

            refreshTable();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }

    /* -------------------------
         REFRESH TABLE
       ------------------------- */

    private void refreshTable() {
        model.setRowCount(0);

        for (Reservation r : reservationService.getAllReservations()) {
            model.addRow(new Object[]{
                    r.getReservationId(),
                    r.getTableNumber(),
                    fmt.format(r.getStart()),
                    fmt.format(r.getEnd())
            });
        }
    }
}
