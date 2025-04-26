package io.github.tetris_battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class HealthBar {
    private float pivot = 50f; // 50% is neutral
    private int width = 300;   // Default width

    public void setWidth(int width) {
        this.width = width;
    }

    public float getPivot() {
        return pivot;
    }

    public boolean isEndGame() {
        return pivot <= 0 || pivot >= 100;
    }

    public void pushLeft(float percent) {
        pivot -= percent;
        clampPivot();
    }

    public void pushRight(float percent) {
        pivot += percent;
        clampPivot();
    }

    public void setPivot(float pivot) {
        this.pivot = pivot;
        clampPivot();
    }

    private void clampPivot() {
        if (pivot < 0) pivot = 0;
        if (pivot > 100) pivot = 100;
    }

    public void draw(ShapeRenderer shapeRenderer, int posX, int posY) {
        // Background bar (grey)
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(posX, posY, width, 30);

        // Green side (left player's health)
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(posX, posY, (pivot / 100f) * width, 30);

        // Red side (right player's pushback)
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(posX + (pivot / 100f) * width, posY, width - (pivot / 100f) * width, 30);
    }
}
