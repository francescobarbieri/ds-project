package it.unimib.sd2024.utils;

import java.io.IOException;
import java.util.concurrent.Future;

public class DBRequest {

    private Client client;
    private String command;
    private String collectionName;

    public DBRequest (String collectionName) throws IOException {
        this.client = new Client();
        this.collectionName = collectionName;
        this.command = new String();
    }

    // ritorna true se successo o false se insuccesso
    // SET collectionName key {json}
    public DBResponse setDoc(String key, String jsonString) {
        command = String.format("SET %s %s %s", collectionName, key, jsonString);
        return new DBResponse(sendCommand(command));
    }

    // ritorna una stringa json da parsare
    // GET collectinName key
    public DBResponse getDoc(String key) {
        command = String.format("GET %s %s", collectionName, key);
        return new DBResponse(sendCommand(command));
    }

    // ritorna una lista di stringhe json da parsare
    // OPPURE una sola stringa json che è un array, e poi quando la parso la trasformo in lista
    // GET ALL collectinName 
    public DBResponse getDocs() {
        command = String.format("GETALL %s", collectionName);
        return new DBResponse(sendCommand(command));
    }
    
    // GET collectinName WHERE key value (for each doc) => ritornare i domini di un utente
    public DBResponse getFilteredDoc(String key, String value) {
        command = String.format("GETALL %s WHERE %s %s", collectionName, key, value);
        return new DBResponse(sendCommand(command));
    }

    // ritorna true se successo o false se insuccesso
    // UPDATE collectionName key {json} 
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
