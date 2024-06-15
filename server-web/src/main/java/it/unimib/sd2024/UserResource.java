package it.unimib.sd2024;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.print.attribute.standard.Media;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

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
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Represents the "user" resource in "http://localhost:8080/user".
 */
@Path("user")
public class UserResource {
    private ObjectMapper objectMapper = new ObjectMapper();
    static { }

    @OPTIONS
    @Produces(MediaType.APPLICATION_JSON)
    public Response avoidCORSBlocking() {
        return Response.ok()
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Headers", "*")
            .header("Access-Control-Allow-Credentials", "false")
            .header("Access-Control-Max-Age", "3600")
            .header("Access-Control-Request-Method", "*")
            .header("Access-Control-Request-Headers", "origin, x-request-with")
            .build();
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
                return Response.status(Response.Status.NOT_FOUND)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "*")
                    .header("Access-Control-Allow-Credentials", "false")
                    .header("Access-Control-Max-Age", "3600")
                    .header("Access-Control-Request-Method", "*")
                    .header("Access-Control-Request-Headers", "origin, x-request-with")
                    .build();
            } else {
                User user = objectMapper.readValue(response, User.class);
                return Response.ok(user, MediaType.APPLICATION_JSON)
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
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Headers", "*")
            .header("Access-Control-Allow-Credentials", "false")
            .header("Access-Control-Max-Age", "3600")
            .header("Access-Control-Request-Method", "*")
            .header("Access-Control-Request-Headers", "origin, x-request-with")
            .build();
        }
          
    }

    /**
     * Implementation of POST "/user".
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(String jsonString) {

        //TODO: seguire la convenzione delle altre classi Resource per ottenere i dati dal json

        JSONObject jsonObject = new JSONObject(jsonString);

        try {           
            Client client = new Client("localhost", 3030);
            String command = String.format("USER SET %s %s %s", jsonObject.getString("email"), jsonObject.getString("name"), jsonObject.getString("surname"));
            String response = client.sendCommand(command);
            client.close();

            if(response.startsWith("OK, user ID: ")) {
                String userId = response.substring(13).trim();

                StringBuilder jsonResponse = new StringBuilder();
                jsonResponse.append("{\"id\": \"" + userId + "\"}");

                return Response.ok(jsonResponse.toString(), MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "*")
                .header("Access-Control-Allow-Credentials", "false")
                .header("Access-Control-Max-Age", "3600")
                .header("Access-Control-Request-Method", "*")
                .header("Access-Control-Request-Headers", "origin, x-request-with")
                .build();
            } else {
                return Response.status(Response.Status.CONFLICT).entity(response)
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
}