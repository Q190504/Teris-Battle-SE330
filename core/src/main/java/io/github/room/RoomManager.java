package io.github.room;

import com.google.gson.Gson;
import io.github.network.server.PlayerConnection;
import io.github.tetris_battle.Tetromino;
import io.github.data.TetrominoDTO;

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

        String roomId;
        Room room;
        String json;
        switch (parts[0]) {
        case "create":
            room = createRoom(player);
            player.send("room_created:" + room.getRoomId());
            break;
        case "auto":
            Matchmaker.getInstance().autoJoin(player);
            break;
        case "join":
            if (parts.length < 3) {
                player.send("lack_of_information");
            }
            room = rooms.get(parts[1]);
            player.name = parts[2];
            System.out.println(player);
            if (room != null) {
                room.requestJoin(player);
            } else {
                player.send("room_not_found");
            }
            break;
        case "accept":
            room = player.currentRoom;
            if (room != null) {
                room.approvePlayer(parts[1]);
            }
            break;
        case "leave":
            if (player.currentRoom != null) {
                player.currentRoom.removePlayer(player);
            }
            break;
        case "game_state":
            if (parts.length < 3) {
                player.send("invalid_game_state");
                break;
            }
            room = player.currentRoom;
            if (room != null) {
                room.broadcastExcept(player, input);
            }
            break;
        case "request_piece":
            int index = Integer.parseInt(parts[1]);
            room = player.currentRoom;

            if (room != null) {
                Tetromino piece = room.getPiece(index);
                TetrominoDTO dto = piece.toDTO();
                json = new Gson().toJson(dto);
                player.send("piece:" + json);

                piece = room.getNextPiece(index);
                dto = piece.toDTO();
                json = new Gson().toJson(dto);
                player.send("next_piece:" + json);

            }
            break;
        case "start":
            Room startRoom = player.currentRoom;
            if (startRoom != null && startRoom.getOwner() == player) {
                startRoom.startGame();
                System.out.println("Starting game");
            } else {
                player.send("not_owner_or_invalid_room");
            }
            break;

        }


    }
}
