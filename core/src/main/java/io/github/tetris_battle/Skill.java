package io.github.tetris_battle;

public abstract class Skill {
    protected boolean active;
    private final float cooldownTime;
    private float currentCooldown = 0f;

    public Skill(float cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public void update(float delta) {
        if (currentCooldown > 0) {
            currentCooldown -= delta;
        }
    }

    public abstract void activate();

    public boolean canActivate() {
        return currentCooldown <= 0 && !active;
    }

    public void startCooldown() {
        currentCooldown = cooldownTime;
    }

    public boolean isActive() {
        return active;
    }

    public float getCooldown() {
        return cooldownTime;
    }

    public float getCurrentCooldown() {
        return currentCooldown;
    }
}

