package it.unimib.sd2024.handlers;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

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

        if (users.containsKey(email)) {
            return "ERROR: User with email " + email + " already exists\n";
        }

        String id = UUID.randomUUID().toString().substring(0, 4);

        users.put(email, new User(id, email, name, surname));
        saveUsers();
        return "OK, user ID: " + id + "\n";
    }

    private String get(String[] parts) {
        if (parts.length != 3) return "ERROR: GET command must be in the format: GET user <key>\n";
        String value = "Test";
        return value != null ? value + "\n" : "NULL\n";
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
}
