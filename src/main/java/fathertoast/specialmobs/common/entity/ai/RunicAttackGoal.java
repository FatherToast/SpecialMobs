package fathertoast.specialmobs.common.entity.ai;

import fathertoast.specialmobs.common.entity.enderman.RunicEndermanEntity;
import net.minecraft.entity.ai.goal.Goal;

public class RunicAttackGoal extends Goal {

    private final RunicEndermanEntity runicEnderman;
    private final int attackTime;
    private final float baseDamage;

    public RunicAttackGoal( RunicEndermanEntity runicEnderman, int attackTime, float baseDamage ) {
        this.runicEnderman = runicEnderman;
        this.attackTime = attackTime;
        this.baseDamage = baseDamage;
    }

    @Override
    public boolean canUse() {
        return false;
    }
}
