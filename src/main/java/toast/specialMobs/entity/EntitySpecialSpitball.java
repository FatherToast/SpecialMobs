package toast.specialMobs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import toast.specialMobs.SpecialDamageSource;
import toast.specialMobs.entity.cavespider.Entity_SpecialCaveSpider;
import toast.specialMobs.entity.spider.Entity_SpecialSpider;

import java.util.List;

public class EntitySpecialSpitball extends Entity {
    
    public EntityLiving shootingEntity = null;
    
    // The amount of damage this deals.
    private float damage;
    
    public EntitySpecialSpitball( World world ) {
        super( world );
    }
    
    public EntitySpecialSpitball( World world, EntityLiving entity, Entity target, float speed, float spread ) {
        super( world );
        shootingEntity = entity;
        setLocationAndAngles( entity.posX, entity.posY + entity.getEyeHeight() - 0.1, entity.posZ, entity.rotationYaw, entity.rotationPitch );
        posX -= MathHelper.cos( rotationYaw / 180.0F * (float) Math.PI ) * 0.16F;
        posY -= 0.1;
        posZ -= MathHelper.sin( rotationYaw / 180.0F * (float) Math.PI ) * 0.16F;
        setPosition( posX, posY, posZ );
        motionX = (target.posX - entity.posX) * 0.7;
        motionY = (target.posY + target.getEyeHeight() - 0.7 - posY) * 0.7;
        motionZ = (target.posZ - entity.posZ) * 0.7;
        double vH = MathHelper.sqrt_double( motionX * motionX + motionZ * motionZ );
        
        if( vH >= 1E-7 ) {
            rotationYaw = (float) (Math.atan2( motionZ, motionX ) * 180.0 / Math.PI) - 90.0F;
            rotationPitch = (float) (-Math.atan2( motionY, vH ) * 180.0 / Math.PI);
            double dX = motionX / vH;
            double dZ = motionZ / vH;
            setLocationAndAngles( entity.posX + dX, posY, entity.posZ + dZ, rotationYaw, rotationPitch );
            yOffset = 0.0F;
            calculateVelocity( motionX, motionY + vH * 0.2, motionZ, speed, spread );
        }
    }
    
    @Override
    protected void entityInit() {
        setSize( 0.25F, 0.25F );
    }
    
    @Override
    public boolean isInRangeToRenderDist( double d ) {
        double d1 = boundingBox.getAverageEdgeLength() * 256.0;
        return d < d1 * d1;
    }
    
    public void calculateVelocity( double vX, double vY, double vZ, float v, float variance ) {
        float vi = MathHelper.sqrt_double( vX * vX + vY * vY + vZ * vZ );
        vX /= vi;
        vY /= vi;
        vZ /= vi;
        vX += rand.nextGaussian() * 0.0075 * variance;
        vY += rand.nextGaussian() * 0.0075 * variance;
        vZ += rand.nextGaussian() * 0.0075 * variance;
        vX *= v;
        vY *= v;
        vZ *= v;
        motionX = vX;
        motionY = vY;
        motionZ = vZ;
        float vH = MathHelper.sqrt_double( vX * vX + vZ * vZ );
        prevRotationYaw = rotationYaw = (float) (Math.atan2( vX, vZ ) * 180.0 / Math.PI);
        prevRotationPitch = rotationPitch = (float) (Math.atan2( vY, vH ) * 180.0 / Math.PI);
    }
    
    @Override
    public void setVelocity( double vX, double vY, double vZ ) {
        motionX = vX;
        motionY = vY;
        motionZ = vZ;
        
        if( prevRotationPitch == 0.0F && prevRotationYaw == 0.0F ) {
            float vH = MathHelper.sqrt_double( vX * vX + vZ * vZ );
            prevRotationYaw = rotationYaw = (float) (Math.atan2( vX, vZ ) * 180.0 / Math.PI);
            prevRotationPitch = rotationPitch = (float) (Math.atan2( vY, vH ) * 180.0 / Math.PI);
        }
    }
    
