package io.github.logic.tetris_battle.skill;

import io.github.logic.tetris_battle.score.ScoreManager;
import io.github.logic.utils.AudioManager;
import io.github.logic.utils.SkillConfigs;

public class ExtraPointsSkill extends Skill {
    private final ScoreManager scoreManager;

    public ExtraPointsSkill(ScoreManager scoreManager, float cooldownTime) {
        super(cooldownTime);
        this.scoreManager = scoreManager;
        setEffectingTime(SkillConfigs.EXTRA_POINT_ACTIVE); // 10 seconds
    }

    @Override
    public String getName() {
        return "Extra Point";
    }

    public static String getStaticName() {
        return "Extra Point";
    }
    public static String getStaticInstruction() {
        return "Double point for " + SkillConfigs.EXTRA_POINT_ACTIVE + "s." +
            "\nCooldown: " + SkillConfigs.EXTRA_POINT_CD + "s.";
    }

    public String getInstruction() {
        return "Score X2";
    }

    @Override
    public void activate() {
        if (canActivate()) {
            active = true;
            activeTimer = getEffectingTime();
            // Play skill activation sound
            AudioManager.getInstance().playSkillActivate();
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
