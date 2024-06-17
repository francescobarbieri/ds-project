package it.unimib.sd2024;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unimib.sd2024.utils.Client;
import it.unimib.sd2024.utils.ResponseBuilderUtil;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Represents the "orders" resource in "http://localhost:8080/orders".
 */
@Path("orders")
public class OrderResource {
    ObjectMapper mapper = new ObjectMapper();

    @OPTIONS
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response avoidCORSBlocking() {
        return ResponseBuilderUtil.buildOkResponse();
    }

    /**
     * Implementation of GET "/orders".
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

            if("".equals(response.trim())) {
                return ResponseBuilderUtil.build(Response.Status.NOT_FOUND); //TODO: add client this error handling (anche per ORDERS)
            } else {
                return ResponseBuilderUtil.buildOkResponse(response, MediaType.APPLICATION_JSON);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseBuilderUtil.build(Response.Status.INTERNAL_SERVER_ERROR);
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

            //TODO: gestire concorrenza se due utenti stanno facendo la stessa operazione insieme

            String userId = getFieldValue(jsonNode, "userId");
            int duration = jsonNode.get("duration").asInt();
            String price = getFieldValue(jsonNode, "price");
            String domain = getFieldValue(jsonNode, "domain");
            String accountHolder = getFieldValue(jsonNode, "accountHolder");
            String cardNumber = getFieldValue(jsonNode, "cardNumber");
            String cvv = getFieldValue(jsonNode, "cvv");
            String operation = getFieldValue(jsonNode, "operation");

            try {
                if (! domainValidator(domain)) throw new Exception("ERROR: Invalid domain.");
                else if (userId == "" || userId == null) throw new Exception("ERROR: Missing userId in request body.");
                else if (cvv.length() != 3) throw new Exception("ERROR: Invalid cvv");
                else if (! cardNumberValidator(cardNumber)) throw new Exception("ERROR: Invalid card number. Use this for testing: 4532015112830366");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseBuilderUtil.build(Response.Status.BAD_REQUEST, e.getMessage());
            }
            
            String operationDate = Long.toString(System.currentTimeMillis());

            long millsInAYear = TimeUnit.DAYS.toMillis(365);
            String expirationDate = Long.toString(System.currentTimeMillis() + (duration * millsInAYear));

            Client client = new Client("localhost", 3030);
            String orderCommand, orderResponse, domainCommand, domainResponse; 

            switch (operation) {
                // Purchase a new domain
                case "purchase":
                    // Add order
                    orderCommand = String.format("ORDER SET %s %s %s %s %s %s %s %s", domain, userId, price, operationDate, cvv, cardNumber, operation, accountHolder);
                    orderResponse = client.sendCommand(orderCommand);            

                    // Add domain
                    domainCommand = String.format("DOMAIN SET %s %s %s %s", domain, userId, operationDate, expirationDate);
                    domainResponse = client.sendCommand(domainCommand);

                    client.close();

                    if("OK".equals(orderResponse.trim()) && "OK".equals(domainResponse.trim())) {
                        return ResponseBuilderUtil.buildOkResponse();
                    } else if (domainResponse.trim().startsWith("ERROR:")) {
                        return ResponseBuilderUtil.build(Response.Status.CONFLICT, domainResponse);
                    }

                    break;
                // Renew an existing domain
                case "renewal":

                    // Get domain expiration date
                    domainCommand = "DOMAIN GET " + domain;
                    domainResponse = client.sendCommand(domainCommand);

                    JsonNode jsonNodeDomain = mapper.readTree(domainResponse);
                    String userIdDomain = getFieldValue(jsonNodeDomain, "userId");
                    long exiprationDomain = jsonNodeDomain.get("expiryDate").asLong();
                    long purchaseDate = jsonNodeDomain.get("purchaseDate").asLong();

                    // Max cumulative years renewal check (max 10 years)
                    if(! isWithin10Years(purchaseDate, Long.parseLong(expirationDate))) {
                        System.out.println("Renew invalido");
                        return ResponseBuilderUtil.build(Response.Status.NOT_ACCEPTABLE, "ERROR: You can't renew for more than 10 comulative years.");
                    }

                    // Check if domain belongs to userId
                    if(! userId.trim().equals(userIdDomain.trim())) {
                        client.close();
                        return ResponseBuilderUtil.build(Response.Status.FORBIDDEN, "You don't have access to this operation");
                    } else {
                        // New expiration date string
                        String newExpiration = Long.toString(exiprationDomain + (duration * millsInAYear));
                        
                        // Update domain expiration date
                        domainCommand = "DOMAIN UPDATE " + domain + " " + newExpiration;
                        domainResponse = client.sendCommand(domainCommand);

                        // Add order record
                        orderCommand = String.format("ORDER SET %s %s %s %s %s %s %s %s", domain, userId, price, operationDate, cvv, cardNumber, operation, accountHolder);
                        orderResponse = client.sendCommand(orderCommand);

                        client.close();

                        // If both are okay returns okay response
                        if("OK".equals(domainResponse.trim()) && "OK".equals(orderResponse.trim())) {
                            return ResponseBuilderUtil.buildOkResponse();
                        }
                    }
                default:
                    // If operation different from purchase or renewal
                    return ResponseBuilderUtil.build(Response.Status.BAD_REQUEST, "ERROR: Unkwon operation type.");
            }

            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseBuilderUtil.build(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper to get field value from JSON
    private String getFieldValue(JsonNode jsonNode, String fieldName) {
        JsonNode fieldNode = jsonNode.get(fieldName);
        if(fieldName == null) {
            System.out.println("Field " + fieldName + " is missing or null.");
        }
        return fieldNode != null ? fieldNode.asText() : null;
    }

    // Domain validation using regex
    private boolean domainValidator(String domain) {
        String DOMAIN_NAME_WITH_TLD_REGEX = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";
        Pattern DOMAIN_NAME_WITH_TLD_PATTERN = Pattern.compile(DOMAIN_NAME_WITH_TLD_REGEX);

        if(domain == null) return false;

        Matcher matcher = DOMAIN_NAME_WITH_TLD_PATTERN.matcher(domain);
        return matcher.matches();
    }

    // Card number validation
    private boolean cardNumberValidator(String card) {
        if(card == null || card.isEmpty()) return false;

        int sum = 0;
        boolean alternate = false;

        for(int i = card.length() - 1; i >= 0; i-- ) {
            char c = card.charAt(i);
            if(!Character.isDigit(c)) return false; // Invalid character  found
            int n = Character.getNumericValue(c);
            if(alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }

            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    // Check if date is no more than 10 years forward the purchase date
    private boolean isWithin10Years (long millis1, long millis2) {
        Date date1 = new Date(millis1);
        Date date2 = new Date(millis2);
        
        // Calculate the difference in milliseconds
        long diffInMillis = Math.abs(date1.getTime() - date2.getTime());

        // Convert difference from milliseconds to years
        long diffInYears = diffInMillis / (1000L * 60 * 60 * 24 * 365);
        
        return diffInYears <= 10;
    }
}
