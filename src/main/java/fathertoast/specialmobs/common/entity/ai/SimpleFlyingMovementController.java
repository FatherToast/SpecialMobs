package fathertoast.specialmobs.common.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Simple movement controller that can be used by flying entities, which takes their movement speed attribute into account.
 */
public class SimpleFlyingMovementController extends MoveControl {
    
    private int floatDuration;
    
    public SimpleFlyingMovementController( Mob entity ) { super( entity ); }
    
    /** Called each tick while this move controller is active. */
    @Override
    public void tick() {
        if( operation == MoveControl.Operation.MOVE_TO ) {
            if( floatDuration-- <= 0 ) {
                floatDuration = mob.getRandom().nextInt( 5 ) + 2;
                
                Vec3 moveVec = new Vec3(
                        wantedX - mob.getX(),
                        wantedY - mob.getY(),
                        wantedZ - mob.getZ() );
                final int distance = Mth.ceil( moveVec.length() );
                moveVec = moveVec.normalize();
                
                if( mob.getRandom().nextBoolean() || canReach( moveVec, distance ) ) { // Skip the "boxcast" sometimes
                    mob.setDeltaMovement( mob.getDeltaMovement().add( moveVec.scale( getScaledMoveSpeed() ) ) );
                }
                else {
                    operation = MoveControl.Operation.WAIT;
                }
            }
        }
    }
    
    public double getScaledMoveSpeed() {
        return 0.1 * speedModifier * mob.getAttributeValue( Attributes.MOVEMENT_SPEED ) / Attributes.MOVEMENT_SPEED.getDefaultValue();
    }
    
    public double getDistanceSqToWantedPosition() {
        return mob.distanceToSqr( getWantedX(), getWantedY(), getWantedZ() );
    }
    
    public boolean isWantedPositionStale( LivingEntity target ) {
        return !hasWanted() ||
                target.distanceToSqr(
                        getWantedX(),
                        getWantedY() - target.getBbHeight() / 2.0F,
                        getWantedZ()
                ) > 1.0;
    }
    
    public boolean canReachWantedPosition() {
        return hasWanted() && canReachPosition( getWantedX(), getWantedY(), getWantedZ() );
    }
    
    public boolean canReachPosition( LivingEntity target ) {
        return canReachPosition( target.getX(), target.getY( 0.5 ), target.getZ() );
    }
    
    public boolean canReachPosition( double x, double y, double z ) {
        final Vec3 targetVec = new Vec3( x - mob.getX(), y - mob.getY(), z - mob.getZ() );
        final int distance = Mth.ceil( targetVec.length() );
        return canReach( targetVec.normalize(), distance );
    }
    
    private boolean canReach( Vec3 direction, int distance ) {
        AABB boundingBox = mob.getBoundingBox();
        for( int i = 1; i < distance; i++ ) {
            boundingBox = boundingBox.move( direction );
            if( !mob.level.noCollision( mob, boundingBox ) ) return false;
        }
        return true;
    }
}