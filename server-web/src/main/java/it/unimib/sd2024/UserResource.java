package it.unimib.sd2024;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unimib.sd2024.model.User;
import it.unimib.sd2024.utils.Client;
import it.unimib.sd2024.utils.ResponseBuilderUtil;
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
    private ObjectMapper objectMapper = new ObjectMapper();

    @OPTIONS
    @Produces(MediaType.APPLICATION_JSON)
    public Response avoidCORSBlocking() {
        return ResponseBuilderUtil.buildOkResponse();
    }

    /**
     * Implementation of GET "/user".
     */
    @Path("/{userId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("userId") String userId) {
        try {
            Client client = new Client("localhost", 3030);
            String command = "USER GET " + userId;
            String response = client.sendCommand(command);
            client.close();

            if("NULL".equals(response.trim())) {
                // User not found
                return ResponseBuilderUtil.build(Response.Status.NOT_FOUND);
            } else {
                User user = objectMapper.readValue(response, User.class);
                return ResponseBuilderUtil.buildOkResponse(user, MediaType.APPLICATION_JSON);
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
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            String name = getFieldValue(jsonNode, "name");
            String surname = getFieldValue(jsonNode, "surname");
            String email = getFieldValue(jsonNode, "email");

            if(!emailValidator(email)) {
                return ResponseBuilderUtil.build(Response.Status.BAD_REQUEST, "ERROR: Invalid email.");
            }

            Client client = new Client("localhost", 3030);
            String command = String.format("USER SET %s %s %s", email, name, surname);
            String response = client.sendCommand(command);
            client.close();

            if(response.startsWith("OK, user ID: ")) {
                String userId = response.substring(13).trim();

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

    private String getFieldValue(JsonNode jsonNode, String fieldName) {
        JsonNode fieldNode = jsonNode.get(fieldName);
        if(fieldName == null) {
            System.out.println("Field " + fieldName + " is missing or null.");
        }
        return fieldNode != null ? fieldNode.asText() : null;
    }

    private boolean emailValidator(String email) {
        String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

        if(email == null) return false;

        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }
    
}