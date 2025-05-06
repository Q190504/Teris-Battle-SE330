package io.github.tetris_battle;

import com.badlogic.gdx.Gdx;

public class ScoreManager {
    private Side side;
    private HealthBar healthBar;
    private int combo;
    private static final float BASE_SCORE = 5;
    private static final float COMBO_MULTI = 1.5f;
    private boolean doubleDamage = false;

    public ScoreManager(Side side, HealthBar healthBar) {
        this.side = side;
        this.combo = 0;
        this.healthBar = healthBar;
    }

    public void score() {
        float score = BASE_SCORE + combo * COMBO_MULTI;
        if (doubleDamage) {
            score *= 2;
        }
        Gdx.app.log("ScoreManager", "Score: " + score);
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

    public void setDoubleDamage(boolean enabled) {
        this.doubleDamage = enabled;
    }

    public boolean isDoubleDamage() {
        return doubleDamage;
    }
}
