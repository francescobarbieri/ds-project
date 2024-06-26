package it.unimib.sd2024;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONTokener;

public class DBHandler {

    public DBHandler() {
        Path starterPath = Paths.get("starter/starter.json");
        Path collectionPath = Paths.get("collections");

        try {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(collectionPath);
            // If collections folder is empty and starter file is defined, populate the database accordingly
            if(!directoryStream.iterator().hasNext() && Files.exists(starterPath)) {
                System.out.println("Populating the database ...");

                // Read the starter JSON file
                FileInputStream fis = new FileInputStream(starterPath.toString());
                JSONTokener tokener = new JSONTokener(fis);
                JSONObject jsonObject = new JSONObject(tokener);
                fis.close();

                // Iterate over the keys in the JSON object
                Iterator<String> keys = jsonObject.keys();
                while(keys.hasNext()) {
                    String fileName = keys.next();
                    JSONObject fileData = jsonObject.getJSONObject(fileName);

                    // Write the data in the corrisponding collection file
                    try (FileWriter file = new FileWriter("collections/" + fileName + ".json")) {
                        file.write(fileData.toString(4));
                    }
                }

                System.out.println("Database populated!");
            } else {
                System.out.println("Database already populated.");
            }
            directoryStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String setDoc(String command) {
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

    public String getDocs(String command) {
        int whereIndex = command.indexOf("WHERE");

        try {
            if(whereIndex != -1) {
                String collectionName = command.substring(0, whereIndex).trim().split(" ")[1].toLowerCase();
                String keyFilter = command.substring(whereIndex + 5).trim().split(" ")[0].toLowerCase();
                String valueFilter = command.substring(whereIndex + 5).trim().split(" ")[1].toLowerCase();
    
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
                        if(value.equals(valueFilter)) {
                            responseJson.put(key, innerObject);
                        }
                    }
                }

                System.out.println(responseJson.toString());

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

    public String update(String command) {
        String[] parts = command.split(" ");

        String collectionName = parts[1].toLowerCase();
        String key = parts[2].toLowerCase();
        String newJsonString = parts[3].toLowerCase();

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
                innerObject.put(key, updateData.get(innerKey));
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
