package it.unimib.sd2024;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.json.JSONObject;

public class DBHandler {

    public DBHandler() { }
    
    /**
     * Sets a new document in the specified collection or updates an existing one.
     *
     * @param command the command string containing collection name, key, and JSON data
     * @return "+\n" if successful, or "-ERROR: ..." on failure
     */
    public synchronized String setDoc(String command) {
        String[] parts = command.split(" ", 4);

        String collectionName = parts[1].toLowerCase();
        String key = parts[2].toLowerCase();
        String jsonData = parts[3];

        Path path = Paths.get("collections/" + collectionName + ".json");
        try {
            if(!Files.exists(path)) {
                Files.createFile(path);
                JSONObject emptyJsonObject = new JSONObject();
                Files.write(path, emptyJsonObject.toString(4).getBytes());
            }
            
            // Read the existing content in the collection
            String collectionString = new String(Files.readAllBytes(path));
            JSONObject collection = new JSONObject(collectionString);

            if(collection.has(key)) {
                System.out.println("Key already exists.");
                return "-ERROR: Key already exists.\n";
            }

            JSONObject newDocument = new JSONObject(jsonData);
            collection.put(key, newDocument);
            
            Files.write(path, collection.toString(4).getBytes());
            System.out.println("In collection /"+ collectionName + " created a new document with key: " + key);

            return "+\n";
        } catch (IOException e) {
            e.printStackTrace();
            return "-ERROR: Unknown error.\n";
        }
    }

    /**
     * Retrieves a document from the specified collection based on the key.
     *
     * @param command the command string containing collection name and key
     * @return JSON representation of the document if found, or "-NOTFOUND\n" if not found
     *         "-ERROR: ..." on failure
     */
    public String getDoc(String command) {
        String[] parts = command.split(" ");

        String collectionName = parts[1].toLowerCase();
        String key = parts[2].toLowerCase();

        Path path = Paths.get("collections/" + collectionName + ".json");
        try {
            if(!Files.exists(path)) {
                return "-ERROR: Collection not found.\n";
            }
            
            // Read the existing content in the collection
            String collectionString = new String(Files.readAllBytes(path));
            JSONObject collection = new JSONObject(collectionString);

            if(collection.has(key)) {
                System.out.println("Document found.");
                return "+" + collection.getJSONObject(key).toString() + "\n";
            } else {
                System.out.println("Document NOT found.");
                return "-NOTFOUND\n";
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            return "-ERROR: Unknown error.\n";
        }
    }

    /**
     * Retrieves documents from the specified collection based on a optional WHERE clause.
     *
     * @param command the command string containing collection name and WHERE clause
     * @return JSON representation of matched documents, or "-ERROR: ..." on failure
     */
    public String getDocs(String command) {
        int whereIndex = command.indexOf("WHERE");

        try {
            if(whereIndex != -1) {
                String collectionName = command.substring(0, whereIndex).trim().split(" ")[1].toLowerCase();
                String keyFilter = command.substring(whereIndex + 5).trim().split(" ")[0];
                String valueFilter = command.substring(whereIndex + 5).trim().split(" ")[1];
    
                Path path = Paths.get("collections/" + collectionName + ".json");
                if(!Files.exists(path)) {
                    System.out.println("Collection not found.");
                    return "-ERROR: Collection not found.\n";
                }
                    
                // Read the existing content in the collection
                String collectionString = new String(Files.readAllBytes(path));
                JSONObject collection = new JSONObject(collectionString);
                
                JSONObject responseJson = new JSONObject();

                Iterator<String> keys = collection.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject innerObject = collection.getJSONObject(key);

                    if(innerObject.has(keyFilter)) {
                        String value = innerObject.getString(keyFilter);
                        if(value.trim().equals(valueFilter.trim())) {
                            responseJson.put(key, innerObject);
                        }
                    }
                }
                return "+" + responseJson.toString() + "\n";
            } else {
                String collectionName = command.trim().split(" ")[1];
    
                Path path = Paths.get("collections/" + collectionName + ".json");
                if(!Files.exists(path)) {
                    System.out.println("Collection not found.");
                    return "-ERROR: Collection not found.\n";
                }
    
                // Read the existing content in the collection
                String collectionString = new String(Files.readAllBytes(path));
                JSONObject collection = new JSONObject(collectionString);

                return "+" + collection.toString() + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "-ERROR: Unknown error.\n";
        }
    }

    /**
     * Updates a document in the specified collection with new JSON data.
     *
     * @param command the command string containing collection name, key, and new JSON data
     * @return "+\n" if successful, or "-ERROR: ..." on failure
     */
    public synchronized String update(String command) {
        String[] parts = command.split(" ");

        String collectionName = parts[1].toLowerCase();
        String key = parts[2].toLowerCase();
        String newJsonString = parts[3];

        JSONObject updateData = new JSONObject(newJsonString);

        try {
            Path path = Paths.get("collections/" + collectionName + ".json");
            if(!Files.exists(path)) {
                System.out.println("Collection not found.");
                return "-ERROR: Collection not found.\n";
            }
            
            // Read the existing content in the collection
            String collectionString = new String(Files.readAllBytes(path));
            JSONObject collection = new JSONObject(collectionString);
    
            JSONObject innerObject = collection.getJSONObject(key);
    
            for(String innerKey : updateData.keySet()) {
                innerObject.put(innerKey, updateData.get(innerKey));
            }

            Files.write(path, collection.toString(4).getBytes());
            System.out.println("In collection /"+ collectionName + " updated the document with key: " + key);

            return "+\n";
        } catch (IOException e) {
            e.printStackTrace();
            return "-ERROR: Unknown error.\n";
        }
    }
}
