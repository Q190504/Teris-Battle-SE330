package io.github.client.ui;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.TimeUtils;
import io.github.logic.tetris_battle.Player;
import io.github.logic.tetris_battle.board.Board;
import io.github.logic.tetris_battle.board.Tetromino;
import io.github.logic.tetris_battle.board.TetrominoSpawner;
import io.github.logic.utils.*;

public class SinglePlayerGameScreen implements Screen, InputProcessor {

    private final int ROWS = 25, COLS = 10, SIZE = 30;
    private int startPos;

    private Main main;
    private Player player;

    private long startTime;
    private long endTime;
    private long lastAutoDropTime;
    private float durationSeconds;

    private int score;
    private float dropInterval = 1000; // milliseconds

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;

    private Stage stage;
    private Skin skin;

    private Label nextPieceLabel;
    private Label scoreLabel;
    private TextButton leaveRoomBtn;

    public SinglePlayerGameScreen(Main main, TetrominoSpawner spawner) {
        this.main = main;

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        player = new Player(new Board(ROWS, COLS, Side.LEFT, spawner, null, null));

        stage = new Stage();
        skin = UIFactory.getSkin();

        int screenWidth = Gdx.graphics.getWidth();
        startPos = (screenWidth - (COLS * SIZE)) / 2;

        InputMultiplexer multiplexer = new InputMultiplexer(stage, this);
        Gdx.input.setInputProcessor(multiplexer);

        setupUI();
    }

    private void setupUI() {
        Tetromino.loadAssets();

        nextPieceLabel = UIFactory.createLabel("NEXT PIECE");

        scoreLabel = UIFactory.createTitle("Score: 0");
        scoreLabel.setPosition(startPos, SIZE * ROWS + SIZE * 1.5f);

        stage.addActor(scoreLabel);

        leaveRoomBtn = UIFactory.createTextButton("LEAVE", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().stopMusic();
                AudioManager.getInstance().playMusic("menu_bg", true);
                main.setScreen(new MatchScreen(main));
            }
        });
        stage.addActor(leaveRoomBtn);
    }

    private void checkEndGame() {
        if (player.isFullBoard()) {
            endTime = TimeUtils.millis();
            durationSeconds = (endTime - startTime) / 1000.0f;
            main.setScreen(new EndGameScreen(main, false, durationSeconds, player.getScore()));
        }
    }

    @Override
    public void render(float delta) {
        if (stage == null) return;

        updateAutoDrop();
        checkEndGame();
        player.update(delta);

        clearScreen();
        stage.act(Gdx.graphics.getDeltaTime());

        drawBoard();
        drawNextPiecePreview();
        updateScoreDisplay();
        stage.draw();
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(AppColors.BACKGROUND.r, AppColors.BACKGROUND.g, AppColors.BACKGROUND.b, AppColors.BACKGROUND.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void drawBoard() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(startPos, SIZE, SIZE * COLS, SIZE * ROWS);
        shapeRenderer.end();

        batch.begin();
        player.drawBoard(batch, startPos, SIZE);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(startPos, SIZE, SIZE * COLS, SIZE * ROWS);
        shapeRenderer.end();
    }

    private void drawNextPiecePreview() {
        int previewXPos = (int) ((COLS * SIZE) / 2 - 0.5f * SIZE);
        int previewYPos = ROWS * SIZE - 3 * SIZE;
        int previewWidth = SIZE * 6;
        int previewHeight = SIZE * 5;

        nextPieceLabel.setPosition(previewXPos, previewYPos + previewHeight - SIZE);
        stage.addActor(nextPieceLabel);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(previewXPos - previewWidth / 4f, previewYPos - previewHeight / 4f, previewWidth, previewHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(previewXPos - previewWidth / 4f, previewYPos - previewHeight / 4f, previewWidth, previewHeight);
        shapeRenderer.end();

        Tetromino nextPiece = player.getNextTetromino();
        if (nextPiece != null) {
            batch.begin();
            nextPiece.draw(batch, previewXPos, previewYPos, ROWS);
            batch.end();
        }
    }

    private void updateScoreDisplay() {
        score = player.getScore();
        scoreLabel.setText("Score: " + score);

        leaveRoomBtn.setPosition(startPos + COLS * SIZE + 20, SIZE * ROWS + SIZE * 1.5f);
        leaveRoomBtn.setColor(Color.RED);
    }

    private void updateAutoDrop() {
        long now = TimeUtils.millis();
        long elapsed = now - lastAutoDropTime;

        int score = player.getScore(); // Assumes player exposes score

        // Speed curve based on score
        float minInterval = 100f;
        float maxInterval = 1000f;
        float scoreFactor = Math.min(score / 1000f, 1f); // Clamp to max 1.0
        float decayRate = 2.0f; // Adjust to control speed curve

        // Exponential decay based on score
        dropInterval = minInterval + (maxInterval - minInterval) * (float) Math.exp(-decayRate * scoreFactor);

        if (elapsed >= dropInterval) {
            player.dropPiece(); // Auto-drop
            lastAutoDropTime = now;
        }
    }


    @Override public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.LEFT && !player.isBeingLocked()) player.movePiece(-1);
        else if (keycode == Input.Keys.RIGHT && !player.isBeingLocked()) player.movePiece(1);
        else if (keycode == Input.Keys.DOWN) player.dropPiece();
        else if (keycode == Input.Keys.UP) player.rotatePiece();
        else if (keycode == Input.Keys.SPACE) player.dropCurrentPieceToBottom();
        return true;
    }

    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }

    @Override public void resize(int width, int height) {}

    @Override public void show() {
        startTime = TimeUtils.millis();
        lastAutoDropTime = startTime;

        AudioManager.getInstance().stopMusic();
        AudioManager.getInstance().playMusic("game_bg", true);
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        stage.dispose();
        skin.dispose();
    }
}
