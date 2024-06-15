package it.unimib.sd2024;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

     //TODO: do this

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
            String accountHolder = getFieldValue(jsonNode, "accountHolder");
            String cardNumber = getFieldValue(jsonNode, "cardNumber");
            String cvv = getFieldValue(jsonNode, "cvv");
            String operation = getFieldValue(jsonNode, "operation");
            
            String operationDate = Long.toString(System.currentTimeMillis());

            long millsInAYear = TimeUnit.DAYS.toMillis(365);
            String expirationDate = Long.toString(System.currentTimeMillis() + (duration * millsInAYear));

            switch (operation) {
                case "purchase":
                    // add order record
                    Client client = new Client("localhost", 3030);
                    String orderCommand = String.format("ORDER SET %s %s %s %s %s %s %s", domain, userId, price, operationDate, cvv, cardNumber, operation);
                    String orderResponse = client.sendCommand(orderCommand);            

                    // add domain record
                    String domainCommand = String.format("DOMAIN SET %s %s %s %s", domain, userId, operationDate, operationDate, expirationDate);
                    String domainResponse = client.sendCommand(domainCommand);

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
                    // serve credo un metodo "update" per il dominio
                    break;
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
