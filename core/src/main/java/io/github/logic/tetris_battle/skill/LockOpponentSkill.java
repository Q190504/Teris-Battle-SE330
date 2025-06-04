package io.github.logic.tetris_battle.skill;

import io.github.logic.tetris_battle.Player;
import io.github.client.ui.Main;
import io.github.logic.utils.AudioManager;
import io.github.logic.utils.Messages;
import io.github.logic.utils.SkillConfigs;

public class LockOpponentSkill extends Skill {
    private final Player player;


    public LockOpponentSkill(Player player, float cooldownTime) {
        super(cooldownTime);
        this.player = player;
        setEffectingTime(SkillConfigs.LOCK_OPPONENT_ACTIVE); //5 seconds
    }

    public LockOpponentSkill(float cooldownTime) {
        super(cooldownTime);
        this.player = null;
        setEffectingTime(SkillConfigs.LOCK_OPPONENT_ACTIVE); //5 seconds
    }

    @Override
    public String getName() {
        return "Lock Opponent";
    }

    public static String getStaticName() {
        return "Lock Opponent";
    }
    public static String getStaticInstruction() {
        return "Lock opponent skill for " + SkillConfigs.LOCK_OPPONENT_ACTIVE + "s." +
            "\nCooldown: " + SkillConfigs.LOCK_OPPONENT_CD + "s.";
    }

    public String getInstruction() {
        return "Opponent LOCKED";
    }

    @Override
    public void activate() {
        if (canActivate()) {
            active = true;
            activeTimer = getEffectingTime();
            // Play skill activation sound
            AudioManager.getInstance().playSkillActivate();
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
