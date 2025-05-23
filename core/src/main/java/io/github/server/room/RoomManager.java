package io.github.server.room;

import com.google.gson.Gson;
import io.github.server.PlayerConnection;
import io.github.logic.tetris_battle.board.Tetromino;
import io.github.logic.data.TetrominoDTO;
import io.github.logic.utils.Messages;

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
        case Messages.CREATE:
            room = createRoom(player);
            player.name = parts[1];
            player.send(Messages.ROOM_CREATED + Messages.SEPARATOR + room.getRoomId());
            break;

        case Messages.AUTO:
            player.name = parts[1];
            Matchmaker.getInstance().autoJoin(player);
            break;

        case Messages.JOIN:
            room = rooms.get(parts[1]);
            player.name = parts[2];
            System.out.println(player);
            if (room != null) {
                room.requestJoin(player);
            } else {
                player.send(Messages.ROOM_NOT_FOUND);
            }
            break;

        case Messages.ACCEPT:
            room = player.currentRoom;
            if (room != null) {
                room.approvePlayer(parts[1]);
            }
            break;

        case Messages.LEAVE:
            if (player.currentRoom != null) {
                player.currentRoom.removePlayer(player);
            }
            break;

        case Messages.REQUEST_PIECE:
            int index = Integer.parseInt(parts[1]);
            room = player.currentRoom;

            if (room != null) {
                Tetromino piece = room.getPiece(index);
                TetrominoDTO dto = piece.toDTO();
                json = new Gson().toJson(dto);
                player.send(Messages.PIECE + Messages.SEPARATOR + json);

                piece = room.getNextPiece(index);
                dto = piece.toDTO();
                json = new Gson().toJson(dto);
                player.send(Messages.NEXT_PIECE + Messages.SEPARATOR + json);
            }
            break;

        case Messages.START:
            Room startRoom = player.currentRoom;
            if (startRoom != null && startRoom.getOwner() == player) {
                startRoom.startGame();
                System.out.println("Starting game");
            } else {
                player.send(Messages.NOT_OWNER_OR_INVALID_ROOM);
            }
            break;

        default:
            room = player.currentRoom;
            if (room != null) {
                room.broadcastExcept(player, input);
            }
            break;
        }
    }

}
