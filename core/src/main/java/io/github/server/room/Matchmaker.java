package io.github.server.room;

import io.github.server.PlayerConnection;
import io.github.logic.utils.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Matchmaker {
    private static final Matchmaker instance = new Matchmaker();

    public static Matchmaker getInstance() {
        return instance;
    }

    public void autoJoin(PlayerConnection player) {
        List<Room> emptyRooms = new ArrayList<>(RoomManager.getInstance().getEmptyRooms().values());

        if (!emptyRooms.isEmpty()) {
            int index = ThreadLocalRandom.current().nextInt(emptyRooms.size());
            Room selectedRoom = emptyRooms.get(index);
            selectedRoom.requestJoin(player);
        } else {
            player.send(Messages.ROOM_NOT_FOUND);
        }
    }
}
