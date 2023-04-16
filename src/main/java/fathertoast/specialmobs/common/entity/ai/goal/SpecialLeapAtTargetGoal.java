package fathertoast.specialmobs.common.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SpecialLeapAtTargetGoal extends Goal {
    private static final float BASE_Y_VELOCITY = 0.4F;
    
    private final Mob mob;
    
    private final int infrequency;
    
    private final float minDistSq;
    private final float maxDistSq;
    
    private final float forwardPower;
    private final float upwardPower;
    
    private float momentum = 0.2F;
    private boolean canUseWhileMounted = false;
    private boolean ignoreFallDamage = false;
    
    private LivingEntity target;
    
    public SpecialLeapAtTargetGoal( Mob entity, int infreq, float minDist, float maxDist, float forward, float upward ) {
        mob = entity;
        infrequency = infreq;
        minDistSq = minDist * minDist;
        maxDistSq = maxDist * maxDist;
        forwardPower = forward;
        upwardPower = upward;
        setFlags( EnumSet.of( Goal.Flag.JUMP, Goal.Flag.MOVE ) );
    }
    
    /** Builder that sets momentum. */
    @SuppressWarnings( "unused" )
    public SpecialLeapAtTargetGoal setMomentum( float value ) {
        momentum = value;
        return this;
    }
    
    /** Builder that enables the entity to leap while mounted. */
    @SuppressWarnings( "unused" )
    public SpecialLeapAtTargetGoal canUseWhileMounted() {
        canUseWhileMounted = true;
        return this;
    }
    
    /** Builder that enables the entity to ignore fall damage from leaping. */
    @SuppressWarnings( "unused" )
    public SpecialLeapAtTargetGoal ignoreFallDamage() {
        ignoreFallDamage = true;
        return this;
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() {
        if( !mob.isOnGround() || mob.isPassenger() || !canUseWhileMounted && mob.isVehicle() ) return false;
        
        target = mob.getTarget();
        if( target == null || mob.getRandom().nextInt( infrequency ) != 0 ) return false;
        
        final double distanceSq = mob.distanceToSqr( target );
        return distanceSq >= minDistSq && distanceSq <= maxDistSq;
    }
    
    /** Called when this AI is activated. */
    @Override
    public void start() {
        final Vec3 vLeap = new Vec3( target.getX() - mob.getX(), 0.0, target.getZ() - mob.getZ() )
                .normalize().scale( forwardPower ).add( mob.getDeltaMovement().scale( momentum ) );
        
        mob.setDeltaMovement( vLeap.x, BASE_Y_VELOCITY * upwardPower, vLeap.z );
    }
    
    /** @return Called each update while active and returns true if this AI can remain active. */
    @Override
    public boolean canContinueToUse() {
        return !mob.isOnGround() && !mob.isPassenger() && !mob.isInWaterOrBubble() && !mob.isInLava();
    }
    
    /** Called each tick while this AI is active. */
    @Override
    public void tick() {
        if( ignoreFallDamage ) mob.fallDistance = 0.0F;
    }
}