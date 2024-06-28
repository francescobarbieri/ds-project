package it.unimib.sd2024;

import java.io.IOException;
import java.io.StringReader;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Optional;
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

    private static final String DOMAIN_NAME_WITH_TLD_REGEX = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";
    private static final Pattern DOMAIN_NAME_WITH_TLD_PATTERN = Pattern.compile(DOMAIN_NAME_WITH_TLD_REGEX);
    private static final int CVV_LENGTH = 3;
    private static final int MAX_YEARS = 10;
    private static final long MILLS_IN_A_YEAR = TimeUnit.DAYS.toMillis(365);
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ID_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();
    
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

            Optional<String> validationError = validateOrderRequest(jsonObject);
            if (validationError.isPresent()) {
                return ResponseBuilderUtil.build(Response.Status.BAD_REQUEST, validationError.get());
            }

            String operation = jsonObject.getString("operation");

            switch (operation) {
                // Purchase a new domain
                case "purchase":
                    return handlePurchase(jsonObject);
                // Renew an existing domain
                case "renewal":
                   return handleRenewal(jsonObject);
                default:
                    // If operation different from purchase or renewal
                    return ResponseBuilderUtil.build(Response.Status.BAD_REQUEST, "ERROR: Unkwon operation type.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseBuilderUtil.build(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    private Response handlePurchase(JsonObject jsonObject) throws IOException {
        String domain = jsonObject.getString("domain");
        String userId = jsonObject.getString("userId");
        long operationDate = System.currentTimeMillis();
        int duration = Integer.parseInt(jsonObject.getString("duration"));
        String expirationDate = Long.toString(operationDate + (duration * MILLS_IN_A_YEAR));

        Domain domainToUpload = new Domain(domain, userId, Long.parseLong(expirationDate), operationDate, operationDate);
        DBRequest domainRequest = new DBRequest("domains");
        DBResponse domainResponse = domainRequest.setDoc(domain, domainToUpload.toJson());

        if (domainResponse.getErrorMessage().equals("ERROR: Key already exists.")) {
            domainResponse = handleExistingDomain(domain, domainToUpload, operationDate);
        }

        return createOrderRecord(jsonObject, domain, userId, operationDate, "purchase", domainResponse);
    }

    private DBResponse handleExistingDomain(String domain, Domain domainToUpload, long operationDate) throws IOException {
        DBRequest domainRequest = new DBRequest("domains");
        DBResponse domainResponse = domainRequest.getDoc(domain);

        if (domainResponse.isOk()) {
            Jsonb jsonb = JsonbBuilder.create();
            Domain existingDomain = jsonb.fromJson(domainResponse.getResponse(), Domain.class);

            if (existingDomain.getExpirationDate() < operationDate) {
                domainRequest = new DBRequest("domains");
                domainResponse = domainRequest.update(domain, domainToUpload.toJson());
            }
        }

        return domainResponse;
    }

    private Response handleRenewal(JsonObject jsonObject) throws IOException {
        String domain = jsonObject.getString("domain");
        String userId = jsonObject.getString("userId");
        long operationDate = System.currentTimeMillis();
        int duration = Integer.parseInt(jsonObject.getString("duration"));

        DBRequest domainRequest = new DBRequest("domains");
        DBResponse domainResponse = domainRequest.getDoc(domain);

        if (!domainResponse.isOk()) {
            return ResponseBuilderUtil.build(Response.Status.NOT_FOUND);
        }

        Jsonb jsonb = JsonbBuilder.create();
        Domain domainObject = jsonb.fromJson(domainResponse.getResponse(), Domain.class);

        if (!userId.equals(domainObject.getUserId())) {
            return ResponseBuilderUtil.build(Response.Status.FORBIDDEN, "You don't have access to this operation");
        }

        long newExpiration = domainObject.getExpirationDate() + (duration * MILLS_IN_A_YEAR);

        if (!isWithin10Years(domainObject.getLastRenewed(), newExpiration) || duration > MAX_YEARS) {
            return ResponseBuilderUtil.build(Response.Status.NOT_ACCEPTABLE, "ERROR: You can't renew for more than 10 years.");
        }

        domainObject.setLastRenewed(operationDate);
        domainObject.setExpirationDate(newExpiration);

        domainRequest = new DBRequest("domains");
        domainResponse = domainRequest.update(domain, domainObject.toJson());

        return createOrderRecord(jsonObject, domain, userId, operationDate, "renewal", domainResponse);
    }

    private Response createOrderRecord(JsonObject jsonObject, String domain, String userId, long operationDate, String operation, DBResponse domainResponse) throws IOException {
        Order tempOrder = new Order(
            domain, 
            userId, 
            Integer.parseInt(jsonObject.getString("price")), 
            operationDate, 
            jsonObject.getString("accountHolder"), 
            jsonObject.getString("cvv"), 
            jsonObject.getString("cardNumber"), 
            operation
        );
        
        String id = randomId();
        DBRequest orderRequest = new DBRequest("orders");
        DBResponse orderResponse = orderRequest.setDoc(id, tempOrder.toJson());

        if (orderResponse.isOk() && domainResponse.isOk()) {
            return ResponseBuilderUtil.buildOkResponse();
        } else if (orderResponse.getErrorMessage().startsWith("ERROR")) {
            return ResponseBuilderUtil.build(Response.Status.CONFLICT, domainResponse);
        }

        return ResponseBuilderUtil.build(Response.Status.INTERNAL_SERVER_ERROR);
    }


    // Validate all order data coming from client
    private Optional<String> validateOrderRequest(JsonObject jsonObject) {
        String domain = jsonObject.getString("domain");
        String userId = jsonObject.getString("userId");
        String cardNumber = jsonObject.getString("cardNumber");
        String cvv = jsonObject.getString("cvv");

        if (!domainValidator(domain)) return Optional.of("ERROR: Invalid domain.");
        if (userId == null || userId.isEmpty()) return Optional.of("ERROR: Missing userId in request body.");
        if (cvv.length() != CVV_LENGTH) return Optional.of("ERROR: Invalid cvv");
        if (!cardNumberValidator(cardNumber)) return Optional.of("ERROR: Invalid card number. Use this for testing: 4532015112830366");

        return Optional.empty();
    }

    // Domain validation using regex
    private boolean domainValidator(String domain) {
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
        // Secure random should have less collisions than basic random
        StringBuilder id = new StringBuilder(ID_LENGTH);

        for(int i = 0; i < ID_LENGTH; i++){
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            id.append(CHARACTERS.charAt(randomIndex));
        }

        return id.toString();
    }
}
