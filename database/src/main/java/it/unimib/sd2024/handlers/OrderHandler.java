package it.unimib.sd2024.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import it.unimib.sd2024.model.Order;

public class OrderHandler {
    private final File orderFile = new File("resources/orders.json");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Integer, Order> orders = new ConcurrentHashMap<>();

    public OrderHandler() {
        loadOrders();
    }

    public String handle(String[] parts) {
        if(parts.length < 2) return "ERROR: Invalid command.\n";
        String command = parts[1].toUpperCase();

        switch (command) {
            case "SET":
                return set(parts);
            case "GET":
                return get(parts);
            case "GETALL":
                return getall(parts);
            default:
                return "ERROR: Unknown command\n";
        }
    }

    private String set(String[] parts) {
        //TODO: completare la stringa di errore
        if(parts.length != 10) {
            System.out.println("ERROR: SET command must be in the format: ORDER SET");
            return "ERROR: SET command must be in the format: ORDER SET\n";
        } 

        String domain = parts[2];
        String userId = parts[3];
        String price = parts[4];
        long date = Long.parseLong(parts[5]);
        String cvv = parts[6];
        String cardNumber = parts[7];
        String operationType = parts[8];
        String accountHolder = parts[9];

        orders.put(orders.size(), new Order(domain, userId, price, date, cvv, cardNumber, operationType, accountHolder));
        saveOrders();
        return "OK\n";
    }

    private String get (String[] parts) {
        //TODO: finish this
        return "Temp\n";
    }

    private String getall (String[] parts) {
        try {
            // Returns all orders
            if(parts.length == 2) {
                List<Order> orderList = new ArrayList<>(orders.values());
                return objectMapper.writeValueAsString(orderList) + "\n";
            }
            // Return only userId's orders
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

    private void loadOrders() {
        if(orderFile.exists()) {
            try {
                Map<Integer, Order> loadedOrders = objectMapper.readValue(orderFile, new TypeReference<Map<Integer, Order>>() {});
                orders.putAll(loadedOrders);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveOrders() {
        try {
            objectMapper.writeValue(orderFile, orders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
