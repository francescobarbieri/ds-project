package it.unimib.sd2024.handlers;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import it.unimib.sd2024.model.Order;

public class OrderHandler {
    private final File orderrFile = new File("resources/domains.json");
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
            default:
                return "ERROR: Unknown command";
        }
    }

    private String set(String[] parts) {
        //TODO: completare la stringa di errore
        if(parts.length != 9) return "ERROR: SET command must be in the format: ORDER SET";

        String domain = parts[2];
        String userId = parts[3];
        String price = parts[4];
        long date = Long.parseLong(parts[5]);
        String cvv = parts[6];
        String cardNumber = parts[7];
        String operationType = parts[8];

        orders.put(orders.size(), new Order(domain, userId, price, date, cvv, cardNumber, operationType));
        saveOrders();
        return "OK\n";
    }

    private String get (String[] parts) {
        //TODO: finish this
        return "Temp";
    }

    private void loadOrders() {
        if(orderrFile.exists()) {
            try {
                Map<Integer, Order> loadedOrders = objectMapper.readValue(orderrFile, new TypeReference<Map<Integer, Order>>() {});
                orders.putAll(loadedOrders);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveOrders() {
        try {
            objectMapper.writeValue(orderrFile, orders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
