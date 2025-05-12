package io.github.room;
import io.github.network.server.PlayerConnection;
import io.github.tetris_battle.Tetromino;
import io.github.tetris_battle.TetrominoSpawner;

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
        owner.send("join_request:" + player.name + ":" + player);
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
            player.send("approved:" + roomId);

            for (PlayerConnection rejectedPlayer : pending.values()) {
                rejectedPlayer.send("rejected:" + roomId);
                rejectedPlayer.currentRoom = null;
            }
            pending.clear();
        }
    }

    public void removePlayer(PlayerConnection player) {
        pending.remove(player.toString());
        approved.remove(player.toString());
        player.send("left_room");
        player.currentRoom = null;
    }
}
