package it.unimib.sd2024.utils;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ResponseBuilderUtil {

    // Common headers added to all responses
    private static Response.ResponseBuilder addCommonHeaders (Response.ResponseBuilder responseBuilder) {
        return responseBuilder
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "*")
            .header("Access-Control-Allow-Headers", "*")
            .header("Access-Control-Allow-Credentials", "false")
            .header("Access-Control-Max-Age", "3600")
            .header("Access-Control-Request-Method", "*")
            .header("Access-Control-Request-Headers", "origin, x-requested-with");
    }

    // Build response with status
    public static Response build(Response.Status status) {
        Response.ResponseBuilder responseBuilder = Response.status(status);
        return addCommonHeaders(responseBuilder).build();
    }

    // Build response with status and entity
    public static Response build(Response.Status status, Object entity) {
        Response.ResponseBuilder responseBuilder = Response.status(status).entity(entity);
        return addCommonHeaders(responseBuilder).build();
    }

    // Build response with status, entity, and media type
    public static Response build(Response.Status status, Object entity, MediaType mediaType) {
        Response.ResponseBuilder responseBuilder = Response.status(status).entity(entity).type(mediaType);
        return addCommonHeaders(responseBuilder).build();
    }

    // Build empty OK response 
    public static Response buildOkResponse() {
        Response.ResponseBuilder responseBuilder = Response.ok();
        return addCommonHeaders(responseBuilder).build();
    }

    // Build OK response with entity and media type
    public static Response buildOkResponse(Object entity, String mediaType) {
        Response.ResponseBuilder responseBuilder = Response.ok(entity, mediaType);
        return addCommonHeaders(responseBuilder).build();
    }
}
