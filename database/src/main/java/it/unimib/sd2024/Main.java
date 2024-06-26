package it.unimib.sd2024;

import java.net.*;
import java.util.Iterator;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/**
 * Main class that starts the database server.
*/
public class Main {
    private Selector selector; // Selector for managing multiple channels
    
    private InetSocketAddress listenAddress; // Address to listen on
    public static final int PORT = 3030; // Default port number

    private static DBHandler dbHandler = new DBHandler();

    /**
     * Constructor to initialize the server with a specific address and port.
     *
     * @param address the address to bind the server to
     * @param port the port to bind the server to
     * @throws IOException if an I/O error occurs
     */
    public Main(String address, int port) throws IOException {
        listenAddress = new InetSocketAddress(address, port);
    }

    /**
     * Starts the server to listen for incoming connections and handle them.
     *
     * @throws IOException if an I/O error occurs
     */
    public void startServer() throws IOException {
        this.selector = Selector.open(); // Open a selector
        ServerSocketChannel serverChannel = ServerSocketChannel.open(); // Open a server socket channel
        serverChannel.configureBlocking(false); // Configure it to be non-blocking
        serverChannel.socket().bind(listenAddress); // Bind the server socket to the specified address
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT); // Register the server socket with the selector for accept operation

        System.out.println("Database started on port: " + PORT);

        // Main loop to handle incoming connections and data
        while (true) {
            // Wait for events
            selector.select();  
            // Get the keys for which events occurred
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            // Iterate over the keys
            while(keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if(!key.isValid()) continue;

                if(key.isAcceptable()) {
                    accept(key); // Handle accept event
                } else if (key.isReadable()) {
                    read(key); // Handle read event
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
        socketChannel.configureBlocking(false); // Configure it to be non-blocking
        socketChannel.register(this.selector, SelectionKey.OP_READ);  // Register it with the selector for read operations
        System.out.println("Connection accepted: " + socketChannel.getLocalAddress());
    }

    /**
     * Reads data from a connected client.
     *
     * @param key the selection key
     * @throws IOException if an I/O error occurs
     */
    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel(); // Get the socket channel
        ByteBuffer buffer = ByteBuffer.allocate(256); // Allocate a buffer for reading data
        int numRead = -1;

        try {
            numRead = socketChannel.read(buffer); // Read data into the buffer
        } catch (IOException e) {
            System.out.println("Error reading data: " + e);
        }

        if(numRead == -1) {
            this.closeConnection(key); // Close the connection if no data was read (end of stream)
            return; 
        }

        // Process the received data
        String receivedString = new String(buffer.array()).trim();
        System.out.println("Received: " + receivedString);

        String response = handleCommand(receivedString); // Handle the received command
        byte[] responseBytes = response.getBytes();
        int responseLength = responseBytes.length;
        int offset = 0;

        // Write the response in chunks
        while (offset < responseLength) {
            int bytesToWrite = Math.min(responseLength - offset, buffer.capacity());
            buffer.clear();
            buffer.put(responseBytes, offset, bytesToWrite);
            buffer.flip();
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer); // Write data to the client
            }
            offset += bytesToWrite;
        }
    }

    /**
     * Handles a command received from a client.
     *
     * @param command the command string
     * @return the response string
     */
    private String handleCommand(String command) {
        String[] parts = command.split(" ", 2);
        //TODO: check commands number of parts if(parts.length < 2) return "ERROR: invalid command. \n"; // Check if the command is valid
        String operation = parts[0].toUpperCase();

        System.out.println(operation);

        switch (operation) {
            case "SET":
                return dbHandler.setDoc(command);
            case "GET":
                return dbHandler.getDoc(command);
            case "GETALL":
                return dbHandler.getDocs(command);
            case "UPDATE":
                return dbHandler.update(command);
            default:
                return "-ERROR: Unknown command.\n";
        }
    }

    /**
     * Closes the connection associated with the given key.
     *
     * @param key the selection key
     * @throws IOException if an I/O error occurs
     */
    private void closeConnection(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel(); // Get the socket channel
        socketChannel.close();  // Close the channel
        key.cancel(); // Cancel the key
        System.out.println("Connection closed");
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
            dbHandler = new DBHandler();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

