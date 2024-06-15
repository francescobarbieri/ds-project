package it.unimib.sd2024;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.print.attribute.standard.Media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unimib.sd2024.model.Domain;

import it.unimib.sd2024.model.User;
import it.unimib.sd2024.utils.Client;
import jakarta.json.JsonException;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Represents the "domain" resource in "http://localhost:8080/domains".
 */
@Path("domains")
public class DomainsResource {
    ObjectMapper mapper = new ObjectMapper();

    static { }

    @OPTIONS
    public Response avoidCORSBlocking2() {
        return Response.ok()
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "*")
            .header("Access-Control-Allow-Headers", "*")
            .header("Access-Control-Allow-Credentials", "false")
            .header("Access-Control-Max-Age", "3600")
            .header("Access-Control-Request-Method", "*")
            .header("Access-Control-Request-Headers", "origin, x-request-with")
            .build();
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


            if(response == "") { // TODO: error
                return Response.status(Response.Status.NOT_FOUND)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "*")
                    .header("Access-Control-Allow-Headers", "*")
                    .header("Access-Control-Allow-Credentials", "false")
                    .header("Access-Control-Max-Age", "3600")
                    .header("Access-Control-Request-Method", "*")
                    .header("Access-Control-Request-Headers", "origin, x-request-with")
                .build();
            } else {
                return Response.ok(response, MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "*")
                    .header("Access-Control-Allow-Headers", "*")
                    .header("Access-Control-Allow-Credentials", "false")
                    .header("Access-Control-Max-Age", "3600")
                    .header("Access-Control-Request-Method", "*")
                    .header("Access-Control-Request-Headers", "origin, x-request-with")
                .build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "*")
                .header("Access-Control-Allow-Headers", "*")
                .header("Access-Control-Allow-Credentials", "false")
                .header("Access-Control-Max-Age", "3600")
                .header("Access-Control-Request-Method", "*")
                .header("Access-Control-Request-Headers", "origin, x-request-with")
            .build();
        }
    }

    /**
     * Implementazione di GET "/domains/{domain}/availability".
     */
    @Path("/{domain}/availability")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkAvailabilityDomain(@PathParam("domain") String domain) {

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

                return Response.ok(jsonResponse, MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "*")
                    .header("Access-Control-Allow-Headers", "*")
                    .header("Access-Control-Allow-Credentials", "false")
                    .header("Access-Control-Max-Age", "3600")
                    .header("Access-Control-Request-Method", "*")
                    .header("Access-Control-Request-Headers", "origin, x-request-with")
                .build();
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

                return Response.status(Response.Status.CONFLICT).entity(jsonResponse)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "*")
                    .header("Access-Control-Allow-Credentials", "false")
                    .header("Access-Control-Max-Age", "3600")
                    .header("Access-Control-Request-Method", "*")
                    .header("Access-Control-Request-Headers", "origin, x-request-with")
                    .build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage())
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "*")
                .header("Access-Control-Allow-Credentials", "false")
                .header("Access-Control-Max-Age", "3600")
                .header("Access-Control-Request-Method", "*")
                .header("Access-Control-Request-Headers", "origin, x-request-with")
                .build();
        }
    }

    private String getFieldValue(JsonNode jsonNode, String fieldName) {
        JsonNode fieldNode = jsonNode.get(fieldName);
        if(fieldName == null) {
            System.out.println("Field " + fieldName + " is missing or null.");
        }
        return fieldNode != null ? fieldNode.asText() : null;
    }
}