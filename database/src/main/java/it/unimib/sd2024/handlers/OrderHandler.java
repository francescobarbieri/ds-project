package it.unimib.sd2024.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import it.unimib.sd2024.model.Order;

/**
 * Handler class for managing order-related commands.
 */
public class OrderHandler {
    private final File orderFile = new File("resources/orders.json"); // File where orders are stored
    private final ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper instance for JSON
    private final Map<Integer, Order> orders = new ConcurrentHashMap<>(); // ConcurrentHashMap to store order data in memory

    /**
     * Constructor that initializes the OrderHandler and loads orders from file.
     */
    public OrderHandler() {
        loadOrders(); // Load orders from the JSON file into the orders map
    }

    /**
     * Handles order-related commands.
     *
     * @param parts the command parts
     * @return the response string
     */
    public String handle(String[] parts) {
        // Check if the command has at least 2 parts
        if(parts.length < 2) return "ERROR: Invalid command.\n";
        // Get the specific order command
        String command = parts[1].toUpperCase();

        // Handle the command based on its type
        switch (command) {
            case "SET":
                return set(parts);
            case "GETALL":
                return getall(parts);
            default:
                return "ERROR: Unknown command\n";
        }
    }

    /**
     * Handles the SET command to create a new order.
     *
     * @param parts the command parts
     * @return the response string
     */
    private String set(String[] parts) {
        // Check if the SET command has the correct number of parts
        if(parts.length != 10) {
            System.out.println("ERROR: SET command must be in the format: ORDER SET <domain> <userId> <price> <currentDate> <cvv> <cardNumber> <operationType> <accountHolder>");
            return "ERROR: SET command must be in the format: ORDER SET <domain> <userId> <price> <currentDate> <cvv> <cardNumber> <operationType> <accountHolder>\n";
        } 

        // Extract order details from the command parts
        String domain = parts[2];
        String userId = parts[3];
        String price = parts[4];
        long date = Long.parseLong(parts[5]);
        String cvv = parts[6];
        String cardNumber = parts[7];
        String operationType = parts[8];
        String accountHolder = parts[9];

        // Add the new order to the orders map
        orders.put(orders.size(), new Order(domain, userId, price, date, cvv, cardNumber, operationType, accountHolder));
        saveOrders(); // Save the updated orders map to the JSON file
        return "OK\n";
    }

    /**
     * Handles the GETALL command to retrieve orders.
     *
     * @param parts the command parts
     * @return the response string
     */
    private String getall (String[] parts) {
        try {
            // Returns all orders if no userId is specified
            if(parts.length == 2) {
                List<Order> orderList = new ArrayList<>(orders.values());
                return objectMapper.writeValueAsString(orderList) + "\n";
            }
            // Returns orders for a specific userId if provided
            else if (parts.length == 3) {
                String userId = parts[2];

                List<Order> orderList = orders.values().stream()
                    .filter(order -> userId.equals(order.getUserId()))
                    .collect(Collectors.toList());
                
                return objectMapper.writeValueAsString(orderList) + "\n";
            } else {
                return "ERROR: GETALL command must be in the format: ORDER GETALL <optional: userId> \n";
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "Unknown error\n";
    }

    /**
     * Loads orders from the JSON file into the orders map.
     */
    private void loadOrders() {
        // Check if the order file exists
        if(orderFile.exists()) {
            try {
                // Read orders from the JSON file and put them into the orders map
                Map<Integer, Order> loadedOrders = objectMapper.readValue(orderFile, new TypeReference<Map<Integer, Order>>() {});
                orders.putAll(loadedOrders);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves the current orders map to the JSON file.
     */
    public void saveOrders() {
        try {
             // Write the orders map to the JSON file
            objectMapper.writeValue(orderFile, orders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
