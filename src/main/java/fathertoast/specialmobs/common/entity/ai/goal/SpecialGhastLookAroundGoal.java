package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.ghast._SpecialGhastEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Implementation of GhastEntity.LookAroundGoal that is visible to special ghasts and sensitive to special mob data.
 */
public class SpecialGhastLookAroundGoal extends Goal {
    
    private final _SpecialGhastEntity ghast;
    
    public SpecialGhastLookAroundGoal( _SpecialGhastEntity entity ) {
        ghast = entity;
        setFlags( EnumSet.of( Goal.Flag.LOOK ) );
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() { return true; }
    
    /** Called each tick while this AI is active. */
    @Override
    public void tick() {
        final LivingEntity target = ghast.getTarget();
        if( target != null ) {
            final float range = ghast.getSpecialData().getRangedAttackMaxRange() > 0.0F ?
                    ghast.getSpecialData().getRangedAttackMaxRange() :
                    16.0F; // Range for melee ghast to face target
            
            if( target.distanceToSqr( ghast ) < range * range ) {
                setFacing( target.getX() - ghast.getX(), target.getZ() - ghast.getZ() );
                return;
            }
        }
        // Allow move direction facing even if target exists out of attack range
        setFacing( ghast.getDeltaMovement().x, ghast.getDeltaMovement().z );
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
    
    private void setFacing( double x, double z ) {
        ghast.setYRot((float) Mth.atan2( x, z ) * -180.0F / (float) Math.PI);
        ghast.yBodyRot = ghast.getYRot();
    }
}