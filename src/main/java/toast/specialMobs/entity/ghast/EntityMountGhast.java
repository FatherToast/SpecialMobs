package toast.specialMobs.entity.ghast;

import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import toast.specialMobs.MobHelper;

import java.util.List;

public class EntityMountGhast extends Entity_SpecialGhast {
    
    /// The target rider.
    public EntityLiving targetedRider;
    /// Whether the target was in range last tick.
    public boolean prevInRange;
    
    /// The last known rider.
    private Entity lastRiddenByEntity;
    /// Whether the current rider has a ranged attack.
    private boolean riderIsRanged;
    
    public EntityMountGhast( World world ) {
        super( world );
    }
    
    /// Updates the current goal.
    @Override
    protected void updateEntityGoal() {
        // Update the current target.
        updateEntityTarget();
        // Determine goal: melee attack, float in range, or pickup rider.
        float distanceSq = Float.POSITIVE_INFINITY;
        if( targetedEntity != null ) {
            distanceSq = (float) targetedEntity.getDistanceSqToEntity( this );
        }
        boolean inRange = false;
        if( riddenByEntity != null ) {
            if( riddenByEntity.isEntityAlive() && targetedEntity != null && isRiderRanged() ) {
                inRange = distanceSq < 64.0;
            }
            targetedRider = null;
        }
        else if( targetedEntity == null && (targetedRider == null || targetedRider.ridingEntity != null || !targetedRider.isEntityAlive()) && rand.nextInt( 100 ) == 0 ) {
            List list = worldObj.getEntitiesWithinAABBExcludingEntity( this, boundingBox.expand( 100.0, 100.0, 100.0 ) );
            double closestDistance = Double.POSITIVE_INFINITY;
            
            for( Object o : list ) {
                if( o instanceof EntityLiving ) {
                    EntityLiving entity = (EntityLiving) o;
                    if( entity instanceof IMob && !(entity instanceof EntityFlying) && entity.ridingEntity == null && entity.riddenByEntity == null && entity != targetedEntity && getEntitySenses().canSee( entity ) ) {
                        double distance = entity.getDistanceSqToEntity( this );
                        if( distance < closestDistance ) {
                            targetedRider = entity;
                            closestDistance = distance;
                        }
                    }
                }
            }
        }
        // Perform movement.
        double vX = waypointX - posX;
        double vY = waypointY - posY;
        double vZ = waypointZ - posZ;
        double v = vX * vX + vY * vY + vZ * vZ;
        if( v < 0.1 || v > 3600.0 || inRange != prevInRange ) {
            if( inRange ) {
                setRandomWaypoints( 4.0F );
            }
            else if( targetedRider != null ) {
                waypointX = targetedRider.posX;
                waypointY = targetedRider.posY + targetedRider.height / 2.0F;
                waypointZ = targetedRider.posZ;
                if( !isCourseTraversable( Math.sqrt( v ) ) ) {
                    setRandomWaypoints( 32.0F );
                }
            }
            else if( targetedEntity != null ) {
                waypointX = targetedEntity.posX;
                waypointY = targetedEntity.posY + targetedEntity.height / 2.0F;
                waypointZ = targetedEntity.posZ;
                if( !isCourseTraversable( Math.sqrt( v ) ) ) {
                    setRandomWaypoints( 32.0F );
                }
            }
            else {
                setRandomWaypoints( 32.0F );
                waypointY = Math.max( waypointY, Math.max( 70.0, worldObj.getHeightValue( (int) Math.floor( waypointX ), (int) Math.floor( waypointZ ) ) + 16.0 ) );
            }
        }
        if( courseChangeCooldown-- <= 0 ) {
            courseChangeCooldown += rand.nextInt( 5 ) + 2;
            v = Math.sqrt( v );
            if( isCourseTraversable( v ) ) {
                double speed = getEntityAttribute( SharedMonsterAttributes.movementSpeed ).getAttributeValue() / v;
                if( targetedEntity == null && targetedRider == null ) {
                    speed *= 0.3;
                }
                motionX += vX * speed;
                motionY += vY * speed;
                motionZ += vZ * speed;
            }
            else {
                setRandomWaypoints( 8.0F );
            }
        }
        // Execute goal, if able.
        if( attackCounter > 0 ) {
            attackCounter--;
        }
        if( targetedRider != null ) {
            renderYawOffset = rotationYaw = (float) Math.atan2( targetedRider.posX - posX, targetedRider.posZ - posZ ) * -180.0F / (float) Math.PI;
            
            double reach = width * width * 4.0F + targetedRider.width;
            if( getDistanceSq( targetedRider.posX, targetedRider.posY + targetedRider.height / 2.0F, targetedRider.posZ ) <= reach ) {
                targetedRider.mountEntity( this );
                targetedRider = null;
            }
        }
        else if( targetedEntity != null ) {
            renderYawOffset = rotationYaw = (float) Math.atan2( targetedEntity.posX - posX, targetedEntity.posZ - posZ ) * -180.0F / (float) Math.PI;
            
            if( attackCounter <= 0 ) {
                double reach = width * width * 4.0F + targetedEntity.width;
                if( getDistanceSq( targetedEntity.posX, targetedEntity.posY + targetedEntity.height / 2.0F, targetedEntity.posZ ) <= reach ) {
                    attackCounter = 20;
                    swingItem();
                    attackEntityAsMob( targetedEntity );
                }
            }
            
            if( riddenByEntity instanceof EntityLiving ) {
                if( targetedEntity instanceof EntityLivingBase ) {
                    ((EntityLiving) riddenByEntity).setAttackTarget( (EntityLivingBase) targetedEntity );
                }
                if( riddenByEntity instanceof EntityCreature ) {
                    ((EntityCreature) riddenByEntity).setTarget( targetedEntity );
                }
            }
        }
        else {
            renderYawOffset = rotationYaw = -((float) Math.atan2( motionX, motionZ )) * 180.0F / (float) Math.PI;
        }
        prevInRange = inRange;
    }
    
