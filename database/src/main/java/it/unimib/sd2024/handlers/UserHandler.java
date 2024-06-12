package it.unimib.sd2024.handlers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserHandler {
    private final Map<String, String> users = new ConcurrentHashMap<>();

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
        if (parts.length != 4) return "ERROR: SET command must be in the format: SET user <key> <value>\n";
        users.put(parts[2], parts[3]);
        return "OK\n";
    }

    private String get(String[] parts) {
        if (parts.length != 3) return "ERROR: GET command must be in the format: GET user <key>\n";
        String value = users.get(parts[2]);
        return value != null ? value + "\n" : "NULL\n";
    }

    private String delete(String[] parts) {
        if (parts.length != 3) return "ERROR: DELETE command must be in the format: DELETE user <key>\n";
        users.remove(parts[2]);
        return "OK\n";
    }
}
