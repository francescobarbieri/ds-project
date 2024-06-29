package it.unimib.sd2024.utils;

import java.io.IOException;

public class DBRequest {

    private Client client;
    private String command;
    private String collectionName;

    public DBRequest (String collectionName) throws IOException {
        this.client = new Client();
        this.collectionName = collectionName;
        this.command = new String();
    }

    public DBResponse setDoc(String key, String jsonString) {
        command = String.format("SET %s %s %s", collectionName, key, jsonString);
        return new DBResponse(sendCommand(command));
    }

    public DBResponse getDoc(String key) {
        command = String.format("GET %s %s", collectionName, key);
        return new DBResponse(sendCommand(command));
    }

    public DBResponse getDocs() {
        command = String.format("GETALL %s", collectionName);
        return new DBResponse(sendCommand(command));
    }
    
    public DBResponse getFilteredDoc(String key, String value) {
        command = String.format("GETALL %s WHERE %s %s", collectionName, key, value);
        return new DBResponse(sendCommand(command));
    }

    public DBResponse update(String key, String jsonString) {
        command = String.format("UPDATE %s %s %s", collectionName, key, jsonString);       
        return new DBResponse(sendCommand(command));
    }

    private String sendCommand(String command) {
        String response = null;
        try {
            response = client.sendCommand(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Command executed: " + command);
        return response;
    }
}
