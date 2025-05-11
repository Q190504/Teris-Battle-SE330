package io.github.tetris_battle;

public abstract class Skill {
    protected boolean active;
    private final float cooldownTime;
    private float currentCooldown = 0f;
    private float effectingTime;

    public Skill(float cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public void update(float delta) {
        if (currentCooldown > 0) {
            currentCooldown -= delta;
            if (currentCooldown < 0) currentCooldown = 0;
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

    public float getEffectingTime() { return effectingTime; }

    public void setEffectingTime(float effectingTime)
    {
        this.effectingTime = effectingTime;
    }
}

