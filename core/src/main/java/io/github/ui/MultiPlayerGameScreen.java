package io.github.ui;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.google.gson.Gson;
import io.github.data.*;
import io.github.tetris_battle.*;

public class MultiPlayerGameScreen implements Screen, InputProcessor, HandleMessageScreen {
    private final int ROWS = 20, COLS = 10, SIZE = 30;
    private final int spaceBetween2Boards = SIZE * 2;
    private final int startPos = SIZE * 3;

    private Main main;
    private String roomId;
    private HealthBar healthBar;

    private Player player;
    private Board opponentBoard;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;

    private Stage stage;
    private Skin skin;

    private Label leftNextPieceLabel, rightNextPieceLabel;
    private TextButton leaveRoomBtn;

    private TextButton extraPointBtn;
    private TextButton lockOpponentBtn;
    private TextButton speedBoostBtn;

    private ExtraPointsSkill activeExtraPointSkill;
    private LockOpponentSkill activeLockOpponentSkill;

    private float gameStateTimer = 0f;
    private final float GAME_STATE_INTERVAL = 0.1f;
    private int lastSentAck = 0;
    private int lastReceivedAck = 0;

    public MultiPlayerGameScreen(Main main, HealthBar healthBar, String roomId) {
        this.main = main;
        this.roomId = roomId;
        this.healthBar = healthBar;

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        player = new Player(new Board(ROWS, COLS, Side.LEFT, null, healthBar, roomId));
        opponentBoard = new Board(ROWS, COLS, Side.RIGHT, null, healthBar, roomId);

        this.healthBar.setWidth(COLS * SIZE * 2 + spaceBetween2Boards);

        stage = new Stage();
        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);

