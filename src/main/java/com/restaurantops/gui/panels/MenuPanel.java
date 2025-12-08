package com.restaurantops.gui.panels;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Recipe;
import com.restaurantops.service.MenuService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MenuPanel extends JPanel {

    private final MenuService menuService;

    private JTable menuTable;

    public MenuPanel() {

        this.menuService = RestaurantEngine.getInstance().getMenuService();

        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        add(buildMenuTable(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        // refresh safely on EDT
        SwingUtilities.invokeLater(this::refreshTable);
    }

    /* ============================
            MENU TABLE
       ============================ */

    private JScrollPane buildMenuTable() {

        menuTable = new JTable(new DefaultTableModel(
                new Object[]{
                        "ID", "Name", "Category", "Price",
                        "Calories", "Allergens", "Ingredients"
                },
                0
        ));

        menuTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        return new JScrollPane(menuTable);
    }


    /* ============================
            BUTTON PANEL
       ============================ */

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshBtn = new JButton("Refresh");
        JButton addBtn = new JButton("Add Item");
        JButton editBtn = new JButton("Edit Item");
        JButton deleteBtn = new JButton("Delete Item");

        refreshBtn.addActionListener(e -> SwingUtilities.invokeLater(this::refreshTable));
        addBtn.addActionListener(e -> SwingUtilities.invokeLater(this::onAddItem));
        editBtn.addActionListener(e -> SwingUtilities.invokeLater(this::onEditItem));
        deleteBtn.addActionListener(e -> SwingUtilities.invokeLater(this::onDeleteItem));

        panel.add(refreshBtn);
        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);

        return panel;
    }

    /* ============================
            TABLE POPULATION
       ============================ */

    private void refreshTable() {

        DefaultTableModel model = (DefaultTableModel) menuTable.getModel();
        model.setRowCount(0);

        List<MenuItem> items = menuService.getAllItems();
        for (MenuItem item : items) {

            Recipe r = item.getRecipe();
            String ingredients = (r == null)
                    ? "None"
                    : r.getIngredients().entrySet().stream()
                    .map(e -> e.getKey() + "x" + e.getValue())
                    .collect(Collectors.joining(", "));

            String allergens = item.getAllergens().isEmpty()
                    ? "None"
                    : String.join(", ", item.getAllergens());

            model.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    String.format("₹%.2f", item.getPrice()),
                    item.getCalories(),
                    allergens,
                    ingredients
            });
        }
    }

    /* ============================
            CRUD HANDLERS
       ============================ */

    private void onAddItem() {

        AddEditResult res = showAddEditDialog(null);
        if (res == null) return; // cancelled

        int id = menuService.getNextId();
        MenuItem item = new MenuItem(
                id,
                res.name,
                res.price,
                res.category,
                res.recipe,
                res.allergens,
                res.calories
        );

        boolean ok = menuService.addMenuItem(item);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to add item (id conflict).");
        } else {
            JOptionPane.showMessageDialog(this, "Item added.");
            refreshTable();
        }
    }

    private void onEditItem() {
        int row = menuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item to edit.");
            return;
        }

        int id = (int) menuTable.getValueAt(row, 0);
        MenuItem existing = menuService.getById(id);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "Selected item not found.");
            return;
        }

        AddEditResult res = showAddEditDialog(existing);
        if (res == null) return;

        MenuItem updated = new MenuItem(
                existing.getId(),
                res.name,
                res.price,
                res.category,
                res.recipe,
                res.allergens,
                res.calories
        );

        boolean ok = menuService.editMenuItem(updated);
        JOptionPane.showMessageDialog(this, ok ? "Updated." : "Update failed.");
        refreshTable();
    }

    private void onDeleteItem() {
        int row = menuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item to delete.");
            return;
        }

        int id = (int) menuTable.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete menu item ID " + id + " ?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = menuService.removeMenuItem(id);
        JOptionPane.showMessageDialog(this, ok ? "Deleted." : "Delete failed.");
        refreshTable();
    }

    /* ============================
            ADD / EDIT DIALOG
       ============================ */

    private AddEditResult showAddEditDialog(MenuItem existing) {

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.anchor = GridBagConstraints.WEST;

        JTextField nameField = new JTextField(20);
        JTextField priceField = new JTextField(8);
        JTextField categoryField = new JTextField(12);
        JComboBox<String> recipeBox;
        JTextField allergensField = new JTextField(20);
        JSpinner caloriesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 10));

        // recipe choices from MenuService -> recipeService
        Map<String, Recipe> recipes = menuService.getAllRecipes();
        List<String> recipeNames = new ArrayList<>(recipes.keySet());
        recipeNames.sort(String::compareToIgnoreCase);
        recipeBox = new JComboBox<>(recipeNames.toArray(new String[0]));
        recipeBox.insertItemAt("<none>", 0);

        if (existing != null) {
            nameField.setText(existing.getName());
            priceField.setText(String.valueOf(existing.getPrice()));
            categoryField.setText(existing.getCategory());
            allergensField.setText(String.join(", ", existing.getAllergens()));
            caloriesSpinner.setValue(existing.getCalories());
            Recipe r = existing.getRecipe();
            if (r != null) {
                recipeBox.setSelectedItem(r.getDishName().toLowerCase());
            } else {
                recipeBox.setSelectedIndex(0);
            }
        }

        int y = 0;
        gc.gridx = 0; gc.gridy = y; p.add(new JLabel("Name:"), gc);
        gc.gridx = 1; p.add(nameField, gc);

        y++;
        gc.gridx = 0; gc.gridy = y; p.add(new JLabel("Price (₹):"), gc);
        gc.gridx = 1; p.add(priceField, gc);

        y++;
        gc.gridx = 0; gc.gridy = y; p.add(new JLabel("Category:"), gc);
        gc.gridx = 1; p.add(categoryField, gc);

        y++;
        gc.gridx = 0; gc.gridy = y; p.add(new JLabel("Recipe:"), gc);
        gc.gridx = 1; p.add(recipeBox, gc);

        y++;
        gc.gridx = 0; gc.gridy = y; p.add(new JLabel("Allergens (comma):"), gc);
        gc.gridx = 1; p.add(allergensField, gc);

        y++;
        gc.gridx = 0; gc.gridy = y; p.add(new JLabel("Calories:"), gc);
        gc.gridx = 1; p.add(caloriesSpinner, gc);

        int result = JOptionPane.showConfirmDialog(this, p,
                existing == null ? "Add Menu Item" : "Edit Menu Item",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return null;

        String name = nameField.getText().trim();
        double price;
        try {
            price = Double.parseDouble(priceField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid price.");
            return null;
        }
        String category = categoryField.getText().trim();
        String recipeChoice = (String) recipeBox.getSelectedItem();
        Recipe recipe = null;
        if (recipeChoice != null && !recipeChoice.equals("<none>")) {
            recipe = menuService.getRecipeByName(recipeChoice);
            if (recipe == null) {
                // try exact key forms (some recipes stored lowercase)
                recipe = menuService.getRecipeByName(recipeChoice.toLowerCase());
            }
        }

        String allergText = allergensField.getText().trim();
        List<String> allergens = new ArrayList<>();
        if (!allergText.isEmpty()) {
            String[] parts = allergText.split(",");
            for (String s : parts) {
                String t = s.trim();
                if (!t.isEmpty()) allergens.add(t);
            }
        }

        int calories = (int) ((Integer) caloriesSpinner.getValue());

        return new AddEditResult(name, price, category, recipe, allergens, calories);
    }

    private static class AddEditResult {
        final String name;
        final double price;
        final String category;
        final Recipe recipe;
        final List<String> allergens;
        final int calories;
        AddEditResult(String name, double price, String category, Recipe recipe, List<String> allergens, int calories) {
            this.name = name;
            this.price = price;
            this.category = category;
            this.recipe = recipe;
            this.allergens = Collections.unmodifiableList(new ArrayList<>(allergens));
            this.calories = calories;
        }
    }
}
