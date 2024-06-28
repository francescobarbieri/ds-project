package it.unimib.sd2024.utils;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Client {

    private final String ADDRESS = "localhost";
    private final int PORT = 3030;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ExecutorService executorService;

    // Initialize client with server address and port
    public Client() throws IOException {
        this.socket = new Socket(ADDRESS, PORT);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.executorService = Executors.newFixedThreadPool(10);
    }

    // Send command to server and get response
    public String sendCommand(String command) throws IOException {
        Future<String> future;
        String response = null;

        try {
            future = executorService.submit( () -> {
                synchronized (this) {
                    out.println(command);
                    String temp = in.readLine();
                    return temp;
                }
            });
            response = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return response;        
    }

    // Close all connections
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
        executorService.shutdown();
    }
}