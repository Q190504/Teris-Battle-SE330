package io.github.network.client;

import java.io.*;
import java.net.*;

public class ClientConnection implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private MessageListener listener;

    public interface MessageListener {
        void onMessage(String msg);
    }

    public ClientConnection(String host, int port, MessageListener listener) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.listener = listener;
    }

    public void start() {
        new Thread(this).start(); // start listening for server messages
    }

    public void send(String msg) {
        System.out.println("Client sent: " + msg);
        out.println(msg);
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (listener != null) {
                    listener.onMessage(line);
                } else {
                    System.out.println("Server: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Connection closed: " + e.getMessage());
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
}
