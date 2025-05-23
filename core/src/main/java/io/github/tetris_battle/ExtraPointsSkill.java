package io.github.tetris_battle;

public class ExtraPointsSkill extends Skill {
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

    public String getInstruction() {
        return "Score X2";
    }

    @Override
    public void activate() {
        if (canActivate()) {
            active = true;
            activeTimer = getEffectingTime();
            scoreManager.setDoubleDamage(true);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (active) {
            activeTimer -= delta;
            if (activeTimer <= 0) {
                active = false;
                scoreManager.setDoubleDamage(false);
                startCooldown();
            }
        }
    }
}
