package io.github.tetris_battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Array;
import io.github.ui.HandleMessageScreen;

public class GameScreen implements Screen, InputProcessor, HandleMessageScreen {
    private final int ROWS = 20, COLS = 10, SIZE = 30;
    private TetrominoSpawner spawner;
    private HealthBar healthBar;
    private Board board;
    private Board board2;

    private final int startPos = SIZE * 3;
    private final int spaceBetween2Boards = SIZE * 2;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;

    private Skin skin;
    private Stage stage;

    private Label leftNextPieceLabel;
    private Label rightNextPieceLabel;

    private TextButton leaveRoomBtn;

    public GameScreen(Main main, TetrominoSpawner spawner, HealthBar healthBar) {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        this.spawner = spawner;
        this.healthBar = healthBar;
        board = new Board(ROWS, COLS, Side.LEFT, spawner, healthBar, "");
        board2 = new Board(ROWS, COLS, Side.RIGHT, spawner, healthBar, "");
        this.healthBar.setWidth(COLS * SIZE * 2 + spaceBetween2Boards);
        Gdx.input.setInputProcessor(this);
        Tetromino.loadAssets();

        stage = new Stage();
        skin = new Skin(Gdx.files.internal("assets\\uiskin.json"));

        // Create Labels
        leftNextPieceLabel = new Label("NEXT PIECE", skin);
        rightNextPieceLabel = new Label("NEXT PIECE", skin);

        //Create button
        leaveRoomBtn = new TextButton("LEAVE ROOM", skin);
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

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        //Draw background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 1);
        shapeRenderer.rect(startPos, SIZE, SIZE * COLS, SIZE * ROWS);
        shapeRenderer.rect(startPos + COLS * SIZE + spaceBetween2Boards, SIZE, SIZE * COLS, SIZE * ROWS);
        shapeRenderer.end();

        //Draw boards
        batch.begin();
        board.draw(batch, startPos, SIZE);
        board2.draw(batch,startPos + COLS * SIZE + spaceBetween2Boards, SIZE);
        batch.end();

        // Draw borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // Set border color
        shapeRenderer.setColor(1, 1, 1, 1); // white border

        // Draw left board border
        shapeRenderer.rect(
            startPos,                       // x
            SIZE,                       // y
            COLS * SIZE,                // width
            ROWS * SIZE                 // height
        );

        // Draw right board border
        shapeRenderer.rect(
            startPos + COLS * SIZE + spaceBetween2Boards,   // x (space between boards = SIZE)
            SIZE,                        // y
            COLS * SIZE,                 // width
            ROWS * SIZE                  // height
        );

        shapeRenderer.end();


        // Draw Left Preview Tetromino
        Tetromino board1NextPiece = board.getNextTetromino();
        Tetromino board2NextPiece = board2.getNextTetromino();

        int leftPreviewXPos = (int) ((float) (COLS * SIZE) / 2 - 0.5f * SIZE);
        int leftPreviewYPos = ROWS * SIZE + 3 * SIZE; // Above the board
        int leftPreviewWidth = SIZE * 6;
        int leftPreviewHeight = SIZE * 5;

        //Left Next Piece label
        leftNextPieceLabel.setPosition(leftPreviewXPos, leftPreviewYPos + leftPreviewHeight - SIZE);
        stage.addActor(leftNextPieceLabel);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // Draw right preview's border
        shapeRenderer.setColor(1, 1, 1, 1); // white border
        shapeRenderer.rect(leftPreviewXPos - (float) leftPreviewWidth / 4, leftPreviewYPos - (float) leftPreviewHeight / 4, leftPreviewWidth + 0.5f, leftPreviewHeight + 0.5f);
        shapeRenderer.end();

        //Draw left preview's background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 1);
        shapeRenderer.rect(leftPreviewXPos - (float) leftPreviewWidth / 4, leftPreviewYPos - (float) leftPreviewHeight / 4, leftPreviewWidth, leftPreviewHeight);
        shapeRenderer.end();

        if (board1NextPiece != null) {
            // Draw the next tetromino
            batch.begin();
            board1NextPiece.draw(batch, leftPreviewXPos, leftPreviewYPos, ROWS);
            batch.end();

            if (Gdx.app != null) {
                Gdx.app.log("GameScreen", "nextPiece (board 1): " + board1NextPiece.getType());
            }
        }

        // Draw Right Preview Tetromino
        int rightPreviewXPos = (int) ((float) (COLS * SIZE * 3) / 2 + 5.5f * SIZE);
        int rightPreviewYPos = ROWS * SIZE + 3 * SIZE; // Above the board
        int rightPreviewWidth = SIZE * 6;
        int rightPreviewHeight = SIZE * 5;

        // Right next piece label
        rightNextPieceLabel.setPosition(rightPreviewXPos, leftPreviewYPos + leftPreviewHeight - SIZE);
        stage.addActor(rightNextPieceLabel);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // Draw right preview's  border
        shapeRenderer.setColor(1, 1, 1, 1); // white border
        shapeRenderer.rect(rightPreviewXPos - (float) rightPreviewWidth /4, rightPreviewYPos - (float) rightPreviewHeight / 4, rightPreviewWidth + 0.5f, rightPreviewHeight + 0.5f);
        shapeRenderer.end();

        //Draw right preview's background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 1);
        shapeRenderer.rect(rightPreviewXPos - (float) rightPreviewWidth / 4, rightPreviewYPos - (float) rightPreviewHeight / 4, rightPreviewWidth, rightPreviewHeight);
        shapeRenderer.end();

        if (board2NextPiece != null) {
            // Draw the next tetromino
            batch.begin();
            board2NextPiece.draw(batch, rightPreviewXPos, rightPreviewYPos, ROWS);
            batch.end();

            if (Gdx.app != null) {
                Gdx.app.log("GameScreen", "nextPiece (board 2): " + board2NextPiece.getType());
            }
        }

        //Draw health bar
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        int maxHeight = Math.max(leftPreviewYPos + leftPreviewHeight + SIZE,
            rightPreviewYPos + rightPreviewHeight + SIZE);
        healthBar.draw(shapeRenderer, startPos, maxHeight);
        shapeRenderer.end();

        //Draw leave room button
        leaveRoomBtn.setPosition( startPos + healthBar.getWidth() + 0.5f * SIZE, maxHeight);
        stage.addActor(leaveRoomBtn);
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
