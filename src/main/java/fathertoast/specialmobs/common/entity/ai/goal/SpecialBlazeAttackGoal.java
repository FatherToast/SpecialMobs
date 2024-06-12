package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.blaze._SpecialBlazeEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * The attack AI used by blazes. This handles both ranged and melee behaviors.
 */
public class SpecialBlazeAttackGoal extends Goal {
    
    private final _SpecialBlazeEntity blaze;
    
    private int attackStep;
    private int attackTime;
    private int lastSeen;
    
    public SpecialBlazeAttackGoal( _SpecialBlazeEntity entity ) {
        blaze = entity;
        setFlags( EnumSet.of( Goal.Flag.MOVE, Goal.Flag.LOOK ) );
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() {
        final LivingEntity target = blaze.getTarget();
        return target != null && target.isAlive() && blaze.canAttack( target );
    }
    
    /** Called when this AI is activated. */
    @Override
    public void start() {
        attackStep = 0;
    }
    
    /** Called when this AI is deactivated. */
    @Override
    public void stop() {
        blaze.setCharged( false );
        lastSeen = 0;
    }
    
    /** Called each tick while this AI is active. */
    @Override
    public void tick() {
        attackTime--;
        
        final LivingEntity target = blaze.getTarget();
        if( target == null ) return;
        
        final SpecialMobData<_SpecialBlazeEntity> data = blaze.getSpecialData();
        
        final boolean canSee = blaze.getSensing().hasLineOfSight( target );
        if( canSee ) lastSeen = 0;
        else lastSeen++;
        
        final double distanceSqr = blaze.distanceToSqr( target );
        final float rangeSq = data.getRangedAttackMaxRange() * data.getRangedAttackMaxRange();
        if( distanceSqr < getAttackReachSqr( target ) ) {
            if( canSee && attackTime <= 0 ) {
                attackTime = 20;
                blaze.doHurtTarget( target );
            }
            blaze.getMoveControl().setWantedPosition( target.getX(), target.getY(), target.getZ(), 1.0 );
        }
        else if( distanceSqr < rangeSq && canSee ) {
            if( attackTime <= 0 ) {
                attackStep++;
                
                if( attackStep == 1 ) {
                    attackTime = data.getRangedAttackCooldown();
                    blaze.setCharged( true );
                }
                else if( attackStep <= 1 + blaze.fireballBurstCount ) {
                    attackTime = blaze.fireballBurstDelay;
                }
                else {
                    attackTime = data.getRangedAttackMaxCooldown() - data.getRangedAttackCooldown();
                    attackStep = 0;
                    blaze.setCharged( false );
                }
                
                if( attackStep > 1 ) {
                    blaze.performRangedAttack( target, attackStep );
                }
            }
            blaze.getLookControl().setLookAt( target, 10.0F, 10.0F );
        }
        else if( lastSeen < 5 ) {
            blaze.getMoveControl().setWantedPosition( target.getX(), target.getY(), target.getZ(), 1.0 );
        }
        
        super.tick();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    protected double getAttackReachSqr(LivingEntity target ) {
        return blaze.getBbWidth() * blaze.getBbWidth() * 4.0F + target.getBbWidth() + 2.0F;
    }
}