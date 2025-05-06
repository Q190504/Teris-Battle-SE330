package io.github.tetris_battle;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private int skillPoints;
    private String name;
    private int score;
    private Board board;
    private ScoreManager scoreManager;
    private TetrominoSpawner spawner;
    private HealthBar healthBar;
    // private List<Skill> activatedSkills;

    public Player(TetrominoSpawner spawner, HealthBar healthBar, String roomId, Side side) {
        this.spawner = spawner;
        this.healthBar = healthBar;
        this.board = new Board(20, 10, side, spawner, healthBar, roomId);
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
        return score;
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
        return scoreManager;
    }
}

