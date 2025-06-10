package io.github.logic.tetris_battle.board;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Gdx;
import io.github.logic.tetris_battle.score.HealthBar;
import io.github.logic.tetris_battle.score.ScoreManager;
import io.github.logic.tetris_battle.helper.CollisionChecker;
import io.github.client.ui.Main;
import io.github.logic.utils.Messages;
import io.github.logic.utils.Side;

public class Board {
    private final int ROWS;
    private final int COLS;
    private int[][] grid;
    private boolean isFull = false;
    private ScoreManager scoreManager;
    private TetrominoSpawner spawner;
    private String roomId;
    private Tetromino currentRunningPiece = null;
    private int currentIndex = 0;
    private Tetromino nextRunningPiece = null;
    private int nextIndex = 0;
    private float spawnTimer = 0f; // Tracks time since last spawn
    private final float SPAWN_DELAY = 1.0f; // Spawn a new piece every 1 second

    public Board(int row, int col, Side side, TetrominoSpawner spawner, HealthBar healthBar, String roomId) {
        this.ROWS = row;
        this.COLS = col;
        this.grid = new int[ROWS][COLS];

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                grid[i][j] = -1; // -1 represents an empty cell
            }
        }
        this.spawner = spawner;
        this.scoreManager = new ScoreManager(side, healthBar);
        this.roomId = roomId;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public boolean isFull() {
        return isFull;
    }

    public void setCurrentRunningPiece(Tetromino piece) {
        this.currentRunningPiece = piece;
    }

    public void setNextRunningPiece(Tetromino piece) {
        this.nextRunningPiece = piece;
    }

    public Tetromino getCurrentRunningPiece() {
        return currentRunningPiece;
    }

    public Tetromino getNextTetromino() { return nextRunningPiece; }

    public void placePiece(Tetromino piece) {
        Array<int[]> shape = piece.getShape();
        int row = piece.getRow(), col = piece.getCol();

        for (int i = 0; i < shape.size; i++) {
            for (int j = 0; j < shape.get(i).length; j++) {
                if (shape.get(i)[j] == 1) {
                    int targetRow = row + i;
                    int targetCol = col + j;

                    if (targetRow >= 0 && targetRow < ROWS && targetCol >= 0 && targetCol < COLS) {
                        if (grid[targetRow][targetCol] != -1) {
                            grid[targetRow][targetCol] = piece.getType();
                        }
                    } else {
                        // Gdx.app.log("PlacePiece", "Out of bounds at row: " + targetRow + ", col: " + targetCol);
                    }

//                    if (targetRow < 0 || targetRow >= ROWS || targetCol < 0 || targetCol >= COLS) {
//                        Gdx.app.log("PlacePiece", "Out of bounds at row: " + targetRow + ", col: " + targetCol);
//                        return;
//                    }
//                    if (grid[targetRow][targetCol] != -1) {
//                        Gdx.app.log("PlacePiece", "Overlap detected at row: " + targetRow + ", col: " + targetCol);
//                        return;
//                    }
                }
            }
        }
        for (int i = 0; i < shape.size; i++) {
            for (int j = 0; j < shape.get(i).length; j++) {
                if (shape.get(i)[j] == 1) {
                    int targetRow = row + i;
                    int targetCol = col + j;
                    grid[targetRow][targetCol] = piece.getType();
                }
            }
        }
        clearFullRows();
        currentRunningPiece = null;
    }

    public void clearFullRows() {
        int clearedRows = 0;

        for (int i = 0; i < ROWS; i++) {
            boolean fullRow = true;
            for (int j = 0; j < COLS; j++) {
                if (grid[i][j] == -1) {
                    fullRow = false;
                    break;
                }
            }

            if (fullRow) {
                clearedRows++;
                // Shift rows down
                for (int k = i; k < ROWS - 1; k++) {
                    grid[k] = grid[k + 1].clone();
                }

                // Reset top row
                grid[ROWS - 1] = new int[COLS];
                for (int j = 0; j < COLS; j++) {
                    grid[ROWS - 1][j] = -1;
                }
                i--;
            }
        }

        // Apply score and combo based on how many were cleared
        if (clearedRows > 0) {
            for (int i = 0; i < clearedRows; i++) {
                scoreManager.score(); // Add score for each row
            }
        } else {
            scoreManager.resetCombo();
        }
    }


    public void dropPiece() {
        if (currentRunningPiece == null || isFull) {
            return;
        }
        Tetromino droppedPiece = currentRunningPiece.clonePiece();
        droppedPiece.drop();

        if (!CollisionChecker.getInstance().checkCollision(droppedPiece, this)) {
            currentRunningPiece.drop();
        } else {
            placePiece(currentRunningPiece);
        }
    }

    public void movePiece(int dir) {
        if (currentRunningPiece == null || isFull) {
            return;
        }
        Tetromino movedPiece = currentRunningPiece.clonePiece();
        movedPiece.move(dir);

        if (!CollisionChecker.getInstance().checkCollision(movedPiece, this)) {
            currentRunningPiece.move(dir);
        }
    }

    public void rotatePiece() {
        if (currentRunningPiece == null || isFull) {
            return;
        }
        Tetromino rotatedPiece = currentRunningPiece.clonePiece();
        rotatedPiece.rotate();

        if (!CollisionChecker.getInstance().checkCollision(rotatedPiece, this)) {
            currentRunningPiece.rotate();
        }
    }

    public void spawnPiece() {
        if (spawner != null) {
            Tetromino piece = spawner.getTetromino(currentIndex);
            nextRunningPiece = spawner.peekNextTetromino(currentIndex + 1);
            handleSpawn(piece);
        } else {
            Main.client.send(Messages.REQUEST_PIECE + Messages.SEPARATOR + currentIndex);
        }

    }

    public void handleSpawn(Tetromino piece) {
        currentRunningPiece = piece;
        currentIndex++;
        Gdx.app.log("SpawnPiece", "Current index: " + (ROWS - currentRunningPiece.getShape().size));

        int spawnRow = ROWS - currentRunningPiece.getShape().size;
        int spawnCol = (COLS - currentRunningPiece.getShape().get(0).length) / 2 + 1;
        currentRunningPiece.setRow(spawnRow);
        currentRunningPiece.setCol(spawnCol);

        if (CollisionChecker.getInstance().checkCollision(currentRunningPiece, this)) {
            Gdx.app.log("SpawnPiece", spawnRow + "," + spawnCol);

            isFull = true;
            currentRunningPiece = null;
        }
    }
    public int getCurrentIndex() {return currentIndex;}

    public int getROWS() {
        return ROWS;
    }

    public int getCOLS() {
        return COLS;
    }

    public void setGrid(int[][] grid) {
        this.grid = grid;
    }

    public int[][] getGrid() {
        return grid;
    }

    public void update(float delta) {
        spawnTimer += delta;

        if (spawnTimer >= SPAWN_DELAY) {
            if (currentRunningPiece == null && !isFull)
            {
                spawnPiece();
            }
            dropPiece();
            spawnTimer = 0f;
        }
    }

    public void draw(SpriteBatch batch, int posX, int posY) {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (grid[i][j] != -1) {
                    Sprite blockSprite = Tetromino.getColorByType(grid[i][j]);
                    blockSprite.setPosition(j * 30 + posX, i * 30 + posY);
                    blockSprite.draw(batch);

                } else {
                    Sprite blockSprite = Tetromino.getColorByType(grid[i][j]); //ghost
                    blockSprite.setPosition(j * 30 + posX, i * 30 + posY);
                    blockSprite.draw(batch);
                }
            }
        }

        if (currentRunningPiece != null) {
            int drawY = currentRunningPiece.getRow() * 30 + posY;
            //Gdx.app.log("Draw", "Drawing piece at row: " + currentRunningPiece.getRow() +
               // ", col: " + currentRunningPiece.getCol() + ", y: " + drawY);
            currentRunningPiece.draw(batch, posX, posY, ROWS);
        }
    }

    public void dropPieceToBottom() {
        if (currentRunningPiece == null || isFull) {
            return;
        }

        // Create a clone to test collision
        Tetromino testPiece = currentRunningPiece.clonePiece();

        // Keep dropping the test piece until collision is detected
        while (true) {
            testPiece.drop(); // Move test piece down one step
            if (CollisionChecker.getInstance().checkCollision(testPiece, this)) {
                // Collision detected, stop here
                break;
            }
            // No collision, update the actual piece position
            currentRunningPiece.drop();
        }

        // Place the piece at its final position
        placePiece(currentRunningPiece);
    }

}
