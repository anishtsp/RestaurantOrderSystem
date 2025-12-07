package com.restaurantops.test;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.model.MenuItem;
import com.restaurantops.model.Order;
import com.restaurantops.model.Customization;
import com.restaurantops.service.TableService;
import com.restaurantops.service.WaiterService;
import com.restaurantops.service.OrderService;
import com.restaurantops.service.MenuService;
import com.restaurantops.model.Waiter;

public class TestPhase5Flow {

    public static void main(String[] args) throws Exception {
        RestaurantEngine engine = RestaurantEngine.getInstance();
        engine.start();

        WaiterService waiterService = engine.getWaiterService();
        waiterService.addWaiter(1, "Asha");
        waiterService.addWaiter(2, "Ravi");

        TableService tableService = engine.getTableService();
        tableService.addTable(1);
        tableService.addTable(2);

        tableService.occupyTable(1);

        MenuService menu = engine.getMenuService();
        MenuItem item = menu.getById(3);
        if (item == null) item = menu.getAllItems().get(0);

        OrderService orderService = engine.getOrderService();
        Customization c = new Customization(false, 1, "none");
        Order order = new Order(1, item, 2, c);
        orderService.placeOrder(order);

        Thread.sleep(4000);

        System.out.println("Test finished.");
        engine.stop();
    }
}
