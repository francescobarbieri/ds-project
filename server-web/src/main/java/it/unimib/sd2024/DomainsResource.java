package it.unimib.sd2024;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import it.unimib.sd2024.model.Domain;
import it.unimib.sd2024.model.PurchaseRequest;
import it.unimib.sd2024.model.User;
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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Rappresenta la risorsa "example" in "http://localhost:8080/example".
 */
@Path("domains")
public class DomainsResource implements ContainerResponseFilter {
    static { }

    @Override
    public void filter(final ContainerRequestContext requestContext,
    final ContainerResponseContext cres) throws IOException {
        cres.getHeaders().add("Access-Control-Allow-Origin", "*");
        cres.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        cres.getHeaders().add("Access-Control-Allow-Credentials", "true");
        cres.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        cres.getHeaders().add("Access-Control-Max-Age", "1209600");
    }

    /**
     * Implementazione di GET "/domains".
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomains(@QueryParam("user") String userid) {

        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{\"domain\": \" hello.com \",");
        jsonString.append("\"availability\": true }");

        String jsonResponse = jsonString.toString(); 

        return Response.ok(jsonResponse, MediaType.APPLICATION_JSON).build();
    }

    /**
     * Implementazione di GET "/domains/{domain}/availability".
     */
    @Path("/{domain}/availability")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkAvailabilityDomain(@PathParam("domain") String domain) {

        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{\"domain\": \"" + domain + "\",");
        jsonString.append("\"availability\": true }");

        String jsonResponse = jsonString.toString(); 

        return Response.ok(jsonResponse, MediaType.APPLICATION_JSON)
        .build();
    }

    @Path("/{domain}/buy")
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
    * Implementation of POST "/{domain}/buy".
    */

    @Path("/{domain}/buy")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response buyDomain(@PathParam("domain") String domain, String body) {

        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{\"domain\": \"" + domain + "\",");
        jsonString.append("\"availability\": true,");
        jsonString.append("\"body\": " + body + " }");

        String jsonResponse = jsonString.toString(); 

        return Response.ok(jsonResponse, MediaType.APPLICATION_JSON)
            .build();
    }
}