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
        Array<int[]> shape = piece.getShape();
        int row = piece.getRow(), col = piece.getCol();
        int[][] grid = board.getGrid();
        int ROWS = board.getROWS();
        int COLS = board.getCOLS();

        for (int i = 0; i < shape.size; i++) {
            for (int j = 0; j < shape.get(i).length; j++) {
                if (shape.get(i)[j] == 1) {
                    int blockRow = row + i;
                    int blockCol = col + j;

                    // Kiểm tra vượt ra ngoài bảng
                    if (blockRow < 0 || blockRow >= ROWS || blockCol < 0 || blockCol >= COLS) {
                        //Gdx.app.log("CollisionChecker", "Out of bounds at row: " + blockRow + ", col: " + blockCol);
                        return true;
                    }
                    // Kiểm tra chồng lấp với khối khác
                    if (grid[blockRow][blockCol] != -1) {
                        //Gdx.app.log("CollisionChecker", "Collision with block at row: " + blockRow + ", col: " + blockCol);
                        return true;
                    }
                }
            }

//            if (blockRow >= 0 && blockRow < board.getROWS() && blockCol >= 0 && blockCol < board.getCOLS()) {
//                if (grid[blockRow][blockCol] != -1) {
//                    return true;
//                }
//            }
//
//            if (blockRow > board.getROWS() || blockCol < 0 || blockCol > board.getCOLS()) {
//                Gdx.app.log("CollisionChecker", "row: " + blockRow + ", col: " + blockCol + ", block: " + block);
//                Gdx.app.log("Return turn", "turrn");
//                return true;
//            }
//
//            if (blockRow >= 0 && blockRow < board.getROWS() && blockCol >= 0 && blockCol < board.getCOLS()) {
//                if (blockRow >= 0 && board.getGrid()[blockRow][blockCol] != -1) {
//                    return true;
//                }
//            }

//            // Check if out of bounds
//            if (blockRow >= board.getROWS() || blockCol < 0 || blockCol >= board.getCOLS()) {
//                return true;
//            }
//            // Check if placed on top of other pieces
//            if (board.getGrid()[blockRow][blockCol] != -1) {
//                return true;
//            }
        }
        return false;
    }
}
