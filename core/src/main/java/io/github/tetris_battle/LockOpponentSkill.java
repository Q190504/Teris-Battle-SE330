package io.github.tetris_battle;

public class LockOpponentSkill extends Skill {
    private float timer = 0f;
    private final Player player;

    public LockOpponentSkill(Player player, float cooldownTime) {
        super(cooldownTime);
        this.player = player;
        setEffectingTime(5f); //5 seconds
    }

    public LockOpponentSkill(float cooldownTime) {
        super(cooldownTime);
        this.player = null;
        setEffectingTime(5f); //5 seconds
    }

    @Override
    public void activate() {
        if (canActivate()) {
            active = true;
            timer = getEffectingTime();
            if (player!= null) {
                player.setIsBeingLocked(true);
            } else {
                Main.client.send("lock_player");
            }
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (active) {
            timer -= delta;
            if (timer <= 0) {
                active = false;
                if (player!= null) {
                    player.setIsBeingLocked(false);
                } else {
                    Main.client.send("unlock_player");
                }
                startCooldown();
            }
        }
    }
}
