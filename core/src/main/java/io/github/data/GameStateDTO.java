package io.github.data;

import java.io.Serializable;

public class GameStateDTO implements Serializable {
    public String roomId;
    public PlayerState player;
    public int ack;
}
