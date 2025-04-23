package io.github.tetris_battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class HealthBar {
    private static HealthBar instance;

    public static HealthBar getInstance() {
        if (instance == null) {
            instance = new HealthBar();
        }
        return instance;
    }

    private float pivot = 50;
    private int width;

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
    }

    public void pushRight(float percent) {
        pivot += percent;
    }

    public void draw(ShapeRenderer shapeRenderer, int posX, int posY) {
        // Draw red background bar
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(posX, posY, width, 30);

        // Draw green health bar
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(posX, posY, (pivot / 100) * width, 30);
    }
}
