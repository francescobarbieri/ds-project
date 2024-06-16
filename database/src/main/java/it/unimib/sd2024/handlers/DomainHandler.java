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

import it.unimib.sd2024.model.Domain;

/**
 * Handler class for managing domain-related commands.
 */
public class DomainHandler {
    private final File domainFile = new File("resources/domains.json"); // File where domains are stored
    private final ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper instance for JSON 
    private final Map<String, Domain> domains = new ConcurrentHashMap<>(); // ConcurrentHashMap to store domain data in memory

    /**
     * Constructor that initializes the DomainHandler and loads domains from file.
     */
    public DomainHandler() {
        loadDomains();  // Load domains from the JSON file into the domains map
    }

    /**
     * Handles domain-related commands.
     *
     * @param parts the command parts
     * @return the response string
     */
    public String handle(String[] parts) {
        // Check if the command has at least 2 parts
        if(parts.length < 2) return "ERROR: Invalid command. \n";
        // Get the specific domain command
        String command = parts[1].toUpperCase();

        // Handle the command based on its type
        switch (command) {
            case "SET":
                return set(parts);
            case "CHECK":
                return check(parts);
            case "GET":
                return get(parts);
            case "GETALL":
                return getall(parts);
            case "UPDATE":
                return update(parts);
            default:
                return "ERROR: Unknown command. \n";
        }
    }

    /**
     * Handles the SET command to add a new domain.
     *
     * @param parts the command parts
     * @return the response string
     */        
    private String set(String[] parts) {
        // Check if the SET command has the correct number of parts
        if(parts.length != 6) 
            return "ERROR: SET command must be in the format: DOMAIN SET <name> <userid> <now> <expirydate>\n";

        // Extract domain details from the command parts
        String name = parts[2];
        String userid = parts[3];
        long now = Long.parseLong(parts[4]);
        long expiryDate = Long.parseLong(parts[5]);

        // Check if the domain already exists
        if(domains.containsKey(name)) {
            System.out.println("ERROR: Domain " + name + " already exists");
            return "ERROR: Domain " + name + " already exists\n";
        }

        // Add the new domain to the domains map
        domains.put(name, new Domain(name, userid, expiryDate, now, false));
        saveDomains();
        return "OK\n";
    }

    /**
     * Handles the CHECK command to check domain availability.
     *
     * @param parts the command parts
     * @return the response string
     */
    private String check(String[] parts) {
        // Check if the CHECK command has the correct number of parts
        if(parts.length != 3)
            return "ERROR: CHECK command must be in the format: DOMAIN CHECK <name>\n";
        
        // Extract domain name from the command parts
        String name = parts[2];

        Domain domain = domains.get(name);

        // Check if the domain exists and is available
        if(domain == null) {
            System.out.println("Domain " + name + " is available");
            return "OK\n";
        //TODO: check expiration date
        //} else if (System.currentTimeMillis() > domain.getExpiryDate()) {
        //    System.out.println("Domain " + name + " is available 2");
        //    return "OK\n";
        } else {
            System.out.println("ERROR: Domain " + name + " is not available");
            return "ERROR: Domain " + name + " is not available\n";
        }
    }

    /**
     * Handles the GET command to retrieve domain details.
     *
     * @param parts the command parts
     * @return the response string
     */
    private String get(String[] parts) {
        // Check if the GET command has the correct number of parts
        if(parts.length != 3)
            return "ERROR: GET command must be in the format: DOMAIN GET <domainName> \n";

        // Extract domain name from the command parts
        String name = parts[2];

        Domain domain = domains.get(name);

        // Check if the domain exists
        if(domain == null) {
            return "ERROR: Domain not found\n";
        } else {
            try {
                return objectMapper.writeValueAsString(domain) + "\n"; // Return domain details in JSON format
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return "ERROR: Unable to process the request\n";
            }
        }
    }

    /**
     * Handles the GETALL command to retrieve all domains or domains for a specific user.
     *
     * @param parts the command parts
     * @return the response string
     */
    private String getall(String[] parts) {
        try {
            // Returns all domains if no userId is specified
            if(parts.length == 2) {
                List<Domain> domainList = new ArrayList<>(domains.values());
                return objectMapper.writeValueAsString(domainList) + "\n";
            }
            // Returns domains for a specific userId if provided
            else if (parts.length == 3) {
                String userId = parts[2];

                List<Domain> domainList = domains.values().stream()
                    .filter(domain -> userId.equals(domain.getUserId()))
                    .collect(Collectors.toList());

                return objectMapper.writeValueAsString(domainList) + "\n";
            } else {
                return "ERROR: GETALL command must be in the format: DOMAIN GETALL <optional: userId> \n";
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "Unknown error\n";
    }

    /**
     * Handles the UPDATE command to update domain expiry date.
     *
     * @param parts the command parts
     * @return the response string
     */
    private String update(String[] parts) {
        // Check if the UPDATE command has the correct number of parts
        if(parts.length != 4)
            return "ERROR: UPDATE command must be in the format: DOMAIN UPDATE <domainname> <expirydate> \n";
        
        // Extract domain name and expiry date from the command parts
        String name = parts[2];
        long expiryDate = Long.parseLong(parts[3]);
        
        Domain domain = domains.get(name);

        // Check if the domain exists
        if(domain == null) {
            return "ERROR: Domain " + name + "doesn't exists\n";
        } else {
            // Update the domain expiry date and save to the domains map
            domains.put(name, new Domain(name, domain.getUserId(), expiryDate, domain.getPurchaseDate(), false));
            saveDomains(); // Save the updated domains map to the JSON file
            return "OK\n";
        }
    }

    /**
     * Loads domains from the JSON file into the domains map.
     */
    private void loadDomains() {
        // Check if the domain file exists
        if(domainFile.exists()) {
            try {
                // Read domains from the JSON file and put them into the domains map
                Map<String, Domain> loadedDomains = objectMapper.readValue(domainFile, new TypeReference<Map<String, Domain>>() {});
                domains.putAll(loadedDomains);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Saves the current domains map to the JSON file.
     */
    public void saveDomains() {
        try {
            // Write the domains map to the JSON file
            objectMapper.writeValue(domainFile, domains);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