    @Override
    public void onUpdate() {
        lastTickPosX = posX;
        lastTickPosY = posY;
        lastTickPosZ = posZ;
        
        super.onUpdate();
        
        if( !worldObj.isRemote ) {
            if( shootingEntity == null || shootingEntity.isDead || getDistanceSqToEntity( shootingEntity ) > 1024.0 ) {
                setDead();
            }
            Vec3 posVec = Vec3.createVectorHelper( posX, posY, posZ );
            Vec3 motionVec = Vec3.createVectorHelper( posX + motionX, posY + motionY, posZ + motionZ );
            MovingObjectPosition object = worldObj.rayTraceBlocks( posVec, motionVec );
            posVec = Vec3.createVectorHelper( posX, posY, posZ );
            motionVec = Vec3.createVectorHelper( posX + motionX, posY + motionY, posZ + motionZ );
            
            if( object != null ) {
                motionVec = Vec3.createVectorHelper( object.hitVec.xCoord, object.hitVec.yCoord, object.hitVec.zCoord );
            }
            Entity entityHit = null;
            // noinspection rawtypes
            List entitiesInPath = worldObj.getEntitiesWithinAABBExcludingEntity( this, boundingBox.addCoord( motionX, motionY, motionZ ).expand( 1.0, 1.0, 1.0 ) );
            double d = Double.POSITIVE_INFINITY;
            
            for( Object o : entitiesInPath ) {
                Entity entityInPath = (Entity) o;
                if( entityInPath.canBeCollidedWith() && !entityInPath.isEntityEqual( shootingEntity ) ) {
                    AxisAlignedBB aabb = entityInPath.boundingBox.expand( 0.3, 0.3, 0.3 );
                    MovingObjectPosition object1 = aabb.calculateIntercept( posVec, motionVec );
                    if( object1 != null ) {
                        double d1 = posVec.distanceTo( object1.hitVec );
                        if( d1 < d ) {
                            entityHit = entityInPath;
                            d = d1;
                        }
                    }
                }
            }
            if( entityHit != null ) {
                object = new MovingObjectPosition( entityHit );
            }
            if( object != null ) {
                onImpact( object );
            }
        }
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        float sqrt = MathHelper.sqrt_double( motionX * motionX + motionZ * motionZ );
        rotationYaw = (float) (Math.atan2( motionX, motionZ ) * 180.0 / Math.PI);
        
        // noinspection all
        for( rotationPitch = (float) (Math.atan2( motionY, sqrt ) * 180.0 / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F ) {
            // Do nothing
        }
        while( rotationPitch - prevRotationPitch >= 180.0F ) {
            prevRotationPitch += 360.0F;
        }
        while( rotationYaw - prevRotationYaw < -180.0F ) {
            prevRotationYaw -= 360.0F;
        }
        while( rotationYaw - prevRotationYaw >= 180.0F ) {
            prevRotationYaw += 360.0F;
        }
        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
        if( isInWater() ) {
            setDead();
        }
        motionX *= 0.99;
        motionY *= 0.99;
        motionZ *= 0.99;
        motionY -= getGravityVelocity();
        setPosition( posX, posY, posZ );
    }
    
    public void onImpact( MovingObjectPosition object ) {
        if( object.entityHit != null ) {
            SpecialDamageSource damageSource = shootingEntity == null
                    ? new SpecialDamageSource( "generic", this, this )
                    : new SpecialDamageSource( "generic", this, shootingEntity );
            
            damageSource.setProjectile().setDamageBypassesArmor().setMagicDamage().setDifficultyScaled().setHungerDamage( 0.6F );
            
            if( object.entityHit.attackEntityFrom( damageSource, getDamage() ) ) {
                if( shootingEntity instanceof Entity_SpecialSpider ) {
                    ((Entity_SpecialSpider) shootingEntity).onTypeAttack( object.entityHit );
                }
                else if( shootingEntity instanceof Entity_SpecialCaveSpider ) {
                    ((Entity_SpecialCaveSpider) shootingEntity).onTypeAttack( object.entityHit );
                }
            }
        }
        setDead();
    }
    
    /** @return The damage this projectile deals. */
    public float getDamage() {
        return damage;
    }
    
    /** Sets the damage this projectile should deal. */
    public void setDamage( float value ) {
        damage = value;
    }
    
    protected float getGravityVelocity() {
        return 0.03F;
    }
    
    @Override
    public float getShadowSize() {
        return 0.0F;
    }
    
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        // Nothing to save
    }
    
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        // Nothing to load
    }
}