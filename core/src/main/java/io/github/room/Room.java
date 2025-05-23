package io.github.room;
import io.github.network.server.PlayerConnection;
import io.github.tetris_battle.Tetromino;
import io.github.tetris_battle.TetrominoSpawner;
import io.github.ui.Messages;

import java.util.*;

public class Room {
    private String roomId;
    private PlayerConnection owner;
    private Map<String, PlayerConnection> pending = new HashMap<>();
    private Map<String, PlayerConnection> approved = new HashMap<>();
    private TetrominoSpawner tetrominoSpawner;

    public Room(String roomId, PlayerConnection owner) {
        this.roomId = roomId;
        this.owner = owner;
        tetrominoSpawner = new TetrominoSpawner();
        owner.currentRoom = this;
        approved.put(owner.toString(), owner);
        System.out.println(approved);
    }

    private boolean gameStarted = false;

    public void startGame() {
        if (approved.size() == 2) {
            gameStarted = true;
            broadcast("game_start:" + roomId);
        } else {
            System.out.println(approved);
            owner.send("cannot_start_not_enough_players");
        }
    }

    public PlayerConnection getOwner() {return owner;}
    public Tetromino getPiece(int index) {
        return tetrominoSpawner.getTetromino(index);
    }
    public Tetromino getNextPiece(int index) {
        return tetrominoSpawner.peekNextTetromino(index + 1);
    }
    public void broadcast(String message) {
        for (PlayerConnection p : approved.values()) {
            p.send(message);
            System.out.println("broadcasting to..." + p.name);
        }
    }

    public void broadcastExcept(PlayerConnection sender, String message) {
        for (PlayerConnection p : approved.values()) {
            if (!p.equals(sender)) {
                p.send(message);
            }
        }
    }

    public String getRoomId() {
        return roomId;
    }

    public boolean canAcceptPlayer() {
        return approved.size() < 2;
    }

    public void requestJoin(PlayerConnection player) {
        if (!canAcceptPlayer()) {
            player.send("room_full");
            return;
        }
        pending.put(player.toString(), player);
        player.currentRoom = this;
        player.send(Messages.JOIN_REQUEST + Messages.SEPARATOR + roomId);
        owner.send(Messages.JOIN_REQUEST + Messages.SEPARATOR + player.name + Messages.SEPARATOR + player);
    }

    public void approvePlayer(String playerKey) {
        if (!canAcceptPlayer()) {
            owner.send("cannot_approve_room_full");
            return;
        }

        PlayerConnection player = pending.remove(playerKey);
        if (player != null) {
            approved.put(playerKey, player);
            player.isApproved = true;
            player.send(Messages.APPROVED + Messages.SEPARATOR + roomId + Messages.SEPARATOR + owner.name);

            for (PlayerConnection rejectedPlayer : pending.values()) {
                rejectedPlayer.send(Messages.REJECTED + Messages.SEPARATOR + roomId);
                rejectedPlayer.currentRoom = null;
            }
            pending.clear();
        }
    }

    public void removePlayer(PlayerConnection player) {
        pending.remove(player.toString());
        approved.remove(player.toString());
        broadcastExcept(player, Messages.PLAYER_LEFT + Messages.SEPARATOR + player.name +
            Messages.SEPARATOR + player);
        if (player == owner) {
            // If owner leaves, close room and notify others
            for (PlayerConnection p : approved.values()) {
                p.send(Messages.ROOM_CLOSED);
                p.currentRoom = null;
            }
            for (PlayerConnection p : pending.values()) {
                p.send(Messages.ROOM_CLOSED);
                p.currentRoom = null;
            }
            RoomManager.getInstance().removeRoom(roomId);
        }
        player.currentRoom = null;
    }
}
