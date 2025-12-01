package com.restaurantops;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.customer.CustomerView;
import com.restaurantops.restaurant.RestaurantView;

import java.util.Scanner;

public class MainLauncher {

    public static void main(String[] args) {

        // Start the backend engine (kitchen + inventory threads)
        RestaurantEngine engine = RestaurantEngine.getInstance();
        engine.start();

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        System.out.println("=== Welcome to RestaurantOps ===");

        while (running) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Customer View");
            System.out.println("2. Restaurant View");
            System.out.println("3. Exit Entire System");
            System.out.print("Choice: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> {
                    CustomerView cv = new CustomerView(engine);
                    cv.run();
                }
                case "2" -> {
                    RestaurantView rv = new RestaurantView(engine);
                    rv.run();
                }
                case "3" -> running = false;
                default -> System.out.println("Invalid option. Try again.");
            }
        }

        System.out.println("\nShutting down backend threads...");
        engine.stop();
        System.out.println("System terminated. Goodbye!");
    }
}
