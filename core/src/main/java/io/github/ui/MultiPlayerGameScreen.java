package io.github.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.InputProcessor;
import com.google.gson.Gson;

import io.github.data.GameStateDTO;
import io.github.data.PlayerState;
import io.github.data.TetrominoDTO;
import io.github.tetris_battle.*;

public class MultiPlayerGameScreen implements Screen, InputProcessor, HandleMessageScreen {
    private final int ROWS = 20, COLS = 10, SIZE = 30;
    private Main main;
    private HealthBar healthBar;
    private String roomId;
    private Board board;
    private Board board2;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;

    private float gameStateTimer = 0f;
    private final float GAME_STATE_INTERVAL = 0.1f; // Send game state at most every 0.1s
    private int lastSentAck = 0;
    private int lastReceivedAck = 0;

    public MultiPlayerGameScreen(Main main, HealthBar healthBar, String roomId) {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        this.healthBar = healthBar;
        this.roomId = roomId;
        board = new Board(ROWS, COLS, Side.LEFT, null, healthBar, roomId);
        board2 = new Board(ROWS, COLS, Side.RIGHT, null, healthBar, roomId);
        this.healthBar.setWidth(COLS * SIZE * 2 + SIZE);
        Gdx.input.setInputProcessor(this);
    }

    private void sendGameState(float delta) {
        gameStateTimer += delta;

        // Only send if interval has passed and previous message acknowledged
        if (gameStateTimer >= GAME_STATE_INTERVAL && lastSentAck <= lastReceivedAck) {
            gameStateTimer = 0;

            GameStateDTO state = new GameStateDTO();
            state.roomId = this.roomId;
            state.player = new PlayerState();
            state.player.grid = board.getGrid();
            state.player.pieceIndex = board.getCurrentIndex();
            state.player.health = healthBar.getPivot();

            Tetromino currentPiece = board.getCurrentRunningPiece();
            state.player.currentPiece = (currentPiece != null) ? currentPiece.toDTO() : null;
            state.ack = lastReceivedAck + 1;

            // Update lastSentAck with the new ACK
            lastSentAck = state.ack;

            String json = new Gson().toJson(state);
            Main.client.send("game_state:" + json);
        }
    }

    private void checkEndGame() {
        if (board.isFull() || board2.isFull() || healthBar.isEndGame()) {
            Gdx.app.log("Event", "End Game");
        }
    }

    @Override
    public void render(float delta) {
        checkEndGame();
        board.update(delta);
        sendGameState(delta);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        board.draw(batch, 0, 30);
        board2.draw(batch, COLS * SIZE + SIZE, 30);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        healthBar.draw(shapeRenderer, 0, 0);
        shapeRenderer.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.LEFT) board.movePiece(-1);
        else if (keycode == Input.Keys.RIGHT) board.movePiece(1);
        else if (keycode == Input.Keys.DOWN) board.dropPiece();
        else if (keycode == Input.Keys.UP) board.rotatePiece();

        return true;
    }

    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int i, int i1, int i2, int i3) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public void resize(int width, int height) {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
    }

    @Override
    public void HandleMessage(String msg) {
        if (msg.startsWith("game_state:")) {
            String json = msg.substring("game_state:".length());
            GameStateDTO dto = new Gson().fromJson(json, GameStateDTO.class);

            if (!dto.roomId.equals(this.roomId)) return;
            if (dto.ack <= lastReceivedAck) return;
            lastReceivedAck = dto.ack;

            board2.setGrid(dto.player.grid);

            TetrominoDTO tetrominoDTO = dto.player.currentPiece;
            if (tetrominoDTO != null) {
                Tetromino piece = Tetromino.fromDTO(tetrominoDTO);
                board2.setCurrentRunningPiece(piece);
            } else {
                board2.setCurrentRunningPiece(null);
            }

            float opponentPivot = dto.player.health;
            float myPivot = 100 - opponentPivot;
            healthBar.setPivot(myPivot);

        } else if (msg.startsWith("next_piece:")) {
            String json = msg.substring("next_piece:".length());
            TetrominoDTO dto = new Gson().fromJson(json, TetrominoDTO.class);
            Tetromino piece = Tetromino.fromDTO(dto);
            board.handleSpawn(piece);
        }
    }
}
