package it.unimib.sd2024;

import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import java.util.Iterator;

import javax.swing.text.html.StyleSheet;

import it.unimib.sd2024.handlers.UserHandler;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * Classe principale in cui parte il database.
 */
public class Main {
    private Selector selector;
    
    private InetSocketAddress listenAddress;
    public static final int PORT = 3030;

    private final UserHandler userHandler = new UserHandler();

    public Main(String address, int port) throws IOException {
        listenAddress = new InetSocketAddress(address, port);
    }

    public void startServer() throws IOException {
        this.selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        System.out.println("Database started on port: " + PORT);


        while (true) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while(keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if(!key.isValid()) continue;

                if(key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(this.selector, SelectionKey.OP_READ);
        System.out.println("Connection accepted: " + socketChannel.getLocalAddress());
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(256);
        int numRead = -1;

        try {
            numRead = socketChannel.read(buffer);
        } catch (IOException e) {
            System.out.println("Error reading data: " + e);
        }

        if(numRead == -1) {
            this.closeConnection(key);
            return;
        }

        String receivedString = new String(buffer.array()).trim();
        System.out.println("Received: " + receivedString);

        String resposnse = handleCommand(receivedString);
        buffer.flip();
        buffer = ByteBuffer.wrap(resposnse.getBytes());
        socketChannel.write(buffer);
    }

    private String handleCommand(String command) {
        String[] parts = command.split(" ");
        if(parts.length < 2) return "ERROR: invalid command. \n";
        String resource = parts[0].toUpperCase();

        switch (resource) {
            case "USER":
                return userHandler.handle(parts);
            //case "DOMAIN":
            //    return domainHandler.handle(parts);
            case "EXIT":
                return("bye\n");
            default:
                return("ERROR: unknown resoruce. \n");
        }
    }

    private void closeConnection(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.close();
        key.cancel();
        System.out.println("Connection closed");
    }

    /**
     * Metodo principale di avvio del database.
     *
     * @param args argomenti passati a riga di comando.
     *
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        try {
            new Main("localhost", PORT).startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

