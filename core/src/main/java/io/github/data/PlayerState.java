package io.github.data;

import java.io.Serializable;

public class PlayerState implements Serializable {
    public int[][] grid;
    public TetrominoDTO currentPiece;
    public int pieceIndex;
    public float health;
}
