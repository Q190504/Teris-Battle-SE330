package io.github.tetris_battle;

public class ExtraPointsSkill extends Skill {
    private float timer = 0f;
    private final ScoreManager scoreManager;

    public ExtraPointsSkill(ScoreManager scoreManager, float cooldownTime) {
        super(cooldownTime);
        this.scoreManager = scoreManager;
        setEffectingTime(10f); // 10 seconds
    }

    @Override
    public String getName() {
        return "Extra Point";
    }

    public static String getStaticName() {
        return "Extra Point";
    }

    @Override
    public void activate() {
        if (canActivate()) {
            active = true;
            timer = getEffectingTime();
            scoreManager.setDoubleDamage(true);
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
                startCooldown();
            }
        }
    }
}
