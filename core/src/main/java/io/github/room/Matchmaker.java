package io.github.room;

import io.github.network.server.PlayerConnection;

public class Matchmaker {
    private static Matchmaker instance = new Matchmaker();

    public static Matchmaker getInstance() {
        return instance;
    }

    public void autoJoin(PlayerConnection player) {
        for (Room room : RoomManager.getInstance().getEmptyRooms().values()) {
            if (!room.equals(player.currentRoom)) {
                room.requestJoin(player);
                return;
            }
        }
        player.send("no_available_rooms");
    }
}
