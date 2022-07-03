package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.ghast._SpecialGhastEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

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
        
        if( target.distanceToSqr( ghast ) < data.rangedAttackMaxRange * data.rangedAttackMaxRange && ghast.canSee( target ) ) {
            chargeTime++;
            if( chargeTime == (data.rangedAttackCooldown >> 1) && !ghast.isSilent() ) {
                ghast.level.levelEvent( null, References.EVENT_GHAST_WARN, ghast.blockPosition(), 0 );
            }
            if( chargeTime >= data.rangedAttackCooldown ) {
                ghast.performRangedAttack( target, 1.0F );
                chargeTime = data.rangedAttackCooldown - data.rangedAttackMaxCooldown;
            }
        }
        else if( chargeTime > 0 ) {
            chargeTime--;
        }
        ghast.setCharging( chargeTime > (data.rangedAttackCooldown >> 1) );
    }
}