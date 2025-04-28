package io.github.network.server;
import io.github.room.Room;
import io.github.room.RoomManager;

import java.io.*;
import java.net.*;

public class PlayerConnection implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    public Room currentRoom;
    public String name;
    public boolean isApproved = false;

    public PlayerConnection(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void run() {
        try {
            String input;
            while ((input = in.readLine()) != null) {
                RoomManager.getInstance().handleInput(this, input);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } finally {
            try {
                socket.close();
                System.out.println("Connection closed for: " + socket.getInetAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String msg) {
        out.println(msg);
    }
}
