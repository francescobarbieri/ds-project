package it.unimib.sd2024;

import java.net.*;
import java.nio.channels.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;

import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Main class that starts the database server.
 */
public class Main {
    private final static boolean POPULATE = true;
    private final Selector selector; // Selector for managing multiple channels
    private final InetSocketAddress listenAddress; // Address to listen on
    public static final int PORT = 3030; // Default port number
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10); // Thread pool to handle concurrent connections

    /**
     * Constructor to initialize the server with a specific address and port.
     *
     * @param address the address to bind the server to
     * @param port the port to bind the server to
     * @throws IOException if an I/O error occurs
     */
    public Main(String address, int port) throws IOException {
        listenAddress = new InetSocketAddress(address, port);
        selector = Selector.open(); // Open a selector
    }

    /**
     * Starts the server to listen for incoming connections and handle them.
     *
     * @throws IOException if an I/O error occurs
     */
    public void startServer() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open(); // Open a server socket channel
        serverChannel.configureBlocking(false); // Configure it to be non-blocking
        serverChannel.socket().bind(listenAddress); // Bind the server socket to the specified address
        serverChannel.register(selector, SelectionKey.OP_ACCEPT); // Register the server socket with the selector for accept operation

        populateDatabase();

        System.out.println("Database started on port: " + PORT);

        // Main loop to handle incoming connections and data
        while (true) {
            selector.select();  
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (!key.isValid()) continue;

                if (key.isAcceptable()) {
                    accept(key); // Handle accept event
                }
            }
        }
    }
    
    /**
     * Accepts a new connection and registers it with the selector.
     *
     * @param key the selection key
     * @throws IOException if an I/O error occurs
     */
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel(); // Get the server channel
        SocketChannel socketChannel = serverChannel.accept(); // Accept the new connection
        socketChannel.configureBlocking(true); // Configure it to be blocking for simplicity

        // Submit a new client handler task to the thread pool
        threadPool.submit(new ClientHandler(socketChannel));
        System.out.println("Connection accepted: " + socketChannel.getRemoteAddress());
    }

    private void populateDatabase() {
        Path starterPath = Paths.get("starter/starter.json");
        Path collectionPath = Paths.get("collections");

        try {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(collectionPath);
            // If collections folder is empty and starter file is defined, populate the database accordingly
            if(!directoryStream.iterator().hasNext() && Files.exists(starterPath) && POPULATE) {
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

    /**
     * Main method to start the database server.
     *
     * @param args command line arguments
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        try {
            new Main("localhost", PORT).startServer(); // Create and start the server
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}