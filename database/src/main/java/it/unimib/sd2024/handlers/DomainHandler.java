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

public class DomainHandler {
    private final File domainFile = new File("resources/domains.json");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Domain> domains = new ConcurrentHashMap<>();

    public DomainHandler() {
        loadDomains();
    }

    public String handle(String[] parts) {
        if(parts.length < 2) return "ERROR: Invalid command. \n";
        String command = parts[1].toUpperCase();

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

    private String set(String[] parts) {
        if(parts.length != 6)  return "ERROR: SET command must be in the format: DOMAIN SET <name> <userid> <now> <expirydate>\n";
        String name = parts[2];
        String userid = parts[3];
        long now = Long.parseLong(parts[4]);
        long expiryDate = Long.parseLong(parts[5]);

        if(domains.containsKey(name)) {
            System.out.println("ERROR: Domain " + name + " already exists");
            return "ERROR: Domain " + name + " already exists\n";
        }

        domains.put(name, new Domain(name, userid, expiryDate, now, false));
        saveDomains();
        return "OK\n";
    }

    private String check(String[] parts) {
        if(parts.length != 3) return "ERROR: CHECK command must be in the format: DOMAIN CHECK <name>\n";
        String name = parts[2];
        Domain domain = domains.get(name);

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

    private String get(String[] parts) {
        if(parts.length != 3) return "ERROR: GET command must be in the format: DOMAIN GET <domainName> \n";
        String name = parts[2];
        Domain domain = domains.get(name);

        if(domain == null) {
            return "ERROR: Domain not found\n";
        } else {
            try {
                return objectMapper.writeValueAsString(domain) + "\n";
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return "ERROR: Unable to process the request\n";
            }
        }
    }

    private String getall(String[] parts) {
        try {
            // Returns all domains
            if(parts.length == 2) {
                List<Domain> domainList = new ArrayList<>(domains.values());
                return objectMapper.writeValueAsString(domainList) + "\n";
            }
            // Returns only userId's domains
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

    private String update(String[] parts) {
        if(parts.length != 4) return "ERROR: UPDATE command must be in the format: DOMAIN UPDATE <domainname> <expirydate> \n";
        String name = parts[2];
        long expiryDate = Long.parseLong(parts[3]);

        Domain domain = domains.get(name);

        if(domain == null) {
            return "ERROR: Domain " + name + "doesn't exists\n";
        } else {
            domains.put(name, new Domain(name, domain.getUserId(), expiryDate, domain.getPurchaseDate(), false));
            saveDomains();
            return "OK\n";
        }
    }

    private void loadDomains() {
        if(domainFile.exists()) {
            try {
                Map<String, Domain> loadedDomains = objectMapper.readValue(domainFile, new TypeReference<Map<String, Domain>>() {});
                domains.putAll(loadedDomains);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveDomains() {
        try {
            objectMapper.writeValue(domainFile, domains);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
