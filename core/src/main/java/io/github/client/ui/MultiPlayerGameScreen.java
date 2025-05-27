package io.github.client.ui;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.google.gson.Gson;
import io.github.logic.data.GameStateDTO;
import io.github.logic.data.PlayerState;
import io.github.logic.data.TetrominoDTO;
import io.github.logic.tetris_battle.Player;
import io.github.logic.utils.*;
import io.github.logic.utils.AudioManager.AudioCategory;
import io.github.logic.tetris_battle.board.Board;
import io.github.logic.tetris_battle.board.Tetromino;
import io.github.logic.tetris_battle.score.HealthBar;
import io.github.logic.tetris_battle.skill.ExtraPointsSkill;
import io.github.logic.tetris_battle.skill.LockOpponentSkill;
import io.github.logic.tetris_battle.skill.Skill;
import io.github.logic.tetris_battle.skill.SpeedBoostSkill;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MultiPlayerGameScreen implements Screen, InputProcessor, HandleMessageScreen {
    private final int ROWS = 20, COLS = 10, SIZE = 30;
    private final int spaceBetween2Boards = SIZE * 2;
    private final int startPos = SIZE * 3;

    private Main main;
    private String roomId;
    private HealthBar healthBar;
    private boolean isOwner;

    private Player player;
    private Board opponentBoard;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;

    private Stage stage;
    private Skin skin;

    private Label leftNextPieceLabel, rightNextPieceLabel;
    private TextButton leaveRoomBtn;

    private Map<TextButton, Skill> skills;
    private Map<String, Skill> skillNames;

    Dialog dialog;
    private float gameStateTimer = 0f;
    private final float GAME_STATE_INTERVAL = 0.1f;
    private int lastReceivedAck = 0;

    public MultiPlayerGameScreen(Main main, HealthBar healthBar, String roomId, boolean isOwner, Set<String> selectedSkills) {
        this.main = main;
        this.roomId = roomId;
        this.healthBar = healthBar;
        this.isOwner = isOwner;

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        player = new Player(new Board(ROWS, COLS, Side.LEFT, null, healthBar, roomId));
        opponentBoard = new Board(ROWS, COLS, Side.RIGHT, null, healthBar, roomId);

        this.healthBar.setWidth(COLS * SIZE * 2 + spaceBetween2Boards);

        stage = new Stage();
        skin = UIFactory.getSkin();

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);

        prepareSkills(selectedSkills);
        setupUI();
    }

    private void prepareSkills(Set<String> selectedSkills) {
        Map<TextButton, Skill> map = new HashMap<>();
        Map<String, Skill> mapName = new HashMap<>();
        for (String selectedSkill : selectedSkills) {
            if (selectedSkill.equals(ExtraPointsSkill.getStaticName())) {
                Skill extraPointSkill = new ExtraPointsSkill(player.getScoreManager(), SkillConfigs.EXTRA_POINT_CD);

                // Declare button reference holder as final array to allow modification inside lambda
                final TextButton[] extraPointBtn = new TextButton[1];
                extraPointBtn[0] = UIFactory.createTextButton(extraPointSkill.getName(), new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (extraPointBtn[0].isDisabled()) return;
                        player.useSkill(extraPointSkill);
                    }
                });

                map.put(extraPointBtn[0], extraPointSkill);
                mapName.put(extraPointSkill.getName(), extraPointSkill);
            } else if (selectedSkill.equals(LockOpponentSkill.getStaticName())) {
                Skill lockOpponentSkill = new LockOpponentSkill(SkillConfigs.LOCK_OPPONENT_CD);

                final TextButton[] lockOpponentBtn = new TextButton[1];
                lockOpponentBtn[0] = UIFactory.createTextButton(lockOpponentSkill.getName(), new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (lockOpponentBtn[0].isDisabled()) return;
                        player.useSkill(lockOpponentSkill);
                    }
                });

                map.put(lockOpponentBtn[0], lockOpponentSkill);
                mapName.put(lockOpponentSkill.getName(), lockOpponentSkill);
            } else if (selectedSkill.equals(SpeedBoostSkill.getStaticName())) {
                Skill speedBoostSkill = new SpeedBoostSkill(player, SkillConfigs.SPEED_BOOST_CD);

                final TextButton[] speedBoostBtn = new TextButton[1];
                speedBoostBtn[0] = UIFactory.createTextButton(speedBoostSkill.getName(), new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (speedBoostBtn[0].isDisabled()) return;
                        player.useSkill(speedBoostSkill);
                    }
                });

                map.put(speedBoostBtn[0], speedBoostSkill);
                mapName.put(speedBoostSkill.getName(), speedBoostSkill);
            }
        }

        this.skills = map; // Store for use in update loop or elsewhere
        this.skillNames = mapName;
    }


    private void setupUI() {
        leftNextPieceLabel = UIFactory.createLabel("NEXT PIECE");
        rightNextPieceLabel = UIFactory.createLabel("NEXT PIECE");

        Tetromino.loadAssets();

        leaveRoomBtn = UIFactory.createTextButton("LEAVE", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Main.client.send(Messages.LEAVE);

                //Leave room and go back to match screen play menu background music
                AudioManager.getInstance().stopMusic();
                AudioManager.getInstance().playMusic("menu_bg", true);

                main.setScreen(new MatchScreen(main));
            }
        });
        stage.addActor(leaveRoomBtn);

        for (Map.Entry<TextButton, Skill> entry : skills.entrySet())
        {
            stage.addActor(entry.getKey());
        }
    }

    private void updateSkills(float delta) {
        for (Map.Entry<TextButton, Skill> entry : skills.entrySet()) {
            TextButton button = entry.getKey();
            Skill skill = entry.getValue();

            skill.update(delta);

            int secondsLeft = (int) Math.ceil(skill.getCurrentCooldown());

            if (skill.isActive()) {
                button.setDisabled(true);
                button.setColor(AppColors.BUTTON_BG_MAGENTA);
                button.setText(skill.getInstruction() + " (" + (int)skill.getRemainingActiveTime() + ")");
            } else {
                boolean canUse = skill.canActivate();
                button.setDisabled(!canUse);
                if (canUse)
                    button.setColor(AppColors.BUTTON_BG_CYAN);
                else
                    button.setColor(AppColors.PANEL_BG_LIGHT);
                button.setText(canUse
                    ? skill.getName()
                    : skill.getName() + " (" + secondsLeft + ")");
            }
        }
    }

    private void sendGameState(float delta) {
        gameStateTimer += delta;

        if (gameStateTimer >= GAME_STATE_INTERVAL) {
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

            String json = new Gson().toJson(state);
            Main.client.send(Messages.GAME_STATE + Messages.SEPARATOR + json);
        }
    }


    private void checkEndGame() {
        if (player.isFullBoard() || healthBar.isEndGame()) {
            Gdx.app.log("Event", "End Game");
            //Left side: player
            //Main.client.sent("end_game:win")
            // Optionally: main.setScreen(new GameOverScreen(Main main, bool win/lose, long totaltime));
        }
    }

    @Override
    public void render(float delta) {
        if (stage == null) return;

        updateSkills(delta);
        checkEndGame();
        player.update(delta);
        sendGameState(delta);

        clearScreen();
        stage.act(Gdx.graphics.getDeltaTime());

        drawBoards();
        drawNextPiecePreviews();
        drawHealthBarAndUI(delta);
        stage.draw();
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(AppColors.BACKGROUND.r, AppColors.BACKGROUND.g, AppColors.BACKGROUND.b, AppColors.BACKGROUND.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void drawBoards() {
        // Draw black background areas for boards
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 1);
        shapeRenderer.rect(startPos, SIZE, SIZE * COLS, SIZE * ROWS);
        shapeRenderer.rect(startPos + COLS * SIZE + spaceBetween2Boards, SIZE, SIZE * COLS, SIZE * ROWS);
        shapeRenderer.end();

        // Draw game boards
        batch.begin();
        player.drawBoard(batch, startPos, SIZE);
        opponentBoard.draw(batch, startPos + COLS * SIZE + spaceBetween2Boards, SIZE);
        batch.end();

        // Draw white borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(startPos, SIZE, COLS * SIZE, ROWS * SIZE);
        shapeRenderer.rect(startPos + COLS * SIZE + spaceBetween2Boards, SIZE, COLS * SIZE, ROWS * SIZE);
        shapeRenderer.end();
    }

    private void drawNextPiecePreviews() {
        drawPreview(player.getNextTetromino(), leftNextPieceLabel, true);
        drawPreview(opponentBoard.getNextTetromino(), rightNextPieceLabel, false);
    }

    private void drawPreview(Tetromino piece, Label label, boolean isLeft) {
        int previewXPos = isLeft ?
            (int) ((COLS * SIZE) / 2 - 0.5f * SIZE) :
            (int) ((COLS * SIZE * 3) / 2 + 5.5f * SIZE);
        int previewYPos = ROWS * SIZE + 3 * SIZE;
        int previewWidth = SIZE * 6;
        int previewHeight = SIZE * 5;

        label.setPosition(previewXPos, previewYPos + previewHeight - SIZE);
        stage.addActor(label);

        // Draw border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(previewXPos - previewWidth / 4f, previewYPos - previewHeight / 4f, previewWidth + 0.5f, previewHeight + 0.5f);
        shapeRenderer.end();

        // Draw background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 1);
        shapeRenderer.rect(previewXPos - previewWidth / 4f, previewYPos - previewHeight / 4f, previewWidth, previewHeight);
        shapeRenderer.end();

        if (piece != null) {
            batch.begin();
            piece.draw(batch, previewXPos, previewYPos, ROWS);
            batch.end();
        }

        // Draw skill buttons if available
        if (isLeft) {
            if (skills != null && !skills.isEmpty()) {
                float buttonX = (int) previewXPos + previewWidth - SIZE;
                float buttonY = previewYPos + previewHeight / 2;
                float buttonSpacing = 20;

                int index = 0;
                for (TextButton btn : skills.keySet()) {
                    btn.setSize(270, 40);
                    btn.setPosition(buttonX, buttonY - index * (btn.getHeight() + buttonSpacing));
                    if (btn.getStage() == null) {
                        stage.addActor(btn); // avoid re-adding
                    }
                    index++;
                }
            }
        }
    }

    private void drawHealthBarAndUI(float delta) {
        int maxHeight = Math.max(
            ROWS * SIZE + 3 * SIZE + SIZE * 5 + SIZE,
            ROWS * SIZE + 3 * SIZE + SIZE * 5 + SIZE
        );

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        healthBar.draw(shapeRenderer, startPos, maxHeight);
        shapeRenderer.end();

        leaveRoomBtn.setPosition(startPos + healthBar.getWidth() + 0.25f * SIZE, maxHeight - (float) SIZE /2);
        leaveRoomBtn.setColor(Color.RED);
        stage.addActor(leaveRoomBtn);
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

        } else if (parts[0].equals(Messages.LOCK_PLAYER)) {
            player.setIsBeingLocked(true);

        } else if (parts[0].equals(Messages.UNLOCK_PLAYER)) {
            player.setIsBeingLocked(false);
        } else if (parts[0].equals(Messages.NO_CONN)) {
            showPopup("Disconnected from the server.", new Runnable() {
                @Override
                public void run() {
                    main.setScreen(new MatchScreen(main));
                }
            });
        }
    }



    private void showPopup(String message) {
        showPopup(message, "OK", null);
    }

    private void showPopup(String message, String action) {
        showPopup(message, action, null);
    }

    private void showPopup(String message, Runnable onOk) {
        showPopup(message, "OK", onOk);
    }

    private void showPopup(String message, String action, Runnable onOk) {
        if (dialog != null)
            dialog.hide();
        dialog = UIFactory.createDialog("Notice", message, action, onOk);
        dialog.show(stage);
    }

    @Override public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.LEFT && !player.isBeingLocked()) player.movePiece(-1);
        else if (keycode == Input.Keys.RIGHT && !player.isBeingLocked()) player.movePiece(1);
        else if (keycode == Input.Keys.DOWN) player.dropPiece();
        else if (keycode == Input.Keys.UP) player.rotatePiece();
        else if (keycode == Input.Keys.SPACE) {
            Skill speedBoostSkill = skillNames.get(SpeedBoostSkill.getStaticName());
            if (speedBoostSkill != null)
            {
                ((SpeedBoostSkill) speedBoostSkill).performSpeedDrop();
            }
        }
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
        //Background music
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
