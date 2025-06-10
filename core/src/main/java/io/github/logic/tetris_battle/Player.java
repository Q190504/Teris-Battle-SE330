package io.github.logic.tetris_battle;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.logic.tetris_battle.board.Board;
import io.github.logic.tetris_battle.board.Tetromino;
import io.github.logic.tetris_battle.board.TetrominoSpawner;
import io.github.logic.tetris_battle.score.HealthBar;
import io.github.logic.tetris_battle.score.ScoreManager;
import io.github.logic.tetris_battle.skill.Skill;
import io.github.logic.utils.Side;

public class Player {
    private int skillPoints;
    private String name;
    private int score;
    private Board board;

    private boolean isBeingLocked = false;

    public Player(TetrominoSpawner spawner, HealthBar healthBar, String roomId, Side side) {
        this.board = new Board(20, 10, side, spawner, healthBar, roomId);
    }
    public Player(Board board) {
        this.board = board;
    }
    public void updated() {
    //  board.update();
    }

    public boolean useSkill(Skill skill) {
        if (!skill.isActive()) {
            skill.activate();
            return true;
        }
        return false;
    }

    public Board getBoard() {
        return board;
    }

    public int getScore() {
        return board.getScoreManager().getScore();
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public String getName() {
        return name;
    }

    public ScoreManager getScoreManager() {
        return board.getScoreManager();
    }

    public void movePiece(int dir) {
        board.movePiece(dir);
    }

    public void dropPiece() {
        board.dropPiece();
    }

    public void rotatePiece() {
        board.rotatePiece();
    }

    public boolean isFullBoard() {
        return board.isFull();
    }

    public void update(float delta) {
        board.update(delta);
    }

    public void drawBoard(SpriteBatch batch, int posX, int posY) {
        board.draw(batch, posX, posY);
    }

    public Tetromino getNextTetromino() {
        return board.getNextTetromino();
    }

    public void setIsBeingLocked(boolean isBeingLocked)
    {
        this.isBeingLocked = isBeingLocked;
    }

    public boolean isBeingLocked() { return isBeingLocked; }

    public void dropCurrentPieceToBottom() {
        board.dropPieceToBottom();
    }

}

