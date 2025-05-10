package io.github.tetris_battle;

public class ExtraPointsSkill extends Skill {
    private float timer = 0f;
    private final ScoreManager scoreManager;

    public ExtraPointsSkill(ScoreManager scoreManager, float cooldownTime) {
        super(cooldownTime);
        this.scoreManager = scoreManager;
    }

    @Override
    public void activate() {
        if (canActivate()) {
            active = true;
            timer = 10f;
            scoreManager.setDoubleDamage(true);
            startCooldown();
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (active) {
            timer -= delta;
            if (timer <= 0) {
                active = false;
                scoreManager.setDoubleDamage(false);
            }
        }
    }
}
