package it.unimib.sd2024;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.SysexMessage;

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
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("orders")
public class OrderResource {

    ObjectMapper mapper = new ObjectMapper();

    static { }

    @OPTIONS
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response avoidCORSBlocking() {
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
     *  Implementazione di GET "/orders".
     */

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrder(@QueryParam("userId") String userId) {
        try {
            String response;
            if(userId == null || userId == "") {
                Client client = new Client("localhost", 3030);
                String command = "ORDER GETALL";
                response = client.sendCommand(command);
                client.close();
            } else {
                Client client = new Client("localhost", 3030);
                String command = "ORDER GETALL " + userId;
                response = client.sendCommand(command);
                client.close();
            }

            if(response == "") { // TODO: handle error
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
    * Implementation of POST "/orders".
    */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setOrder(String body) {
        try {
            JsonNode jsonNode = mapper.readTree(body);

            //TODO: GENERAL FOR ALL VALUES IN ALL CLASS (orders, domains, users) ADD FORMAT AND TYPE CHECK
            //TODO: gestire concorrenza se due utenti stanno facendo la stessa operazione insieme

            String userId = getFieldValue(jsonNode, "userId");
            int duration = jsonNode.get("duration").asInt();
            String price = getFieldValue(jsonNode, "price");
            String domain = getFieldValue(jsonNode, "domain");
            //TODO: add accountholder to socket data
            String accountHolder = getFieldValue(jsonNode, "accountHolder");
            String cardNumber = getFieldValue(jsonNode, "cardNumber");
            String cvv = getFieldValue(jsonNode, "cvv");
            String operation = getFieldValue(jsonNode, "operation");
            
            String operationDate = Long.toString(System.currentTimeMillis());

            long millsInAYear = TimeUnit.DAYS.toMillis(365);
            String expirationDate = Long.toString(System.currentTimeMillis() + (duration * millsInAYear));

            Client client = new Client("localhost", 3030);
            String orderCommand;
            String orderResponse;
            String domainCommand;
            String domainResponse; 

            switch (operation) {
                case "purchase":
                    // add order record
                    orderCommand = String.format("ORDER SET %s %s %s %s %s %s %s %s", domain, userId, price, operationDate, cvv, cardNumber, operation, accountHolder);
                    orderResponse = client.sendCommand(orderCommand);            

                    // add domain record
                    domainCommand = String.format("DOMAIN SET %s %s %s %s", domain, userId, operationDate, operationDate, expirationDate);
                    domainResponse = client.sendCommand(domainCommand);

                    client.close();

                    //TODO: handle error "ERROR: Domain <domain> already exists."

                    if("OK".equals(orderResponse.trim()) && "OK".equals(domainResponse.trim())) {
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

                    break;
                case "renewal":
                    //TODO: renewal (add years logic)

                    //TODO: controllare che il dominio appartenga all'utente
                    // prendre la data di scadenza attuale
                    domainCommand = "DOMAIN GET " + domain;
                    domainResponse = client.sendCommand(domainCommand);

                    JsonNode jsonNodeDomain = mapper.readTree(domainResponse);
                    String userIdDomain = getFieldValue(jsonNodeDomain, "userId");
                    long exiprationDomain = jsonNodeDomain.get("expiryDate").asLong();
                    
                    System.out.println(userId);
                    System.out.println(userIdDomain);

                    // Check if domain is actually a userId's domain
                    if(userId != userId) {
                        System.out.println("Qui non ci dovrei entrare");
                        client.close();
                        return Response.status(Response.Status.FORBIDDEN).entity("You don't have access to this operation")
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "*")
                            .header("Access-Control-Allow-Headers", "*")
                            .header("Access-Control-Allow-Credentials", "false")
                            .header("Access-Control-Max-Age", "3600")
                            .header("Access-Control-Request-Method", "*")
                            .header("Access-Control-Request-Headers", "origin, x-request-with")
                        .build();
                    } else {
                        // update expiration date
                        String newExpiration = Long.toString(exiprationDomain + (duration * millsInAYear));
                        
                        // update domain
                        domainCommand = "DOMAIN UPDATE " + domain + " " + newExpiration;
                        domainResponse = client.sendCommand(domainCommand);

                        // add order record
                        orderCommand = String.format("ORDER SET %s %s %s %s %s %s %s %s", domain, userId, price, operationDate, cvv, cardNumber, operation, accountHolder);
                        orderResponse = client.sendCommand(orderCommand);

                        client.close();

                        if("OK".equals(domainResponse.trim()) && "OK".equals(orderResponse.trim())) {
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
                    }
                default:
                    return Response.status(Response.Status.BAD_REQUEST)
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "*")
                        .header("Access-Control-Allow-Headers", "*")
                        .header("Access-Control-Allow-Credentials", "false")
                        .header("Access-Control-Max-Age", "3600")
                        .header("Access-Control-Request-Method", "*")
                        .header("Access-Control-Request-Headers", "origin, x-request-with")
                    .build();
            }

            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
