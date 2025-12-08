package com.restaurantops.gui.panels;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.Table;
import com.restaurantops.model.Waiter;
import com.restaurantops.service.TableService;
import com.restaurantops.service.WaiterService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TablesWaitersPanel extends JPanel {

    private final RestaurantEngine engine;
    private final TableService tableService;
    private final WaiterService waiterService;

    private JTable tableGrid;
    private DefaultTableModel tableModel;

    private JComboBox<String> waiterDropdown;

    private JLabel lblTable, lblCapacity, lblState, lblWaiter, lblReservation, lblMerged;

    private JSpinner addTableNum;
    private JSpinner removeTableNum;

    private JSpinner addWaiterId;
    private JTextField addWaiterName;
    private JSpinner removeWaiterId;

    public TablesWaitersPanel() {

        this.engine = RestaurantEngine.getInstance();
        this.tableService = engine.getTableService();
        this.waiterService = engine.getWaiterService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildMainSplit(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        refreshTableList();
        refreshWaiterList();
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Tables & Waiters");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> {
            refreshTableList();
            refreshWaiterList();
        });

        p.add(title, BorderLayout.WEST);
        p.add(refresh, BorderLayout.EAST);

        return p;
    }

    private JSplitPane buildMainSplit() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        split.setLeftComponent(buildTableListPanel());
        split.setRightComponent(buildTableDetailPanel());
        split.setResizeWeight(0.6);

        return split;
    }

    /* ----------------------------------------------------
                       TABLE LIST PANEL
       ---------------------------------------------------- */

    private JScrollPane buildTableListPanel() {

        tableModel = new DefaultTableModel(
                new Object[]{"Table", "Cap", "State", "Waiter", "ResID", "Merged"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tableGrid = new JTable(tableModel);
        tableGrid.getSelectionModel().addListSelectionListener(e -> showSelectedTable());

        return new JScrollPane(tableGrid);
    }

    private void refreshTableList() {
        tableModel.setRowCount(0);

        for (Table t : tableService.listTables()) {

            String waiterStr = t.getAssignedWaiter()
                    .map(w -> w.getWaiterId() + ": " + w.getName())
                    .orElse("-");

            String res = t.getCurrentReservation()
                    .map(r -> String.valueOf(r.getReservationId()))
                    .orElse("-");

            String merged = t.isMerged()
                    ? t.getMergedFrom().toString()
                    : "-";

            tableModel.addRow(new Object[]{
                    t.getTableNumber(),
                    t.getCapacity(),
                    t.getState(),
                    waiterStr,
                    res,
                    merged
            });
        }
    }

    /* ----------------------------------------------------
                     TABLE DETAIL + ACTIONS PANEL
       ---------------------------------------------------- */

    private JPanel buildTableDetailPanel() {

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Table Details");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        p.add(title);
        p.add(Box.createVerticalStrut(10));

        lblTable      = addDetail(p, "Table:");
        lblCapacity   = addDetail(p, "Capacity:");
        lblState      = addDetail(p, "State:");
        lblWaiter     = addDetail(p, "Waiter:");
        lblReservation= addDetail(p, "Reservation:");
        lblMerged     = addDetail(p, "Merged:");

        p.add(Box.createVerticalStrut(15));
        p.add(buildActionButtons());
        p.add(Box.createVerticalStrut(20));
        p.add(buildWaiterAssignPanel());

        return p;
    }

    private JLabel addDetail(JPanel parent, String label) {
        JPanel row = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(label);
        JLabel value = new JLabel("-");

        lbl.setPreferredSize(new Dimension(100, 20));
        row.add(lbl, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);

        parent.add(row);
        parent.add(Box.createVerticalStrut(5));

        return value;
    }

    private JPanel buildActionButtons() {
        JPanel p = new JPanel(new GridLayout(3, 1, 5, 5));

        JButton occupy = new JButton("Occupy");
        JButton release = new JButton("Release");
        JButton clean = new JButton("Clean");

        occupy.addActionListener(e -> applyToSelectedTable(t -> tableService.occupyTable(t)));
        release.addActionListener(e -> applyToSelectedTable(t -> tableService.releaseTable(t)));
        clean.addActionListener(e -> applyToSelectedTable(t -> tableService.cleanTable(t)));

        p.add(occupy);
        p.add(release);
        p.add(clean);

        return p;
    }

    private JPanel buildWaiterAssignPanel() {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Assign Waiter"));
        p.setLayout(new BorderLayout());

        waiterDropdown = new JComboBox<>();

        JButton assign = new JButton("Assign");
        JButton unassign = new JButton("Unassign");

        assign.addActionListener(e -> assignWaiter());
        unassign.addActionListener(e -> unassignWaiter());

        JPanel btnPanel = new JPanel();
        btnPanel.add(assign);
        btnPanel.add(unassign);

        p.add(waiterDropdown, BorderLayout.NORTH);
        p.add(btnPanel, BorderLayout.SOUTH);

        return p;
    }

    private void showSelectedTable() {
        int row = tableGrid.getSelectedRow();
        if (row == -1) return;

        lblTable.setText(String.valueOf(tableGrid.getValueAt(row, 0)));
        lblCapacity.setText(String.valueOf(tableGrid.getValueAt(row, 1)));
        lblState.setText(String.valueOf(tableGrid.getValueAt(row, 2)));
        lblWaiter.setText(String.valueOf(tableGrid.getValueAt(row, 3)));
        lblReservation.setText(String.valueOf(tableGrid.getValueAt(row, 4)));
        lblMerged.setText(String.valueOf(tableGrid.getValueAt(row, 5)));
    }

    private void applyToSelectedTable(TableAction action) {
        int row = tableGrid.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a table first.");
            return;
        }

        int table = (int) tableGrid.getValueAt(row, 0);

        boolean ok = action.apply(table);

        JOptionPane.showMessageDialog(this,
                ok ? "Success" : "Failed");

        refreshTableList();
    }

    private void assignWaiter() {
        int row = tableGrid.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a table.");
            return;
        }

        if (waiterDropdown.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "No waiter selected.");
            return;
        }

        int table = (int) tableGrid.getValueAt(row, 0);

        String selected = (String) waiterDropdown.getSelectedItem();
        int waiterId = Integer.parseInt(selected.split(":")[0]);

        Waiter w = waiterService.getWaiter(waiterId);
        boolean ok = tableService.assignWaiterToTable(table, w);

        JOptionPane.showMessageDialog(this, ok ? "Assigned" : "Failed");

        refreshTableList();
        refreshWaiterList();
    }

    private void unassignWaiter() {
        int row = tableGrid.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a table.");
            return;
        }

        int table = (int) tableGrid.getValueAt(row, 0);

        boolean ok = tableService.unassignWaiterFromTable(table);

        JOptionPane.showMessageDialog(this, ok ? "Unassigned" : "Failed");

        refreshTableList();
        refreshWaiterList();
    }

    private void refreshWaiterList() {
        waiterDropdown.removeAllItems();

        List<Waiter> list = waiterService.getWaiters();
        for (Waiter w : list) {
            waiterDropdown.addItem(w.getWaiterId() + ": " + w.getName());
        }
    }

    /* ----------------------------------------------------
                    BOTTOM FORM: ADD/REMOVE
       ---------------------------------------------------- */

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 10));

        panel.add(buildAddRemoveTablesPanel());
        panel.add(buildAddRemoveWaitersPanel());

        return panel;
    }

    private JPanel buildAddRemoveTablesPanel() {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Manage Tables"));
        p.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.anchor = GridBagConstraints.WEST;

        addTableNum = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));

        JButton addBtn = new JButton("Add Table");
        addBtn.addActionListener(e -> {
            int t = (int) addTableNum.getValue();
            boolean ok = tableService.addTable(t);
            JOptionPane.showMessageDialog(this, ok ? "Added" : "Exists / Failed");
            refreshTableList();
        });

        removeTableNum = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        JButton rmBtn = new JButton("Remove Table");
        rmBtn.addActionListener(e -> {
            int t = (int) removeTableNum.getValue();
            boolean ok = tableService.removeTable(t);
            JOptionPane.showMessageDialog(this, ok ? "Removed" : "Failed");
            refreshTableList();
        });

        gc.gridx = 0; gc.gridy = 0;
        p.add(new JLabel("Table #:"), gc);
        gc.gridx = 1; p.add(addTableNum, gc);

        gc.gridx = 0; gc.gridy = 1;
        gc.gridwidth = 2;
        p.add(addBtn, gc);

        gc.gridwidth = 1;
        gc.gridx = 0; gc.gridy = 2;
        p.add(new JLabel("Remove #:"), gc);
        gc.gridx = 1; p.add(removeTableNum, gc);

        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        p.add(rmBtn, gc);

        return p;
    }

    private JPanel buildAddRemoveWaitersPanel() {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Manage Waiters"));
        p.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.anchor = GridBagConstraints.WEST;

        addWaiterId = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        addWaiterName = new JTextField(10);

        JButton addBtn = new JButton("Add Waiter");
        addBtn.addActionListener(e -> {
            int id = (int) addWaiterId.getValue();
            String name = addWaiterName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.");
                return;
            }
            boolean ok = waiterService.addWaiter(id, name);
            JOptionPane.showMessageDialog(this, ok ? "Added" : "Exists / Failed");
            refreshWaiterList();
        });

        removeWaiterId = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        JButton rmBtn = new JButton("Remove Waiter");
        rmBtn.addActionListener(e -> {
            int id = (int) removeWaiterId.getValue();
            boolean ok = waiterService.removeWaiter(id);
            JOptionPane.showMessageDialog(this, ok ? "Removed" : "Not Found");
            refreshWaiterList();
        });

        gc.gridx = 0; gc.gridy = 0;
        p.add(new JLabel("Waiter ID:"), gc);
        gc.gridx = 1; p.add(addWaiterId, gc);

        gc.gridx = 0; gc.gridy = 1;
        p.add(new JLabel("Name:"), gc);
        gc.gridx = 1; p.add(addWaiterName, gc);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        p.add(addBtn, gc);

        gc.gridwidth = 1;
        gc.gridx = 0; gc.gridy = 3;
        p.add(new JLabel("Remove ID:"), gc);
        gc.gridx = 1; p.add(removeWaiterId, gc);

        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        p.add(rmBtn, gc);

        return p;
    }

    /* Functional interface for table actions */
    private interface TableAction {
        boolean apply(int table);
    }
}
