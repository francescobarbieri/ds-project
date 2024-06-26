package it.unimib.sd2024.utils;

import java.io.IOException;

public class DBRequest {

    private Client client;
    private String command;
    private String collectionName;
    private String response;

    public DBRequest (String collectionName) throws IOException {
        this.client = new Client();
        this.collectionName = collectionName;
        this.command = new String();
    }

    // ritorna true se successo o false se insuccesso
    // SET collectionName key {json}
    public DBResponse setDoc(String key, String jsonString) {
        command = String.format("SET %s %s %s", collectionName, key, jsonString);
        System.out.println("Command executed: " + command);
        
        try {
            response = client.sendCommand(command);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new DBResponse(response);
    }

    // ritorna una stringa json da parsare
    // GET collectinName key
    public DBResponse getDoc(String key) {
        command = String.format("GET %s %s", collectionName, key);
        System.out.println("Command executed: " + command);

        try {
            response = client.sendCommand(command);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new DBResponse(response);
    }

    // ritorna una lista di stringhe json da parsare
    // OPPURE una sola stringa json che è un array, e poi quando la parso la trasformo in lista
    // GET ALL collectinName 
    public DBResponse getDocs() {
        command = String.format("GETALL %s", collectionName);
        System.out.println("Command executed: " + command);

        try {
            response = client.sendCommand(command);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new DBResponse(response);
    }
    
    // GET collectinName WHERE key value (for each doc) => ritornare i domini di un utente
    public DBResponse getFilteredDoc(String key, String value) {
        command = String.format("GETALL %s WHERE %s %s", collectionName, key, value);
        System.out.println("Command executed: " + command);

        try {
            response = client.sendCommand(command);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new DBResponse(response);
    }

    // ritorna true se successo o false se insuccesso
    // UPDATE collectionName key {json} 
    public DBResponse update(String key, String jsonString) {
        command = String.format("UPDATE %s %s %s", collectionName, key, jsonString);
        System.out.println("Command executed: " + command);
        
        try {
            response = client.sendCommand(command);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new DBResponse(response);
    }

    // ritorna una stringa json da parsare
    // GET collectinName key
    public DBResponse checkDoc(String key) {
        command = String.format("CHECK %s %s", collectionName, key);
        System.out.println("Command executed: " + command);

        // String response = "+";
        String response = "-CONFLICT";

        return new DBResponse(response);
    }
}
