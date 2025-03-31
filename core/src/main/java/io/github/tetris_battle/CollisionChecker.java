package io.github.tetris_battle;

import com.badlogic.gdx.utils.Array;

public class CollisionChecker {
    private static CollisionChecker instance;

    public static CollisionChecker getInstance() {
        if (instance == null)
            instance = new CollisionChecker();
        return instance;
    }

    public boolean checkCollision(Tetromino piece, Board board) {
        Array<int[]> shape = piece.getShape();
        int row = piece.getRow(), col = piece.getCol();

        for (int[] block : shape) {
            int blockRow = row + block[0];
            int blockCol = col + block[1];

            // Check if out of bounds
            if (blockRow >= board.getROWS() || blockCol < 0 || blockCol >= board.getCOLS()) {
                return true;
            }
            // Check if placed on top of other pieces
            if (board.getGrid()[blockRow][blockCol] != -1) {
                return true;
            }
        }
        return false;
    }
}
