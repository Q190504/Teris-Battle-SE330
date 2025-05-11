package io.github.tetris_battle;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.ui.HandleMessageScreen;

public class GameScreen implements Screen, InputProcessor, HandleMessageScreen {
    private final int ROWS = 20, COLS = 10, SIZE = 30;
    private TetrominoSpawner spawner;
    private HealthBar healthBar;
    private Player player1;
    private Player player2;

    private final int startPos = SIZE * 3;
    private final int spaceBetween2Boards = SIZE * 2;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;

    private Skin skin;
    private Stage stage;

    private Label leftNextPieceLabel;
    private Label rightNextPieceLabel;

    private TextButton leaveRoomBtn;

    private TextButton extraPointBtn;
    private TextButton lockOpponentBtn;
    private TextButton speedBoostBtn;

    private ExtraPointsSkill activeExtraPointSkill;
    private LockOpponentSkill activeLockOpponentSkill;

    private Label countdownLabel;

    public GameScreen(Main main, TetrominoSpawner spawner, HealthBar healthBar) {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        this.spawner = spawner;
        this.healthBar = healthBar;
        player1 = new Player(spawner, healthBar, "", Side.LEFT);
        player2 = new Player(spawner, healthBar, "", Side.RIGHT);
        this.healthBar.setWidth(COLS * SIZE * 2 + spaceBetween2Boards);

        Tetromino.loadAssets();

        stage = new Stage();
        skin = new Skin(Gdx.files.internal("assets\\uiskin.json"));

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);

        // Create Labels
        leftNextPieceLabel = new Label("NEXT PIECE", skin);
        rightNextPieceLabel = new Label("NEXT PIECE", skin);

        //Create button
        leaveRoomBtn = new TextButton("LEAVE ROOM", skin);
        extraPointBtn = new TextButton("X2", skin);
        lockOpponentBtn = new TextButton("Lock Opponent", skin);

        extraPointBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (extraPointBtn.isDisabled())
                    return;
                activeExtraPointSkill = new ExtraPointsSkill(player1.getScoreManager(), 90);
                player1.useSkill(activeExtraPointSkill);
            }
        });

        lockOpponentBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (lockOpponentBtn.isDisabled())
                    return;
                activeLockOpponentSkill = new LockOpponentSkill(player2, 10f);
                player1.useSkill(activeLockOpponentSkill);
            }
        });
    }

    private void checkEndGame() {
        if (player1.isFullBoard() || player2.isFullBoard() || healthBar.isEndGame()) {
            // Game Over logic (switch screen or show game over message)
            //Gdx.app.log("Event", "End Game");
        }
    }

    @Override
    public void render(float delta) {
        checkEndGame();
        player1.update(delta);
        player2.update(delta);

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
        player1.drawBoard(batch, startPos, SIZE);
        player2.drawBoard(batch,startPos + COLS * SIZE + spaceBetween2Boards, SIZE);
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
        Tetromino board1NextPiece = player1.getNextTetromino();
        Tetromino board2NextPiece = player2.getNextTetromino();

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

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.A  && player1.isBeingLocked()) player1.movePiece(-1);
        else if (keycode == Input.Keys.D && player1.isBeingLocked()) player1.movePiece(1);
        else if (keycode == Input.Keys.S) player1.dropPiece();
        else if (keycode == Input.Keys.W) player1.rotatePiece();

        if (keycode == Input.Keys.LEFT && !player2.isBeingLocked()) player2.movePiece(-1);
        else if (keycode == Input.Keys.RIGHT && !player2.isBeingLocked()) player2.movePiece(1);
        else if (keycode == Input.Keys.DOWN) player2.dropPiece();
        else if (keycode == Input.Keys.UP) player2.rotatePiece();
        
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
        stage.dispose();
        skin.dispose();
    }

    @Override
    public void HandleMessage(String msg) {

    }
}
