package io.github.tetris_battle;

public class ScoreManager {
    private Side side;
    private HealthBar healthBar;
    private int combo;
    private static final float BASE_SCORE = 5;
    private static final float COMBO_MULTI = 1.5f;

    public ScoreManager(Side side, HealthBar healthBar) {
        this.side = side;
        this.combo = 0;
        this.healthBar = healthBar;
    }

    public void score() {
        float score = BASE_SCORE + combo * COMBO_MULTI;
        if (healthBar != null) {
            if (this.side == Side.LEFT) {
                healthBar.pushRight(score);
            } else {
                healthBar.pushLeft(score);
            }
        }
        combo++;
    }

    public void resetCombo() {
        this.combo = 0;
    }
}
