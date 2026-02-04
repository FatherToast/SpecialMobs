package toast.specialMobs.entity.ghast;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

public class EntityMeleeGhast extends Entity_SpecialGhast {
    
    public EntityMeleeGhast( World world ) {
        super( world );
    }
    
    /// Updates the current goal.
    @Override
    protected void updateEntityGoal() {
        // Perform movement.
        double vX = waypointX - posX;
        double vY = waypointY - posY;
        double vZ = waypointZ - posZ;
        double v = vX * vX + vY * vY + vZ * vZ;
        
        if( v < 0.1 || v > 3600.0 ) {
            if( targetedEntity != null ) {
                waypointX = targetedEntity.posX;
                waypointY = targetedEntity.posY + targetedEntity.height / 2.0F;
                waypointZ = targetedEntity.posZ;
                
                if( !isCourseTraversable( Math.sqrt( v ) ) ) {
                    setRandomWaypoints( 32.0F );
                }
            }
            else {
                setRandomWaypoints( 32.0F );
            }
        }
        if( courseChangeCooldown-- <= 0 ) {
            courseChangeCooldown += rand.nextInt( 5 ) + 2;
            v = Math.sqrt( v );
            
            if( isCourseTraversable( v ) ) {
                double speed = getEntityAttribute( SharedMonsterAttributes.movementSpeed ).getAttributeValue() / v;
                motionX += vX * speed;
                motionY += vY * speed;
                motionZ += vZ * speed;
            }
            else {
                setRandomWaypoints( 8.0F );
            }
        }
        // Update the current target.
        updateEntityTarget();
        // Execute goal, if able.
        if( attackCounter > 0 ) {
            attackCounter--;
        }
        if( targetedEntity != null ) {
            renderYawOffset = rotationYaw = -((float) Math.atan2( targetedEntity.posX - posX, targetedEntity.posZ - posZ )) * 180.0F / (float) Math.PI;
            
            if( attackCounter <= 0 ) {
                double reach = width * width * 4.0F + targetedEntity.width;
                
                if( getDistanceSq( targetedEntity.posX, targetedEntity.posY + targetedEntity.height / 2.0F, targetedEntity.posZ ) <= reach ) {
                    attackCounter = 20;
                    swingItem();
                    attackEntityAsMob( targetedEntity );
                }
            }
        }
        else {
            renderYawOffset = rotationYaw = -((float) Math.atan2( motionX, motionZ )) * 180.0F / (float) Math.PI;
        }
    }
}