package it.unimib.sd2024;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientHandler implements Runnable {
    private DBHandler dbHandler = new DBHandler(); 
    private final SocketChannel socketChannel;

    public ClientHandler(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(256);
            while (socketChannel.isConnected()) {
                buffer.clear();
                int numRead = socketChannel.read(buffer);
                if (numRead == -1) {
                    break;
                }

                String receivedString = new String(buffer.array(), 0, numRead).trim();
                System.out.println("Received: " + receivedString);

                String response = handleCommand(receivedString);
                socketChannel.write(ByteBuffer.wrap(response.getBytes()));
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        if (parts.length < 2) {
            return "ERROR: invalid command.\n";
        }
        String operation = parts[0].toUpperCase();

        System.out.println(operation);

        synchronized (dbHandler) {
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
                    return "ERROR: Unknown command.\n";
            }
        }
    }
}

