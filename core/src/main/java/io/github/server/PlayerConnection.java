package io.github.server;
import io.github.server.room.Room;
import io.github.server.room.RoomManager;

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
                if (input.equals("ping")) {
                    this.send("pong");
                } else {
                    RoomManager.getInstance().handleInput(this, input);
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } finally {
            try {
                socket.close();
                RoomManager.getInstance().handleInput(this, "leave");
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