        setupUI();
    }

    private void setupUI() {
        leftNextPieceLabel = new Label("NEXT PIECE", skin);
        rightNextPieceLabel = new Label("NEXT PIECE", skin);

        Tetromino.loadAssets();
        leaveRoomBtn = new TextButton("LEAVE ROOM", skin);
        leaveRoomBtn.setPosition(startPos + healthBar.getWidth() + SIZE, ROWS * SIZE + 100);
        leaveRoomBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new MatchScreen(main)); // Or any other screen
            }
        });
        stage.addActor(leaveRoomBtn);

        //Create button
        leaveRoomBtn = new TextButton("LEAVE ROOM", skin);
        extraPointBtn = new TextButton("X2", skin);
        lockOpponentBtn = new TextButton("Lock Opponent", skin);

        extraPointBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (extraPointBtn.isDisabled())
                    return;
                activeExtraPointSkill = new ExtraPointsSkill(player.getScoreManager(), 90);
                player.useSkill(activeExtraPointSkill);
            }
        });

        lockOpponentBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (lockOpponentBtn.isDisabled())
                    return;
                activeLockOpponentSkill = new LockOpponentSkill(10f);
                player.useSkill(activeLockOpponentSkill);
            }
        });
    }

    float lastSentHealth = 0.0f;
    private void sendGameState(float delta) {
        gameStateTimer += delta;

        float currentHealth = healthBar.getPivot();
        boolean healthChanged = currentHealth != lastSentHealth;

        if (gameStateTimer >= GAME_STATE_INTERVAL || healthChanged) {
            gameStateTimer = 0;

            Board board = player.getBoard();
            GameStateDTO state = new GameStateDTO();
            state.roomId = this.roomId;
            state.player = new PlayerState();
            state.player.grid = board.getGrid();
            state.player.pieceIndex = board.getCurrentIndex();
            if (isOwner)
                state.player.health = healthBar.getPivot();
            else {
                state.player.health = healthBar.getLastScore();
                healthBar.setLastScore(0f);
            }
            Tetromino currentPiece = board.getCurrentRunningPiece();
            state.player.currentPiece = (currentPiece != null) ? currentPiece.toDTO() : null;
            Tetromino nextPiece = board.getNextTetromino();
            state.player.nextPiece = (nextPiece != null) ? nextPiece.toDTO() : null;
            state.ack = lastReceivedAck + 1;

            // Update lastSentAck with the new ACK
            lastSentAck = state.ack;

            String json = new Gson().toJson(state);
            Main.client.send("game_state:" + json);

            // Update last sent health
            lastSentHealth = currentHealth;
        }
    }


    private void checkEndGame() {
        if (player.isFullBoard() || healthBar.isEndGame()) {
            Gdx.app.log("Event", "End Game");
            // Optionally: main.setScreen(new GameOverScreen());
        }
    }

    @Override
    public void render(float delta) {
        checkEndGame();
        player.update(delta);
        sendGameState(delta);
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
        player.drawBoard(batch, startPos, SIZE);
        opponentBoard.draw(batch,startPos + COLS * SIZE + spaceBetween2Boards, SIZE);
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
        Tetromino board1NextPiece = player.getNextTetromino();
        Tetromino board2NextPiece = opponentBoard.getNextTetromino();

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
                //Gdx.app.log("GameScreen", "nextPiece (board 2): " + board2NextPiece.getType());
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

        // Skill Btns

        // extraPointBtn
        extraPointBtn.setSize(60, 30);

        extraPointBtn.setPosition(
            leftPreviewXPos + leftPreviewWidth - SIZE,
            leftPreviewYPos + leftPreviewHeight / 2 - extraPointBtn.getHeight() / 2
        );
        stage.addActor(extraPointBtn);

        // Skill duration
        if (activeExtraPointSkill != null) {
            activeExtraPointSkill.update(delta);
            if (activeExtraPointSkill.isActive()) {
                extraPointBtn.setDisabled(true);
                int secondsLeft = (int) Math.ceil(activeExtraPointSkill.getCurrentCooldown());
                extraPointBtn.setText("X2 (" + secondsLeft + ")");
            } else if (!activeExtraPointSkill.canActivate()) {
                extraPointBtn.setDisabled(true);
                int secondsLeft = (int) Math.ceil(activeExtraPointSkill.getCurrentCooldown());
                extraPointBtn.setText("X (" + secondsLeft + ")");
            } else {
                extraPointBtn.setDisabled(false);
                extraPointBtn.setText("X2");
            }
        }


        // Lock opponent Skill Button

        lockOpponentBtn.setSize(190, 30);

        lockOpponentBtn.setPosition(
            leftPreviewXPos + leftPreviewWidth - SIZE,
            leftPreviewYPos + leftPreviewHeight / 2 - extraPointBtn.getHeight() - lockOpponentBtn.getHeight()
        );
        stage.addActor(lockOpponentBtn);

        // Skill duration
        if (activeLockOpponentSkill != null) {
            activeLockOpponentSkill.update(delta);
            if (activeLockOpponentSkill.isActive()) // Skill is activating
            {
                lockOpponentBtn.setDisabled(true);
                lockOpponentBtn.setText("Lock Opponent is active");
            }
            else if (!activeLockOpponentSkill.canActivate()) // Skill can't be activated
            {
                lockOpponentBtn.setDisabled(true);
                int secondsLeft = (int) Math.ceil(activeLockOpponentSkill.getCurrentCooldown());
                lockOpponentBtn.setText("Lock Opponent (" + secondsLeft + ")");
            }
            else // Skill can be activated
            {
                lockOpponentBtn.setDisabled(false);
                lockOpponentBtn.setText("Lock Opponent");
            }
        }
    }

    private void drawPreviewTetromino(Tetromino piece, boolean isLeft) {
        int previewX = isLeft
            ? (int) ((COLS * SIZE) / 2 - 0.5f * SIZE)
            : (int) ((COLS * SIZE * 3) / 2 + 5.5f * SIZE);
        int previewY = ROWS * SIZE + SIZE * 3;
        int width = SIZE * 6;
        int height = SIZE * 5;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 1);
        shapeRenderer.rect(previewX - width / 4f, previewY - height / 4f, width, height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(previewX - width / 4f, previewY - height / 4f, width + 0.5f, height + 0.5f);
        shapeRenderer.end();

        batch.begin();
        if (piece != null) piece.draw(batch, previewX, previewY, ROWS);
        batch.end();

        Label label = isLeft ? leftNextPieceLabel : rightNextPieceLabel;
        label.setPosition(previewX, previewY + height - SIZE);
        if (!label.hasParent()) stage.addActor(label);
    }

    @Override
    public void HandleMessage(String msg) {
        String[] parts = msg.split(Messages.SEPARATOR);
        if (parts[0].equals(Messages.GAME_STATE)) {
            String json = msg.substring(Messages.GAME_STATE.length() + Messages.SEPARATOR.length());
            GameStateDTO dto = new Gson().fromJson(json, GameStateDTO.class);
            if (!dto.roomId.equals(this.roomId) || dto.ack <= lastReceivedAck) return;
            lastReceivedAck = dto.ack;

            opponentBoard.setGrid(dto.player.grid);
            TetrominoDTO tetrominoDTO = dto.player.currentPiece;
            opponentBoard.setCurrentRunningPiece(tetrominoDTO != null ? Tetromino.fromDTO(tetrominoDTO) : null);

            tetrominoDTO = dto.player.nextPiece;
            opponentBoard.setNextRunningPiece(tetrominoDTO != null ? Tetromino.fromDTO(tetrominoDTO) : null);

            if (isOwner) {
                float opponentDamage = dto.player.health;
                healthBar.setPivot(healthBar.getPivot() - opponentDamage);
            } else {
                if (healthBar.getLastScore() != 0f) return;
                float opponentPivot = dto.player.health;
                healthBar.setPivot(100 - opponentPivot);
            }

        } else if (parts[0].equals(Messages.PLAYER_LEFT)) {
            Main.client.send(Messages.LEAVE);
            showPopup("Opponent has left the game!", "LEAVE", () -> main.setScreen(new MatchScreen(main)));

        } else if (parts[0].equals(Messages.PIECE)) {
            String json = msg.substring(Messages.PIECE.length() + Messages.SEPARATOR.length());
            Tetromino piece = Tetromino.fromDTO(new Gson().fromJson(json, TetrominoDTO.class));
            player.getBoard().handleSpawn(piece);

        } else if (parts[0].equals(Messages.NEXT_PIECE)) {
            String json = msg.substring(Messages.NEXT_PIECE.length() + Messages.SEPARATOR.length());
            Tetromino piece = Tetromino.fromDTO(new Gson().fromJson(json, TetrominoDTO.class));
            player.getBoard().setNextRunningPiece(piece);
        } else if (msg.startsWith("lock_player")) {
            player.setIsBeingLocked(true);
        } else if (msg.startsWith("unlock_player")) {
            player.setIsBeingLocked(false);
        }
    }

    @Override public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.LEFT && !player.isBeingLocked()) player.movePiece(-1);
        else if (keycode == Input.Keys.RIGHT && !player.isBeingLocked()) player.movePiece(1);
        else if (keycode == Input.Keys.DOWN) player.dropPiece();
        else if (keycode == Input.Keys.UP) player.rotatePiece();
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
    @Override public void show() {}
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