    /// Updates this entity's target.
    @Override
    protected void updateEntityTarget() {
        if( targetedEntity != null && targetedEntity.isDead ) {
            targetedEntity = null;
        }
        if( targetedEntity == null || aggroCooldown-- <= 0 ) {
            targetedEntity = worldObj.getClosestVulnerablePlayerToEntity( this, 100.0 );
            if( targetedEntity != null && dimension == 0 ) {
                double dX = targetedEntity.posX - posX;
                double dZ = targetedEntity.posZ - posZ;
                if( dX * dX + dZ * dZ > 256.0 ) {
                    targetedEntity = null;
                }
            }
            if( targetedEntity != null ) {
                aggroCooldown = 20;
            }
        }
        if( targetedRider != null && (targetedRider.ridingEntity != null || targetedRider.riddenByEntity != null || !targetedRider.isEntityAlive()) ) {
            targetedRider = null;
        }
    }
    
    /// Returns true if the rider has a ranged attack.
    public boolean isRiderRanged() {
        if( lastRiddenByEntity != riddenByEntity ) {
            riderIsRanged = riddenByEntity instanceof EntityLiving && MobHelper.hasRangedAttack( (EntityLiving) riddenByEntity );
            lastRiddenByEntity = riddenByEntity;
        }
        return riderIsRanged;
    }
    
    /// True if the ghast has an unobstructed line of travel to the waypoint.
    @Override
    public boolean isCourseTraversable( double v ) {
        double dX = (waypointX - posX) / v;
        double dY = (waypointY - posY) / v;
        double dZ = (waypointZ - posZ) / v;
        AxisAlignedBB aabb;
        /// Check to not suffocate rider.
        if( riddenByEntity != null && riddenByEntity.isEntityAlive() ) {
            aabb = riddenByEntity.boundingBox.copy();
            for( int i = 1; i < v; i++ ) {
                aabb.offset( dX, dY, dZ );
                if( !worldObj.getCollidingBoundingBoxes( riddenByEntity, aabb ).isEmpty() )
                    return false;
            }
        }
        /// Check for self.
        aabb = boundingBox.copy();
        for( int i = 1; i < v; i++ ) {
            aabb.offset( dX, dY, dZ );
            if( !worldObj.getCollidingBoundingBoxes( this, aabb ).isEmpty() )
                return false;
        }
        return true;
    }
}