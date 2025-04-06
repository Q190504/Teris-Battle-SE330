package io.github.tetris_battle;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private int skillPoints;
    private String name;
    private int score;
    private Board board;
    private ScoreManager scoreManager;
    // private List<Skill> activatedSkills;

    public Player(String name, TetrominoSpawner spawner, Board board) {
        this.skillPoints = 0;
        this.name = name;
        this.score = 0;
        this.board = board;
    }

    public void updated() {
    //  board.update();
    }

    public void addSkillPoints(int points) {
        this.skillPoints += points;
    }

//    public boolean useSkill(Skill skill) {
//        if (skillPoints >= skill.getCooldown() && !skill.isActive()) {
//            skill.activate();
//            activatedSkills.add(skill);
//            skillPoints -= skill.getCooldown();
//            return true;
//        }
//        return false;
//    }
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

//    public List<Skill> getActivatedSkills() {
//        return new ArrayList<>(activatedSkills);
//    }
}

