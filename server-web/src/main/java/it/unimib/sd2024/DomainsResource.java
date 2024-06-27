package it.unimib.sd2024;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.attribute.standard.Media;

import org.json.JSONObject;

import it.unimib.sd2024.model.Domain;
import it.unimib.sd2024.model.User;
import it.unimib.sd2024.utils.DBRequest;
import it.unimib.sd2024.utils.DBResponse;
import it.unimib.sd2024.utils.ResponseBuilderUtil;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Represents the "domains" resource in "http://localhost:8080/domains".
 */
@Path("domains")
public class DomainsResource {

    @OPTIONS
    public Response avoidCORSBlocking2() {
        return ResponseBuilderUtil.buildOkResponse();
    }

    /**
     * Implementation of GET "/domains".
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomains(@QueryParam("userId") String userId) {
        try {
            DBRequest domainRequest = new DBRequest("domains");
            DBResponse response;

            if(userId == null || userId == "") {
                response = domainRequest.getDocs();
            } else {
                response = domainRequest.getFilteredDoc("userId", userId);

                if(response.isOk()) {
                    JSONObject jsonResponse = new JSONObject(response.getResponse());
                    long currentTimeMillis = System.currentTimeMillis();

                    JSONObject filteredResponse = new JSONObject();

                    Iterator<String> keys = jsonResponse.keys();
                    while(keys.hasNext()) {
                        String key = keys.next();
                        JSONObject domain = jsonResponse.getJSONObject(key);

                        long expirationDate;
                        if(domain.has("expirationDate")) {
                            expirationDate = domain.getLong("expirationDate");
                        } else continue;

                        System.out.println(new Date(expirationDate));

                        if(expirationDate > currentTimeMillis) {
                            filteredResponse.put(key, domain);
                        }
                    }

                    return ResponseBuilderUtil.buildOkResponse(filteredResponse.toString(), MediaType.APPLICATION_JSON);
                } else {
                    response.returnErrors();
                }
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
     * Implementation of GET "/domains/{domain}/availability".
     */
    @Path("/{domain}/availability")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkAvailabilityDomain(@PathParam("domain") String domain) {

        if(!domainValidator(domain)) {
            return ResponseBuilderUtil.build(Response.Status.BAD_REQUEST, "ERROR: Invalid domain.");
        }

        try {
            DBRequest domainRequest = new DBRequest("domains");
            DBResponse domainResponse = domainRequest.getDoc(domain);

            System.out.println("Error message: " + domainResponse.getErrorMessage());
            System.out.println("Response: " + domainResponse.getResponse());

            if(domainResponse.getErrorMessage().equals("NOTFOUND")) {
                return ResponseBuilderUtil.buildOkResponse();
            } else if(domainResponse.isOk()) {

                // Parse response domain object
                Jsonb jsonb = JsonbBuilder.create();
                Domain domainJson = jsonb.fromJson(domainResponse.getResponse(), Domain.class);

                // Extrat domain infos
                String userId = domainJson.getUserId();
                Long expirationDate = domainJson.getExpirationDate();

                // Check if domain has expired, if it has it is available
                if(expirationDate < System.currentTimeMillis())
                    return ResponseBuilderUtil.buildOkResponse();

                // Else, not available, get user data and return 409 error
                DBRequest userRequest = new DBRequest("users");
                DBResponse userResponse = userRequest.getDoc(userId);

                if(!userResponse.isOk())
                    return ResponseBuilderUtil.build(Response.Status.INTERNAL_SERVER_ERROR);

                User userJson = jsonb.fromJson(userResponse.getResponse(), User.class);
                String name = userJson.getName();
                String email = userJson.getEmail();
                String surname = userJson.getSurname();

                // Build json response
                JsonObject jsonObject = Json.createObjectBuilder()
                    .add("name", name)
                    .add("surname", surname)
                    .add("email", email)
                    .add("expirationDate", expirationDate)
                    .build();
                String jsonResponse = jsonb.toJson(jsonObject);

                return ResponseBuilderUtil.build(Response.Status.CONFLICT, jsonResponse);
            } else {
                return domainResponse.returnErrors();
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
}