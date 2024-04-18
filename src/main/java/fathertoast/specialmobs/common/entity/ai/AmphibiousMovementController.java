package fathertoast.specialmobs.common.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

/**
 * The drowned movement controller repurposed for use on other mobs.
 * <p>
 * {@link net.minecraft.world.entity.monster.Drowned.DrownedMoveControl}
 */
public class AmphibiousMovementController<T extends Mob & IAmphibiousMob> extends MoveControl {
    
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
            
            if( operation != MoveControl.Operation.MOVE_TO || owner.getNavigation().isDone() ) {
                owner.setSpeed( 0.0F );
                return;
            }
            
            final double dX = wantedX - owner.getX();
            final double dY = wantedY - owner.getY();
            final double dZ = wantedZ - owner.getZ();
            final double distance = Mth.sqrt( (float) (dX * dX + dY * dY + dZ * dZ) );
            
            final float targetYRot = (float) Mth.atan2( dZ, dX ) * 180.0F / (float) Math.PI - 90.0F;
            owner.setYRot( rotlerp( owner.getYRot(), targetYRot, 90.0F ));
            owner.setYBodyRot(owner.getYRot());
            
            final float maxSpeed = (float) (speedModifier * owner.getAttributeValue( Attributes.MOVEMENT_SPEED ));
            final float speed = Mth.lerp( 0.125F, owner.getSpeed(), maxSpeed );
            owner.setSpeed( speed );
            owner.setDeltaMovement( owner.getDeltaMovement().add(
                    speed * dX * 0.005, speed * dY / distance * 0.1, speed * dZ * 0.005 ) );
        }
        else {
            if( !owner.onGround() ) {
                owner.setDeltaMovement( owner.getDeltaMovement().add( 0.0, -0.008, 0.0 ) );
            }
            super.tick();
        }
    }
}