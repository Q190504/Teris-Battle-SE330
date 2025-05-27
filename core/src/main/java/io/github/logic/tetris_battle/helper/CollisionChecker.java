package io.github.logic.tetris_battle.helper;

import com.badlogic.gdx.utils.Array;
import io.github.logic.tetris_battle.board.Board;
import io.github.logic.tetris_battle.board.Tetromino;

public class CollisionChecker {
    private static CollisionChecker instance;

    public static CollisionChecker getInstance() {
        if (instance == null)
            instance = new CollisionChecker();
        return instance;
    }

    public boolean checkCollision(Tetromino piece, Board board) {
        if (piece == null || board == null) {
            return true;
        }

        Array<int[]> shape = piece.getShape();
        int row = piece.getRow();
        int col = piece.getCol();
        int[][] grid = board.getGrid();
        int ROWS = board.getROWS();
        int COLS = board.getCOLS();

        if (shape == null || grid == null) {
            return true;
        }

        if (shape.size == 0 || shape.size > 4) {
            return true;
        }

        for (int i = 0; i < shape.size; i++) {
            if (shape.get(i) == null || shape.get(i).length == 0 || shape.get(i).length > 4) {
                return true;
            }

            for (int j = 0; j < shape.get(i).length; j++) {
                if (shape.get(i)[j] == 1) {
                    int blockRow = row + i;
                    int blockCol = col + j;

                    if (blockRow < 0 || blockRow >= ROWS || blockCol < 0 || blockCol >= COLS) {
                        return true;
                    }
                    if (grid[blockRow][blockCol] != -1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
