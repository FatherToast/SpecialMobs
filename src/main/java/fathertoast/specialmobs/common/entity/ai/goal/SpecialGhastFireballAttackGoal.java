package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.ghast._SpecialGhastEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Implementation of GhastEntity.FireballAttackGoal that is visible to special ghasts and allows variants to
 * decide how to execute the actual attack.
 */
public class SpecialGhastFireballAttackGoal extends Goal {
    
    private final _SpecialGhastEntity ghast;
    
    private int chargeTime;
    
    public SpecialGhastFireballAttackGoal( _SpecialGhastEntity entity ) { ghast = entity; }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() { return ghast.getTarget() != null; }
    
    /** Called when this AI is activated. */
    @Override
    public void start() { chargeTime = 0; }
    
    /** Called when this AI is deactivated. */
    @Override
    public void stop() { ghast.setCharging( false ); }

    /** Called each tick while this AI is active. */
    @Override
    public void tick() {
        final LivingEntity target = ghast.getTarget();
        if( target == null ) return;
        
        final SpecialMobData<_SpecialGhastEntity> data = ghast.getSpecialData();
        
        if( target.distanceToSqr( ghast ) < data.getRangedAttackMaxRange() * data.getRangedAttackMaxRange() && ghast.hasLineOfSight( target ) ) {
            chargeTime++;
            if( chargeTime == (data.getRangedAttackCooldown() >> 1) && !ghast.isSilent() ) {
                References.LevelEvent.GHAST_WARN.play( ghast );
            }
            if( chargeTime >= data.getRangedAttackCooldown() ) {
                ghast.performRangedAttack( target, 1.0F );
                chargeTime = data.getRangedAttackCooldown() - data.getRangedAttackMaxCooldown();
            }
        }
        else if( chargeTime > 0 ) {
            chargeTime--;
        }
        ghast.setCharging( chargeTime > (data.getRangedAttackCooldown() >> 1) );
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

}