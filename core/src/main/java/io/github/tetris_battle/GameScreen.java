package io.github.tetris_battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
    private SpriteBatch batch;

    public GameScreen(Main main, TetrominoSpawner spawner, HealthBar healthBar) {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        this.spawner = spawner;
        this.healthBar = healthBar;
        board = new Board(ROWS, COLS, Side.LEFT, spawner, healthBar, "");
        board2 = new Board(ROWS, COLS, Side.RIGHT, spawner, healthBar, "");
        this.healthBar.setWidth(COLS * SIZE * 2 + SIZE);
        Gdx.input.setInputProcessor(this);
        Tetromino.loadAssets();
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
        Gdx.gl.glClearColor((float) 120/ 255, (float) 193 / 255, (float) 194 /255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 1);
        shapeRenderer.rect(0, 30, SIZE * COLS, SIZE * ROWS);
        shapeRenderer.rect(COLS * SIZE + SIZE, 30, SIZE * COLS, SIZE * ROWS);
        shapeRenderer.end();

        batch.begin();
        board.draw(batch,0, 30);
        board2.draw(batch,COLS * SIZE + SIZE, 30);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        healthBar.draw(shapeRenderer, 0, 0);
        shapeRenderer.end();

        // Draw Next Tetrominos Preview
        Tetromino board1NextPiece = board.getNextTetromino();
        Tetromino board2NextPiece = board2.getNextTetromino();

        if (board1NextPiece != null) {
            int previewXPos = (int) ((float) (COLS * SIZE) / 2 - 1.5f * SIZE);
            int previewYPos = ROWS * SIZE + 3 * SIZE; // Above the board
            int previewWidth = SIZE * 6;
            int previewHeight = SIZE * 5;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            // Draw border box
            shapeRenderer.setColor(1, 1, 1, 1); // white border
            shapeRenderer.rect(previewXPos - (float) previewWidth / 4, previewYPos - (float) previewHeight / 4, previewWidth, previewHeight);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 1);
            shapeRenderer.rect(previewXPos - (float) previewWidth / 4, previewYPos - (float) previewHeight / 4, previewWidth, previewHeight);
            shapeRenderer.end();

            // Draw the next tetromino
            batch.begin();
            board1NextPiece.draw(batch, previewXPos, previewYPos, ROWS);
            batch.end();

            if (Gdx.app != null) {
                Gdx.app.log("GameScreen", "nextPiece (board 1): " + board1NextPiece.getType());
            }
        }

        if (board2NextPiece != null) {
            int previewXPos = (int) (COLS * SIZE * 3 / 2 - 0.5f * SIZE);
            int previewYPos = ROWS * SIZE + 3 * SIZE; // Above the board
            int previewWidth = SIZE * 6;
            int previewHeight = SIZE * 5;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            // Draw border box
            shapeRenderer.setColor(1, 1, 1, 1); // white border
            shapeRenderer.rect(previewXPos - (float) previewWidth /4, previewYPos - (float) previewHeight / 4, previewWidth, previewHeight);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 1);
            shapeRenderer.rect(previewXPos - (float) previewWidth / 4, previewYPos - (float) previewHeight / 4, previewWidth, previewHeight);
            shapeRenderer.end();

            // Draw the next tetromino
            batch.begin();
            board2NextPiece.draw(batch, previewXPos, previewYPos, ROWS);
            batch.end();

            if (Gdx.app != null) {
                Gdx.app.log("GameScreen", "nextPiece (board 2): " + board2NextPiece.getType());
            }
        }


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
            COLS * SIZE + SIZE,       // x (space between boards = SIZE)
            30,                          // y
            COLS * SIZE,                 // width
            ROWS * SIZE                  // height
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
        batch.dispose();
        shapeRenderer.dispose();
    }

    @Override
    public void HandleMessage(String msg) {

    }
}
