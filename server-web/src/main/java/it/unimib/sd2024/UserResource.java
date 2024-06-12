package it.unimib.sd2024;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import it.unimib.sd2024.model.User;
import jakarta.json.JsonException;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Rappresenta la risorsa "example" in "http://localhost:8080/example".
 */
@Path("user")
public class UserResource {
    // Attributi privati statici...

    // Inizializzazione statica.
    static {
        // ...
    }

    /**
     * Implementazione di GET "/user".
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser() {
        // Aprire qui una socket verso il database, fare il comando per ottenere la risposta.
        // ...

        User user = new User(0, "Marco", "Rossi", "maro.rossi@gmail.com");

        return user;
    }
}