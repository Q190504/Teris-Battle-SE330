package io.github.tetris_battle;

public class LockOpponentSkill extends Skill {
    private float timer = 0f;
    private final Player player;

    public LockOpponentSkill(Player player, float cooldownTime) {
        super(cooldownTime);
        this.player = player;
        setEffectingTime(5f); //5 seconds
    }

    @Override
    public void activate() {
        if (canActivate()) {
            active = true;
            timer = getEffectingTime();
            player.setIsBeingLocked(true);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (active) {
            timer -= delta;
            if (timer <= 0) {
                active = false;
                player.setIsBeingLocked(false);
                startCooldown();
            }
        }
    }
}
