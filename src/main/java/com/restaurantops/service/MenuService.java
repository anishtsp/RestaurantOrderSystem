package com.restaurantops.service;

import com.restaurantops.model.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenuService {

    private final List<MenuItem> items = new ArrayList<>();

    public MenuService() {
        items.add(new MenuItem(1, "Pasta", 150));
        items.add(new MenuItem(2, "Pizza", 220));
        items.add(new MenuItem(3, "Burger", 120));
        items.add(new MenuItem(4, "Lemonade", 60));
        items.add(new MenuItem(5, "Gulab Jamun", 80));
    }

    public List<MenuItem> getAllItems() {
        return Collections.unmodifiableList(items);
    }

    public MenuItem getById(int id) {
        return items.stream()
                .filter(m -> m.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
