package fathertoast.specialmobs.common.entity.ai.goal;

import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.monster.DrownedEntity;

/**
 * Copy of the drowned attack goal made accessible.
 * <p>
 * {@link DrownedEntity.AttackGoal}
 */
public class SpecialDrownedAttackGoal extends ZombieAttackGoal {
    
    private final DrownedEntity drowned;
    
    public SpecialDrownedAttackGoal( DrownedEntity entity, double moveSpeed, boolean longMemory ) {
        super( entity, moveSpeed, longMemory );
        drowned = entity;
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() { return super.canUse() && drowned.okTarget( drowned.getTarget() ); }
    
    /** @return Called each update while active and returns true if this AI can remain active. */
    @Override
    public boolean canContinueToUse() { return super.canContinueToUse() && drowned.okTarget( drowned.getTarget() ); }
}