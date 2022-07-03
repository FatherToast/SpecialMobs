package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.ghast._SpecialGhastEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.MathHelper;

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
            final float range = ghast.getSpecialData().rangedAttackMaxRange > 0.0F ?
                    ghast.getSpecialData().rangedAttackMaxRange :
                    16.0F; // Range for melee ghast to face target
            
            if( target.distanceToSqr( ghast ) < range * range ) {
                setFacing( target.getX() - ghast.getX(), target.getZ() - ghast.getZ() );
                return;
            }
        }
        // Allow move direction facing even if target exists out of attack range
        setFacing( ghast.getDeltaMovement().x, ghast.getDeltaMovement().z );
    }
    
    private void setFacing( double x, double z ) {
        ghast.yBodyRot = ghast.yRot = (float) MathHelper.atan2( x, z ) * -180.0F / (float) Math.PI;
    }
}