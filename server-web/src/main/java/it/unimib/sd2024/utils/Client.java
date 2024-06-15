package it.unimib.sd2024.utils;

import java.io.*;
import java.net.*;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Client(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public String sendCommand(String command) throws IOException {
        out.println(command);
        String temp = in.readLine();

        System.out.println(temp);

        return temp;
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}