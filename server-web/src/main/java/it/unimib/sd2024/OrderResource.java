package it.unimib.sd2024;

import java.io.IOException;
import java.io.StringReader;
import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimib.sd2024.model.Domain;
import it.unimib.sd2024.model.Order;
import it.unimib.sd2024.utils.DBRequest;
import it.unimib.sd2024.utils.DBResponse;
import it.unimib.sd2024.utils.ResponseBuilderUtil;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
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
            DBRequest domainRequest = new DBRequest("orders");
            DBResponse response;

            if(userId == null || userId == "") {
                response = domainRequest.getDocs();
            } else {
                response = domainRequest.getFilteredDoc("userId", userId);
            }

            if(response.isOk()) {
                return ResponseBuilderUtil.buildOkResponse(response.getResponse(), MediaType.APPLICATION_JSON);
            } else {
                return response.returnErrors();
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
    public Response setOrder(String jsonString) {
        try {
            JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
            JsonObject jsonObject = jsonReader.readObject();

            String userId = jsonObject.getString("userId");
            int duration = Integer.parseInt(jsonObject.getString("duration"));
            //TODO add price to order class
            String price = jsonObject.getString("price");
            String domain = jsonObject.getString("domain");
            String accountHolder = jsonObject.getString("accountHolder");
            String cardNumber = jsonObject.getString("cardNumber");
            String cvv = jsonObject.getString("cvv");
            String operation = jsonObject.getString("operation");

            try {
                if (! domainValidator(domain)) throw new Exception("ERROR: Invalid domain.");
                else if (userId == "" || userId == null) throw new Exception("ERROR: Missing userId in request body.");
                else if (cvv.length() != 3) throw new Exception("ERROR: Invalid cvv");
                else if (duration > 10) throw new Exception("ERROR: You can't renew nor buy for more than 10 years.");
                else if (! cardNumberValidator(cardNumber)) throw new Exception("ERROR: Invalid card number. Use this for testing: 4532015112830366");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseBuilderUtil.build(Response.Status.BAD_REQUEST, e.getMessage());
            }
            
            Long operationDate = System.currentTimeMillis();

            long millsInAYear = TimeUnit.DAYS.toMillis(365);
            String expirationDate = Long.toString(System.currentTimeMillis() + (duration * millsInAYear));

            Jsonb jsonb = JsonbBuilder.create();
            String id;
            DBRequest orderRequest, domainRequest;
            DBResponse orderResponse, domainResponse;

            switch (operation) {
                // Purchase a new domain
                case "purchase":
                    // Try to add domain
                    Domain domainToUpload = new Domain(domain, userId, Long.parseLong(expirationDate), operationDate, operationDate);
                    domainRequest = new DBRequest("domains");
                    domainResponse = domainRequest.setDoc(domain, domainToUpload.toJson());

                    // If domain exists but is expired I have to update it with the new data
                    if(domainResponse.getErrorMessage().equals("ERROR: Key already exists.")) {

                        // Get the current domain
                        domainRequest = new DBRequest("domains");
                        domainResponse = domainRequest.getDoc(domain);
                        Domain existingDomain = jsonb.fromJson(domainResponse.getResponse(), Domain.class);
                        
                        // Domain has expired
                        if(existingDomain.getExpirationDate() < operationDate) {
                            domainRequest = new DBRequest("domains");
                            domainResponse = domainRequest.update(domain, domainToUpload.toJson());
                        }
                    } else return ResponseBuilderUtil.build(Response.Status.CONFLICT, "ERROR: Domain already exists.");

                    // Add order
                    Order tempOrder = new Order(domain, userId, Integer.parseInt(price), operationDate, accountHolder, cvv, cardNumber, operation);
                    id = randomId();
                    orderRequest = new DBRequest("orders");
                    orderResponse = orderRequest.setDoc(id, tempOrder.toJson());

                    if(orderResponse.isOk() && domainResponse.isOk()) {
                        return ResponseBuilderUtil.buildOkResponse();
                    } else if (orderResponse.getErrorMessage().startsWith("ERROR")) {
                        return ResponseBuilderUtil.build(Response.Status.CONFLICT, domainResponse);
                    }
                // Renew an existing domain
                case "renewal":
                    domainRequest = new DBRequest("domains");
                    domainResponse = domainRequest.getDoc(domain);

                    Domain domainObject;
                    if(domainResponse.isOk()) {
                        domainObject = jsonb.fromJson(domainResponse.getResponse(), Domain.class);
                    } else return ResponseBuilderUtil.build(Response.Status.NOT_FOUND);

                    String userIdDomain = domainObject.getUserId();
                    long exiprationDate = domainObject.getExpirationDate();
                    long lastRenewedDate = domainObject.getLastRenewed();

                    // New expiration date string
                    Long newExpiration = exiprationDate + (duration * millsInAYear);

                    // Max cumulative years renewal check (max 10 years)
                    if(! isWithin10Years(lastRenewedDate, newExpiration) || duration > 10) {
                        System.out.println("Renew invalido");
                        return ResponseBuilderUtil.build(Response.Status.NOT_ACCEPTABLE, "ERROR: You can't renew for more than 10 years.");
                    }

                    // Check if domain belongs to userId
                    if(! userId.trim().equals(userIdDomain.trim())) {
                        return ResponseBuilderUtil.build(Response.Status.FORBIDDEN, "You don't have access to this operation");
                    } else {
                        // Update domain expiration date
                        domainObject.setLastRenewed(operationDate);
                        domainObject.setExpirationDate(newExpiration);

                        // Update domain in database
                        domainRequest = new DBRequest("domains");
                        domainResponse = domainRequest.update(domain, domainObject.toJson());

                        // Add order record
                        Order orderObject = new Order(domain, userIdDomain, Integer.parseInt(price), operationDate, accountHolder, cvv, cardNumber, "renewal");
                        id = randomId();
                        orderRequest = new DBRequest("orders");        
                        orderResponse = orderRequest.setDoc(id, orderObject.toJson());

                        // If both are okay returns okay response
                        if(orderResponse.isOk() && domainResponse.isOk()) {
                            return ResponseBuilderUtil.buildOkResponse();
                        }
                    }
                default:
                    // If operation different from purchase or renewal
                    return ResponseBuilderUtil.build(Response.Status.BAD_REQUEST, "ERROR: Unkwon operation type.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseBuilderUtil.build(Response.Status.INTERNAL_SERVER_ERROR);
        }
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

    private String randomId() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int ID_LENGTH = 8;
        // Secure random should have less collisions than basic random
        SecureRandom RANDOM = new SecureRandom();

        StringBuilder id = new StringBuilder(ID_LENGTH);

        for(int i = 0; i < ID_LENGTH; i++){
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            id.append(CHARACTERS.charAt(randomIndex));
        }

        return id.toString();
    }
}
