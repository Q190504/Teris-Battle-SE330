package io.github.room;
import io.github.network.server.PlayerConnection;

import java.util.*;

public class Room {
    private String roomId;
    private PlayerConnection owner;
    private Map<String, PlayerConnection> pending = new HashMap<>();
    private Map<String, PlayerConnection> approved = new HashMap<>();

    public Room(String roomId, PlayerConnection owner) {
        this.roomId = roomId;
        this.owner = owner;
        owner.currentRoom = this;
        approved.put(owner.toString(), owner);
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
        owner.send("join_request:" + player.name);
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
        }
    }

    public void removePlayer(PlayerConnection player) {
        pending.remove(player.toString());
        approved.remove(player.toString());
        player.send("left_room");
        player.currentRoom = null;
    }
}
