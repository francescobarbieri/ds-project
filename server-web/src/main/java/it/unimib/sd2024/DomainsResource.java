package it.unimib.sd2024;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unimib.sd2024.utils.Client;
import it.unimib.sd2024.utils.ResponseBuilderUtil;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Represents the "domain" resource in "http://localhost:8080/domains".
 */
@Path("domains")
public class DomainsResource {
    ObjectMapper mapper = new ObjectMapper();

    @OPTIONS
    public Response avoidCORSBlocking2() {
        return ResponseBuilderUtil.buildOkResponse();
    }

    /**
     * Implementazione di GET "/domains".
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomains(@QueryParam("userId") String userId) {
        try {
            String response;
            if(userId == null || userId == "") {
                Client client = new Client("localhost", 3030);
                String command = "DOMAIN GETALL";
                response = client.sendCommand(command);
                client.close();
            } else {
                Client client = new Client("localhost", 3030);
                String command = "DOMAIN GETALL " + userId;
                response = client.sendCommand(command);
                client.close();
            }

            if("".equals(response.trim())) { 
                return ResponseBuilderUtil.build(Response.Status.NOT_FOUND);
            } else {
                return ResponseBuilderUtil.buildOkResponse(response,  MediaType.APPLICATION_JSON);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseBuilderUtil.build(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Implementazione di GET "/domains/{domain}/availability".
     */
    @Path("/{domain}/availability")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkAvailabilityDomain(@PathParam("domain") String domain) {

        if( ! domainValidator(domain)) {
            return ResponseBuilderUtil.build(Response.Status.BAD_REQUEST, "ERROR: Invalid domain.");
        }

        try {
            Client client = new Client("localhost", 3030);
            String command = "DOMAIN CHECK " + domain;
            String response = client.sendCommand(command);

            if("OK".equals(response.trim())) {
                StringBuilder jsonString = new StringBuilder();
                jsonString.append("{\"domain\": \"" + domain + "\",");
                jsonString.append("\"availability\": true }");
        
                String jsonResponse = jsonString.toString(); 

                client.close();

                return ResponseBuilderUtil.buildOkResponse(jsonResponse, MediaType.APPLICATION_JSON);
            } else {
                // Domain not available
                // Get domain infos
                command = "DOMAIN GET " + domain;
                response = client.sendCommand(command);

                // Extract propietary userId and expiration date from json
                JsonNode jsonNodeDomain = mapper.readTree(response);
                String userId = getFieldValue(jsonNodeDomain, "userId");
                String expiryDate = getFieldValue(jsonNodeDomain, "expiryDate");

                // Get userId infos
                command = "USER GET " + userId;
                response = client.sendCommand(command);

                // Extract user infos from json
                JsonNode jsonNodeUser = mapper.readTree(response);
                String name = getFieldValue(jsonNodeUser, "name");
                String surname = getFieldValue(jsonNodeUser, "surname");
                String email = getFieldValue(jsonNodeUser, "email");

                client.close();

                // Compose response JSON
                StringBuilder jsonString = new StringBuilder();
                jsonString.append("{\"name\": \"" + name + "\",");
                jsonString.append("\"surname\": \"" + surname + "\",");
                jsonString.append("\"email\": \"" + email + "\",");
                jsonString.append("\"expiryDate\": \"" + expiryDate + "\" }");
        
                String jsonResponse = jsonString.toString(); 

                return ResponseBuilderUtil.build(Response.Status.CONFLICT, jsonResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseBuilderUtil.build(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String getFieldValue(JsonNode jsonNode, String fieldName) {
        JsonNode fieldNode = jsonNode.get(fieldName);
        if(fieldName == null) {
            System.out.println("Field " + fieldName + " is missing or null.");
        }
        return fieldNode != null ? fieldNode.asText() : null;
    }

    private boolean domainValidator(String domain) {
        String DOMAIN_NAME_WITH_TLD_REGEX = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";
        Pattern DOMAIN_NAME_WITH_TLD_PATTERN = Pattern.compile(DOMAIN_NAME_WITH_TLD_REGEX);

        if(domain == null) return false;

        Matcher matcher = DOMAIN_NAME_WITH_TLD_PATTERN.matcher(domain);
        return matcher.matches();
    }
}