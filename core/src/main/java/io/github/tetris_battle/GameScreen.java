package io.github.tetris_battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;
import io.github.ui.HandleMessageScreen;

public class GameScreen implements Screen, InputProcessor, HandleMessageScreen {
    private final int ROWS = 20, COLS = 10, SIZE = 30;
    private TetrominoSpawner spawner;
    private HealthBar healthBar;
    private Board board;
    private Board board2;

    private ShapeRenderer shapeRenderer;

    public GameScreen(Main main, TetrominoSpawner spawner, HealthBar healthBar) {
        shapeRenderer = new ShapeRenderer();
        this.spawner = spawner;
        this.healthBar = healthBar;
        board = new Board(ROWS, COLS, Side.LEFT, spawner, healthBar, "");
        board2 = new Board(ROWS, COLS, Side.RIGHT, spawner, healthBar, "");
        this.healthBar.setWidth(COLS * SIZE * 2 + SIZE);
        Gdx.input.setInputProcessor(this);
    }

    private void checkEndGame() {
        if (board.isFull() || board2.isFull() || healthBar.isEndGame()) {
            // Game Over logic (switch screen or show game over message)
            Gdx.app.log("Event", "End Game");
        }
    }

    @Override
    public void render(float delta) {
        checkEndGame();
        board.update(delta);
        board2.update(delta);

        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        board.draw(shapeRenderer, 0, 30);
        board2.draw(shapeRenderer, COLS * SIZE + SIZE, 30);
        healthBar.draw(shapeRenderer, 0, 0);

        shapeRenderer.end();

        // Draw Next Tetrominos Preview
        Tetromino board1NextPiece = board.getNextTetromino();
        Tetromino board2NextPiece = board.getNextTetromino();

        if (board1NextPiece != null) {
            int previewXPos = COLS * SIZE / 2 - 2 * SIZE;
            int previewYPos = ROWS * SIZE + 3 * SIZE; // Above the board
            int previewWidth = SIZE * 6;
            int previewHeight = SIZE * 5;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            // Draw border box
            shapeRenderer.setColor(1, 1, 1, 1); // white border
            shapeRenderer.rect(previewXPos - (float) previewWidth / 4, previewYPos - (float) previewHeight / 4, previewWidth, previewHeight);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            // Draw the next tetromino
            shapeRenderer.setColor(Tetromino.getColorByType(board1NextPiece.getType()));
            board1NextPiece.draw(shapeRenderer, previewXPos, previewYPos, ROWS);
            shapeRenderer.end();

            if (Gdx.app != null) {
                Gdx.app.log("GameScreen", "nextPiece (board 1): " + board1NextPiece.getType());
            }
        }

        if (board2NextPiece != null) {
            int previewXPos = COLS * SIZE * 3 / 2 - SIZE;
            int previewYPos = ROWS * SIZE + 3 * SIZE; // Above the board
            int previewWidth = SIZE * 6;
            int previewHeight = SIZE * 5;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            // Draw border box
            shapeRenderer.setColor(1, 1, 1, 1); // white border
            shapeRenderer.rect(previewXPos - (float) previewWidth /4, previewYPos - (float) previewHeight / 4, previewWidth, previewHeight);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            // Draw the next tetromino
            shapeRenderer.setColor(Tetromino.getColorByType(board2NextPiece.getType()));
            board2NextPiece.draw(shapeRenderer, previewXPos, previewYPos, ROWS);
            shapeRenderer.end();

            if (Gdx.app != null) {
                Gdx.app.log("GameScreen", "nextPiece (board 2): " + board2NextPiece.getType());
            }
        }

//        // Draw grids for both boards
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1); // gray grid
//        // Left board grid
//        for (int row = 0; row <= ROWS; row++) {
//            shapeRenderer.line(
//                0,
//                30 + row * SIZE,
//                COLS * SIZE,
//                30 + row * SIZE
//            );
//        }
//        for (int col = 0; col <= COLS; col++) {
//            shapeRenderer.line(
//                col * SIZE,
//                30,
//                col * SIZE,
//                30 + ROWS * SIZE
//            );
//        }
//
//        // Right board grid
//        float offsetX = COLS * SIZE;
//        for (int row = 0; row <= ROWS; row++) {
//            shapeRenderer.line(
//                offsetX,
//                30 + row * SIZE,
//                offsetX + COLS * SIZE,
//                30 + row * SIZE
//            );
//        }
//        for (int col = 0; col <= COLS; col++) {
//            shapeRenderer.line(
//                offsetX + col * SIZE,
//                30,
//                offsetX + col * SIZE,
//                30 + ROWS * SIZE
//            );
//        }
//
//        shapeRenderer.end();

        // Draw borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // Set border color
        shapeRenderer.setColor(1, 1, 1, 1); // white border

        // Draw left board border
        shapeRenderer.rect(
            0,                       // x
            30,                         // y
            COLS * SIZE,                // width
            ROWS * SIZE                 // height
        );

        // Draw right board border
        shapeRenderer.rect(
            COLS * SIZE + SIZE,                  // x (space between boards = SIZE)
            30,                              // y
            COLS * SIZE + SIZE * 1,          // width
            ROWS * SIZE                      // height
        );

        shapeRenderer.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.LEFT) board.movePiece(-1);
        else if (keycode == Input.Keys.RIGHT) board.movePiece(1);
        else if (keycode == Input.Keys.DOWN) board.dropPiece();
        else if (keycode == Input.Keys.UP) board.rotatePiece();

        if (keycode == Input.Keys.A) board2.movePiece(-1);
        else if (keycode == Input.Keys.D) board2.movePiece(1);
        else if (keycode == Input.Keys.S) board2.dropPiece();
        else if (keycode == Input.Keys.W) {
            Gdx.app.log("Event", "rotatePiece");
            board2.rotatePiece();
        }
        return true; // Return true to indicate event was handled
    }

    @Override
    public boolean keyUp(int keycode) { return false; }

    @Override
    public boolean keyTyped(char character) { return false; }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }

    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }

    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    @Override
    public void HandleMessage(String msg) {

    }
}
