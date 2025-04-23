package io.github.room;

import io.github.network.server.PlayerConnection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    private static RoomManager instance = new RoomManager();
    private Map<String, Room> rooms = new ConcurrentHashMap<>();

    public static RoomManager getInstance() {
        return instance;
    }

    public Map<String, Room> getEmptyRooms() {
        Map<String, Room> emptyRooms = new HashMap<>();
        for (Map.Entry<String, Room> entry : rooms.entrySet()) {
            Room room = entry.getValue();
            if (room.canAcceptPlayer()) {
                emptyRooms.put(entry.getKey(), room);
            }
        }
        return emptyRooms;
    }

    public synchronized Room createRoom(PlayerConnection owner) {
        String roomId = UUID.randomUUID().toString().substring(0, 6);
        Room room = new Room(roomId, owner);
        rooms.put(roomId, room);
        return room;
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public void removeRoom(String roomId) {
        rooms.remove(roomId);
    }

    public void handleInput(PlayerConnection player, String input) {
        String[] parts = input.split(":");
        System.out.println("Processing message: " + input);

        switch (parts[0]) {
        case "create":
            Room room = createRoom(player);
            player.send("room_created:" + room.getRoomId());
            break;
        case "auto":
            Matchmaker.getInstance().autoJoin(player);
            break;
        case "join":
            if (parts.length < 3) {
                player.send("lack_of_information");
            }
            Room targetRoom = rooms.get(parts[1]);
            player.name = parts[2];
            if (targetRoom != null) {
                targetRoom.requestJoin(player);
            } else {
                player.send("room_not_found");
            }
            break;
        case "approve":
            Room approveRoom = player.currentRoom;
            if (approveRoom != null) {
                approveRoom.approvePlayer(parts[1]);
            }
            break;
        case "leave":
            if (player.currentRoom != null) {
                player.currentRoom.removePlayer(player);
            }
            break;
        }
    }
}
