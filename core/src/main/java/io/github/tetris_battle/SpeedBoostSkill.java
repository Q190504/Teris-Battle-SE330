package io.github.tetris_battle;

public class SpeedBoostSkill extends Skill {
    private float activeTimer = 0f; // Timer để đếm thời gian skill đang active
    private final Player player;

    public SpeedBoostSkill(Player player, float cooldownTime) {
        super(cooldownTime);
        this.player = player;
        setEffectingTime(5f); // 5 seconds duration
    }

    public SpeedBoostSkill(float cooldownTime) {
        super(cooldownTime);
        this.player = null;
        setEffectingTime(5f); // 5 seconds duration
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

    // Method to get remaining active time
    public float getRemainingActiveTime() {
        return active ? activeTimer : 0f;
    }
}