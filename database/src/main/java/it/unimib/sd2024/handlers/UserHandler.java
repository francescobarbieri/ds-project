package it.unimib.sd2024.handlers;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import it.unimib.sd2024.model.Domain;
import it.unimib.sd2024.model.User;

public class UserHandler {
    private final File userFile = new File("resources/users.json");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public UserHandler() {
        loadUsers();
    }

    public String handle(String[] parts) {
        if(parts.length < 2) return "ERROR: Invalid command. \n";
        String command = parts[1].toUpperCase();

        switch (command) {
            case "SET":
                return set(parts);
            case "GET":
                return get(parts);
            case "DELETE":
                return delete(parts);
            default:
                return "ERROR: Unknown user command. \n";
        }
    }

    private String set(String[] parts) {
        if (parts.length != 5) return "ERROR: SET command must be in the format: SET user <key> <value>\n";

        String email = parts[2];
        String name = parts[3];
        String surname = parts[4];

        String id = generateUniqeId(email);

        if (users.containsKey(id)) {
            System.out.println("ERROR: User with email " + email + " already exists");
            return "ERROR: User with email " + email + " already exists\n";
        }

        users.put(id, new User(id, email, name, surname));
        saveUsers();
        return "OK, user ID: " + id + "\n";
    }

    private String get(String[] parts) {
        if (parts.length != 3) return "ERROR: GET command must be in the format: GET user <key>\n";
        User user = users.get(parts[2]);
        if(user != null) {
            return user.toJSON() + "\n";
        } else {
            return "NULL\n";
        }
    }

    private String delete(String[] parts) {
        if (parts.length != 3) return "ERROR: DELETE command must be in the format: DELETE user <key>\n";
        return "OK\n";
    }

    private void loadUsers() {
        if(userFile.exists()) {
            try {
                Map<String, User> loadedUsers = objectMapper.readValue(userFile, new TypeReference<Map<String, User>>() {});
                users.putAll(loadedUsers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveUsers() {
        try {
            objectMapper.writeValue(userFile, users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateUniqeId(String input) {
        try {
            // Create a SHA-256 hash of the input string
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert the hash bytes to a BigInteger
            BigInteger hasInteger = new BigInteger(1, hashBytes);

            // Get the absolute value to ensure no negative numbers
            String haString = hasInteger.toString();

            // Use modulo operation to get a 5-digit number
            int uniqueId = Math.abs(haString.hashCode()) % 100000;

            // Ensure the uniqueId is always 5 digits by adding leading zeros if necessary
            return String.format("%05d", uniqueId);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating a unique id, " + e);
        }
    }
}
