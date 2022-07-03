package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.ai.SimpleFlyingMovementController;
import fathertoast.specialmobs.common.entity.ghast._SpecialGhastEntity;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;

/**
 * Melee attack goal modified to function for ghasts.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SpecialGhastMeleeAttackGoal extends Goal {
    
    private final _SpecialGhastEntity ghast;
    
    private int attackTimer;
    
    private int pathUpdateCooldown;
    private boolean wasPathingToTarget;
    
    public SpecialGhastMeleeAttackGoal( _SpecialGhastEntity entity ) {
        ghast = entity;
        setFlags( EnumSet.of( Goal.Flag.MOVE ) );
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() { return ghast.getTarget() != null; }
    
    /** Called when this AI is activated. */
    @Override
    public void start() {
        attackTimer = 0;
        
        final LivingEntity target = ghast.getTarget();
        if( target != null ) {
            setWantedPosition( target );
            wasPathingToTarget = !(ghast.getMoveControl() instanceof SimpleFlyingMovementController) ||
                    ((SimpleFlyingMovementController) ghast.getMoveControl()).canReachWantedPosition();
            pathUpdateCooldown = 5;
        }
    }
    
    /** Called each tick while this AI is active. */
    @Override
    public void tick() {
        final LivingEntity target = ghast.getTarget();
        if( target == null ) return;
        
        // Move towards the target
        final MovementController moveControl = ghast.getMoveControl();
        if( pathUpdateCooldown-- <= 0 || !moveControl.hasWanted() ) {
            pathUpdateCooldown = ghast.getRandom().nextInt( 5 ) + 3;
            
            final SimpleFlyingMovementController flyingControl = moveControl instanceof SimpleFlyingMovementController ?
                    (SimpleFlyingMovementController) moveControl : null;
            if( flyingControl == null || flyingControl.isWantedPositionStale( target ) ) {
                if( flyingControl == null || flyingControl.canReachPosition( target ) ) {
                    setWantedPosition( target );
                    wasPathingToTarget = true;
                }
                else if( !wasPathingToTarget || !moveControl.hasWanted() || flyingControl.getDistanceSqToWantedPosition() < 2.0 ) {
                    setWantedPosition();
                    wasPathingToTarget = false;
                }
            }
        }
        
        // Attack the target when able
        if( attackTimer > 0 ) {
            attackTimer--;
        }
        else {
            final double reachSq = (ghast.getBbWidth() * ghast.getBbWidth() + target.getBbWidth() * target.getBbWidth()) / 4.0 + 5.0;
            if( target.distanceToSqr( ghast ) < reachSq ) {
                attackTimer = 20;
                ghast.doHurtTarget( target );
            }
        }
    }
    
    private void setWantedPosition( LivingEntity target ) {
        ghast.getMoveControl().setWantedPosition( target.getX(), target.getY( 0.5 ), target.getZ(), 1.2 );
    }
    
    private void setWantedPosition() {
        final float diameter = 8.0F;
        ghast.getMoveControl().setWantedPosition(
                ghast.getX() + (ghast.getRandom().nextFloat() - 0.5F) * diameter,
                ghast.getY() + (ghast.getRandom().nextFloat() - 0.5F) * diameter,
                ghast.getZ() + (ghast.getRandom().nextFloat() - 0.5F) * diameter,
                1.0 );
    }
}