package io.github.tetris_battle;

public class SpeedBoostSkill extends Skill {

    private final Player player;

    public SpeedBoostSkill(Player player, float cooldownTime) {
        super(cooldownTime);
        this.player = player;
        setEffectingTime(SkillConfigs.SPEED_BOOST_ACTIVE);
    }

    public SpeedBoostSkill(float cooldownTime) {
        super(cooldownTime);
        this.player = null;
        setEffectingTime(SkillConfigs.SPEED_BOOST_ACTIVE);
    }

    @Override
    public String getName() {
        return "Boost Speed";
    }

    public static String getStaticName() {
        return "Boost Speed";
    }
    public static String getStaticInstruction() {
        return "Allow immediate drop for " + SkillConfigs.SPEED_BOOST_ACTIVE + "s." +
            "\nCooldown: " + SkillConfigs.SPEED_BOOST_CD + "s.";
    }

    public String getInstruction() {
        return "Press SPACE";
    }

    @Override
    public void activate() {
        if (canActivate()) {
            active = true;
            activeTimer = getEffectingTime(); // Set timer để đếm ngược
            // Skill is now active, player can use SPACE to drop pieces quickly
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta); // Update cooldown timer

        if (active) {
            activeTimer -= delta;
            if (activeTimer <= 0) {
                active = false;
                activeTimer = 0f;
                startCooldown(); // Bắt đầu cooldown
            }
        }
    }

    // Method to check if speed boost can be used (called when SPACE is pressed)
    public boolean canUseSpeedBoost() {
        return active;
    }

    // Method to perform the speed boost drop
    public void performSpeedDrop() {
        if (active && player != null) {
            player.dropCurrentPieceToBottom();
        }
    }
}
