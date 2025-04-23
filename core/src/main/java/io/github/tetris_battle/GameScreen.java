package io.github.tetris_battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.InputProcessor;

public class GameScreen implements Screen, InputProcessor, HandleMessageScreen  {
    private final int ROWS = 20, COLS = 10, SIZE = 30;
    private Board board = new Board(ROWS, COLS, Side.LEFT);
    private Board board2 = new Board(ROWS, COLS, Side.RIGHT);

    private ShapeRenderer shapeRenderer;

    public GameScreen() {
        shapeRenderer = new ShapeRenderer();
        HealthBar.getInstance().setWidth(COLS * SIZE * 2 + SIZE);
        Gdx.input.setInputProcessor(this);
    }

    private void checkEndGame() {
        if (board.isFull() || board2.isFull() || HealthBar.getInstance().isEndGame()) {
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

        // Use ShapeRenderer's begin/end in the correct order
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw boards and health bar
        board.draw(shapeRenderer, 0, 30);
        board2.draw(shapeRenderer, COLS * SIZE + SIZE, 30);
        HealthBar.getInstance().draw(shapeRenderer, 0, 0);

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
