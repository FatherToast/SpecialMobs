package fathertoast.specialmobs.common.entity.ai.goal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.Items;

/**
 * Copy of the drowned trident attack goal made accessible.
 * <p>
 * {@link net.minecraft.world.entity.monster.Drowned.DrownedTridentAttackGoal}
 */
@SuppressWarnings("JavadocReference")
public class SpecialTridentAttackGoal extends RangedAttackGoal {
    
    private final Mob mob;
    
    public SpecialTridentAttackGoal( RangedAttackMob entity, double speedMod, int attackInterval, float attackRange ) {
        super( entity, speedMod, attackInterval, attackRange );
        mob = (Drowned) entity;
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() { return super.canUse() && mob.getMainHandItem().getItem() == Items.TRIDENT; }
    
    /** Called when this AI is activated. */
    @Override
    public void start() {
        super.start();
        mob.setAggressive( true );
        mob.startUsingItem( InteractionHand.MAIN_HAND );
    }
    
    /** Called when this AI is deactivated. */
    @Override
    public void stop() {
        super.stop();
        mob.stopUsingItem();
        mob.setAggressive( false );
    }
}