package io.github.tetris_battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Gdx;

public class Board {
    private final int ROWS;
    private final int COLS;
    private int[][] grid;
    private boolean isFull = false;
    private ScoreManager scoreManager;
    private Tetromino currentRunningPiece = null;
    private int currentIndex = 0;
    private float spawnTimer = 0f; // Tracks time since last spawn
    private final float SPAWN_DELAY = 1.0f; // Spawn a new piece every 1 second

    public Board(int row, int col, Side side) {
        this.ROWS = row;
        this.COLS = col;
        this.grid = new int[ROWS][COLS];

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                grid[i][j] = -1; // -1 represents an empty cell
            }
        }
        this.scoreManager = new ScoreManager(side);
    }

    public boolean isFull() {
        return isFull;
    }

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
                        Gdx.app.log("PlacePiece", "Out of bounds at row: " + targetRow + ", col: " + targetCol);
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
        for (int i = 0; i < ROWS; i++) {
            boolean fullRow = true;
            for (int j = 0; j < COLS; j++) {
                if (grid[i][j] == -1) {
                    fullRow = false;
                    break;
                }
            }
            if (fullRow) {
                for (int k = i; k < ROWS - 1; k++) {
                    grid[k] = grid[k + 1].clone();
                }
                grid[ROWS - 1] = new int[COLS];
                for (int j = 0; j < COLS; j++) {
                    grid[ROWS - 1][j] = -1;
                }
                scoreManager.score();
            }
        }
        scoreManager.resetCombo();
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
        currentRunningPiece = TetrominoSpawner.getInstance().getTetromino(currentIndex);
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

    public int getROWS() {
        return ROWS;
    }

    public int getCOLS() {
        return COLS;
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

    public void draw(ShapeRenderer shapeRenderer, int posX, int posY) {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (grid[i][j] != -1) {
                    shapeRenderer.setColor(Tetromino.getColorByType(grid[i][j]));
                    shapeRenderer.rect(j * 30 + posX, i * 30 + posY, 30, 30);
                } else {
                    shapeRenderer.setColor(Color.BLACK);
                    shapeRenderer.rect(j * 30 + posX, i * 30 + posY, 30, 30);
                }
            }
        }

        if (currentRunningPiece != null) {
            int drawY = currentRunningPiece.getRow() * 30 + posY;
            Gdx.app.log("Draw", "Drawing piece at row: " + currentRunningPiece.getRow() +
                ", col: " + currentRunningPiece.getCol() + ", y: " + drawY);
            currentRunningPiece.draw(shapeRenderer, posX, posY, ROWS);
        }
    }
}
