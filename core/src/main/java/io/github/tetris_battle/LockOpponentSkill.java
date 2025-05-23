package io.github.tetris_battle;

import io.github.ui.Messages;

public class LockOpponentSkill extends Skill {
    private final Player player;

    public LockOpponentSkill(Player player, float cooldownTime) {
        super(cooldownTime);
        this.player = player;
        setEffectingTime(5f); //5 seconds
    }

    @Override
    public String getName() {
        return "Lock Opponent";
    }

    public static String getStaticName() {
        return "Lock Opponent";
    }

    public String getInstruction() {
        return "Opponent LOCKED";
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
            activeTimer = getEffectingTime();
            if (player!= null) {
                player.setIsBeingLocked(true);
            } else {
                Main.client.send(Messages.LOCK_PLAYER);
            }
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (active) {
            activeTimer -= delta;
            if (activeTimer <= 0) {
                active = false;
                if (player!= null) {
                    player.setIsBeingLocked(false);
                } else {
                    Main.client.send(Messages.UNLOCK_PLAYER);
                }
                startCooldown();
            }
        }
    }
}
