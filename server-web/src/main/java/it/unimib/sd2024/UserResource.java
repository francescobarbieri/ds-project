package it.unimib.sd2024;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimib.sd2024.utils.DBRequest;
import it.unimib.sd2024.utils.DBResponse;
import it.unimib.sd2024.utils.ResponseBuilderUtil;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Represents the "user" resource in "http://localhost:8080/user".
 */
@Path("user")
public class UserResource {

    @OPTIONS
    @Produces(MediaType.APPLICATION_JSON)
    public Response avoidCORSBlocking() {
        return ResponseBuilderUtil.buildOkResponse();
    }

    /**
     * Implementation of GET "/user/{userId}".
     */
    @Path("/{userId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("userId") String userId) {

        if(userId == "" || userId == null) {
            return ResponseBuilderUtil.build(Response.Status.BAD_REQUEST, "ERROR: Missing userId");
        }

        try {
            DBRequest userRequest = new DBRequest("users");
            DBResponse response = userRequest.getDoc(userId);

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
     * Implementation of POST "/user".
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(String jsonString) {
        try {
            JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
            JsonObject jsonObject = jsonReader.readObject();

            String name = jsonObject.getString("name");
            String surname = jsonObject.getString("surname");
            String email = jsonObject.getString("email");

            if(!emailValidator(email) || name.isEmpty() || surname.isEmpty() ) {
                return ResponseBuilderUtil.build(Response.Status.BAD_REQUEST, "ERROR: Invalid email.");
            }

            String userId = generateUniqueId(email);

            // Add userId to json that get sent to the server
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder(jsonObject);
            jsonObjectBuilder.add("userId", userId);
            JsonObject jsonObjectWithUserId = jsonObjectBuilder.build();

            DBRequest userRequest = new DBRequest("users");
            DBResponse response = userRequest.setDoc(userId, jsonObjectWithUserId.toString());

            System.out.println(response.getResponse());

            if(response.isOk()) {
                StringBuilder jsonResponse = new StringBuilder();
                jsonResponse.append("{\"id\": \"" + userId + "\"}");

                return ResponseBuilderUtil.buildOkResponse(jsonResponse.toString(), MediaType.APPLICATION_JSON);
            } else {
                return ResponseBuilderUtil.build(Response.Status.CONFLICT, response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseBuilderUtil.build(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // Email validation using regex
    private boolean emailValidator(String email) {
        String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

        if(email == null) return false;

        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    /**
     * Generate a unique ID for a user based on its email.
     */
    private String generateUniqueId(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            BigInteger hasInteger = new BigInteger(1, hashBytes);
            String haString = hasInteger.toString();
            int uniqueId = Math.abs(haString.hashCode()) % 100000;

            return String.format("%05d", uniqueId);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating a unique id, " + e);
        }
    }
}