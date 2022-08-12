package fathertoast.specialmobs.common.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.util.math.MathHelper;

/**
 * The drowned movement controller repurposed for use on other mobs.
 * <p>
 * {@link net.minecraft.entity.monster.DrownedEntity.MoveHelperController}
 */
public class AmphibiousMovementController<T extends MobEntity & IAmphibiousMob> extends MovementController {
    
    private final T owner;
    
    public AmphibiousMovementController( T entity ) {
        super( entity );
        owner = entity;
    }
    
    public void tick() {
        final LivingEntity target = owner.getTarget();
        if( owner.shouldSwim() && owner.isInWater() ) {
            if( target != null && target.getY() > owner.getY() || owner.isSwimmingUp() ) {
                owner.setDeltaMovement( owner.getDeltaMovement().add( 0.0, 0.002, 0.0 ) );
            }
            
            if( operation != MovementController.Action.MOVE_TO || owner.getNavigation().isDone() ) {
                owner.setSpeed( 0.0F );
                return;
            }
            
            final double dX = wantedX - owner.getX();
            final double dY = wantedY - owner.getY();
            final double dZ = wantedZ - owner.getZ();
            final double distance = MathHelper.sqrt( dX * dX + dY * dY + dZ * dZ );
            
            final float targetYRot = (float) MathHelper.atan2( dZ, dX ) * 180.0F / (float) Math.PI - 90.0F;
            owner.yRot = rotlerp( owner.yRot, targetYRot, 90.0F );
            owner.yBodyRot = owner.yRot;
            
            final float maxSpeed = (float) (speedModifier * owner.getAttributeValue( Attributes.MOVEMENT_SPEED ));
            final float speed = MathHelper.lerp( 0.125F, owner.getSpeed(), maxSpeed );
            owner.setSpeed( speed );
            owner.setDeltaMovement( owner.getDeltaMovement().add(
                    speed * dX * 0.005, speed * dY / distance * 0.1, speed * dZ * 0.005 ) );
        }
        else {
            if( !owner.isOnGround() ) {
                owner.setDeltaMovement( owner.getDeltaMovement().add( 0.0, -0.008, 0.0 ) );
            }
            super.tick();
        }
    }
}