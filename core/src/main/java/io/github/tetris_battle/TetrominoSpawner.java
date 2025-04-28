package io.github.tetris_battle;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Gdx;
import io.github.data.TetrominoDTO;

public class TetrominoSpawner {
    private Array<Integer> bagQueue = new Array<>();
    private Array<Integer> boardBucket;

    public void setBoardBucket(Array<Integer> boardBucket) {
        this.boardBucket = boardBucket;
    }

    private void shuffleBag() {
        Array<Integer> tetrominoTypes = new Array<>();
        for (int i = 0; i < 7; i++) {
            tetrominoTypes.add(i);
        }
        tetrominoTypes.shuffle();
        bagQueue.addAll(tetrominoTypes);
    }

    public Tetromino getTetromino(int index) {
        if (index >= bagQueue.size) {
            shuffleBag();
            if (Gdx.app != null) {
                Gdx.app.log("TetrominoSpawner", "Shuffling new bag index...");
            }
        }

        int minIndex = getMinIndexFromBoardBucket();

        if (minIndex >= 0 && minIndex < bagQueue.size)
        {
            bagQueue.removeRange(0, minIndex);
            if (Gdx.app != null)
            {
                Gdx.app.log("TetrominoSpawner", "Removed elements from bagQueue up to index " + minIndex);
            }
        }
        if (Gdx.app != null)
        {
            Gdx.app.log("TetrominoSpawner", "Spawned new piece at index " + index);
        }
        return new Tetromino(bagQueue.get(index));
    }

    public Tetromino peekNextTetromino(int index) {
        if (index >= bagQueue.size) {
            shuffleBag();
            if (Gdx.app != null) {
                Gdx.app.log("TetrominoSpawner", "Shuffling new bag index...");
            }
        }

        if (!bagQueue.isEmpty()) {
            return new Tetromino(bagQueue.get(index)); // Peek at the NEXT piece
        } else {
            // If there's no piece left, shuffle and peek at the first
            shuffleBag();
            return new Tetromino(bagQueue.get(0));
        }
    }


    // Helper function to get the minimum index from boardBucket
    private int getMinIndexFromBoardBucket() {
        if (boardBucket == null || boardBucket.isEmpty()) {
            return -1;  // Return -1 if the boardBucket is empty or null
        }

         // Get the minimum value in boardBucket
        int minValue = boardBucket.get(0);
        for (int i = 1; i < boardBucket.size; i++) {
            if (boardBucket.get(i) < minValue) {
                minValue = boardBucket.get(i);
            }
        }
        return minValue;
    }
}
