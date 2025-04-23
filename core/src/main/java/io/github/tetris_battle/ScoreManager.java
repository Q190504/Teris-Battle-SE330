package io.github.tetris_battle;

public class ScoreManager {
    private Side side;
    private int combo;
    private static final float BASE_SCORE = 5;
    private static final float COMBO_MULTI = 1.5f;

    public ScoreManager(Side side) {
        this.side = side;
        this.combo = 0;
    }

    public void score() {
        float score = BASE_SCORE + combo * COMBO_MULTI;
        if (this.side == Side.LEFT) {
            HealthBar.getInstance().pushRight(score);
        } else {
            HealthBar.getInstance().pushLeft(score);
        }
        combo++;
    }

    public void resetCombo() {
        this.combo = 0;
    }
}
