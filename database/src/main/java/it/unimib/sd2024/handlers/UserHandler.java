package it.unimib.sd2024.handlers;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import it.unimib.sd2024.model.User;

/**
 * Handler class for managing user-related commands.
 */
public class UserHandler {
    private final File userFile = new File("resources/users.json"); // File where users are stored
    private final ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper instance for JSON
    private final Map<String, User> users = new ConcurrentHashMap<>();  // ConcurrentHashMap to store user data in memory
    
    /**
     * Constructor that initializes the UserHandler and loads users from file.
     */
    public UserHandler() {
        loadUsers(); // Load users from the JSON file into the users map
    }

    /**
     * Handles user-related commands.
     *
     * @param parts the command parts
     * @return the response string
     */
    public String handle(String[] parts) {
        // Check if the command has at least 2 parts 
        if(parts.length < 2) return "ERROR: Invalid command. \n";
        // Get the specific user command
        String command = parts[1].toUpperCase();

        // Handle the command based on its type
        switch (command) {
            case "SET":
                return set(parts);
            case "GET":
                return get(parts);
            default:
                return "ERROR: Unknown user command. \n";
        }
    }

    /**
     * Handles the SET command to create a new user.
     *
     * @param parts the command parts
     * @return the response string
     */
    private String set(String[] parts) {
        // Check if the SET command has the correct number of parts
        if (parts.length != 5) return "ERROR: SET command must be in the format: SET user <key> <value>\n";

        // Extract user details from the command parts
        String email = parts[2];
        String name = parts[3];
        String surname = parts[4];

        // Generate a unique ID for the user based on the email
        String id = generateUniqeId(email);

        // Check if a user with the same ID already exists
        if (users.containsKey(id)) {
            System.out.println("ERROR: User with email " + email + " already exists");
            return "ERROR: User with email " + email + " already exists\n";
        }

        // Create a new user and add it to the users map
        users.put(id, new User(id, email, name, surname));
        saveUsers(); // Save the updated users map to the JSON file
        return "OK, user ID: " + id + "\n";
    }

    /**
     * Handles the GET command to retrieve user details.
     *
     * @param parts the command parts
     * @return the response string
     */
    private String get(String[] parts) {
        // Check if the GET command has the correct number of parts
        if (parts.length != 3) return "ERROR: GET command must be in the format: GET user <key>\n";
        User user = users.get(parts[2]);

        // Get the user by ID from the users map
        if(user != null) {
            return user.toJSON() + "\n"; // Return the user details in JSON format
        } else {
            return "NULL\n";  // Return NULL if the user is not found
        }
    }

    /**
     * Loads users from the JSON file into the users map.
     */
    private void loadUsers() {
        // Check if the user file exists
        if(userFile.exists()) {
            try {
                // Read users from the JSON file and put them into the users ma
                Map<String, User> loadedUsers = objectMapper.readValue(userFile, new TypeReference<Map<String, User>>() {});
                users.putAll(loadedUsers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves the current users map to the JSON file.
     */
    private void saveUsers() {
        try {
            // Write the users map to the JSON file
            objectMapper.writeValue(userFile, users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate a unique ID for a user based on its email.
     */
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
