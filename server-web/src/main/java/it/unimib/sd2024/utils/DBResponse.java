package it.unimib.sd2024.utils;

import jakarta.ws.rs.core.Response;

public class DBResponse {

    private String fullResponse;
    private String response;
    private String errorMessage;

    public DBResponse (String fullResponse) {
        // System.out.println(fullResponse);
        this.fullResponse = fullResponse;
        if(!isOk()) {
            this.errorMessage = fullResponse.substring(1);
        }
        else {
            this.errorMessage = "";
            this.response = fullResponse.substring(1);
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getResponse() {
        return response;
    }

    public boolean isOk() {
        if( fullResponse.charAt(0) == '+' &&
            fullResponse != null &&
            !fullResponse.isEmpty()
        )
            return true;
        return false;
    }

    public Response returnErrors() {
        switch (errorMessage) {
            case "NOTFOUND":
                return ResponseBuilderUtil.build(Response.Status.NOT_FOUND);
            case "CONFLICT":
                return ResponseBuilderUtil.build(Response.Status.CONFLICT);
            default:
                return ResponseBuilderUtil.build(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String toString() {
        return "Response{" +
                ", response='" + response + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
