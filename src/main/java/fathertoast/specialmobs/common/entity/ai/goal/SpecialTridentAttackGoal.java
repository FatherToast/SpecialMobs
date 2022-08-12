package fathertoast.specialmobs.common.entity.ai.goal;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.monster.DrownedEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * Copy of the drowned trident attack goal made accessible.
 * <p>
 * {@link DrownedEntity.TridentAttackGoal}
 */
public class SpecialTridentAttackGoal extends RangedAttackGoal {
    
    private final MobEntity mob;
    
    public SpecialTridentAttackGoal( IRangedAttackMob entity, double p_i48907_2_, int p_i48907_4_, float p_i48907_5_ ) {
        super( entity, p_i48907_2_, p_i48907_4_, p_i48907_5_ );
        mob = (DrownedEntity) entity;
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() { return super.canUse() && mob.getMainHandItem().getItem() == Items.TRIDENT; }
    
    /** Called when this AI is activated. */
    @Override
    public void start() {
        super.start();
        mob.setAggressive( true );
        mob.startUsingItem( Hand.MAIN_HAND );
    }
    
    /** Called when this AI is deactivated. */
    @Override
    public void stop() {
        super.stop();
        mob.stopUsingItem();
        mob.setAggressive( false );
    }
}