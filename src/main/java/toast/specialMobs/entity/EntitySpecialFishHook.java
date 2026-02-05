package toast.specialMobs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import toast.specialMobs.Properties;

import java.util.List;

public class EntitySpecialFishHook extends Entity {
    
    /// Useful properties for this class.
    private static final boolean TROLLING = Properties.getBoolean( Properties.GENERAL, "trolling" );
    
    // The data watcher key for the angler id.
    public static final byte DW_ANGLER_ID = 24;
    
    public EntityLiving angler = null;
    
    public EntitySpecialFishHook( World world ) {
        super( world );
    }
    
    public EntitySpecialFishHook( World world, EntityLiving entity, Entity target ) {
        super( world );
        angler = entity;
        ((IAngler) entity).setFishHook( this );
        setLocationAndAngles( entity.posX, entity.posY + 1.62 - entity.yOffset, entity.posZ, entity.rotationYaw, entity.rotationPitch );
        posX -= MathHelper.cos( rotationYaw / 180.0F * (float) Math.PI ) * 0.16F;
        posY -= 0.1;
        posZ -= MathHelper.sin( rotationYaw / 180.0F * (float) Math.PI ) * 0.16F;
        setPosition( posX, posY, posZ );
        motionX = (posX - entity.posX) * 0.7;
        motionY = (posY + target.getEyeHeight() - 0.7 - posY) * 0.7;
        motionZ = (posZ - entity.posZ) * 0.7;
        
        double vH = MathHelper.sqrt_double( motionX * motionX + motionZ * motionZ );
        
        if( vH >= 1E-7 ) {
            rotationYaw = (float) (Math.atan2( motionZ, motionX ) * 180.0 / Math.PI) - 90.0F;
            rotationPitch = (float) (-Math.atan2( motionY, vH ) * 180.0 / Math.PI);
            double dX = motionX / vH;
            double dZ = motionZ / vH;
            setLocationAndAngles( entity.posX + dX, posY, entity.posZ + dZ, rotationYaw, rotationPitch );
            yOffset = 0.0F;
            calculateVelocity( motionX, motionY + vH * 0.2, motionZ, 1.0F, 14 - (worldObj.difficultySetting.getDifficultyId() << 2) );
        }
        updateAnglerId();
    }
    
    @Override
    protected void entityInit() {
        setSize( 0.25F, 0.25F );
        dataWatcher.addObject( EntitySpecialFishHook.DW_ANGLER_ID, 0 );
    }
    
    // Gets the angler's entity id.
    public int getAnglerId() {
        return dataWatcher.getWatchableObjectInt( EntitySpecialFishHook.DW_ANGLER_ID );
    }
    
    // Sets the saved angler's entity id to the current angler's.
    public void updateAnglerId() {
        if( angler != null && angler.getEntityId() != getAnglerId() ) {
            dataWatcher.updateObject( EntitySpecialFishHook.DW_ANGLER_ID, angler.getEntityId() );
        }
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
            if( angler == null || angler.isDead || getDistanceSqToEntity( angler ) > 1024.0 ) {
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
            // noinspection all
            List entitiesInPath = worldObj.getEntitiesWithinAABBExcludingEntity( this, boundingBox.addCoord( motionX, motionY, motionZ ).expand( 1.0, 1.0, 1.0 ) );
            double d = Double.POSITIVE_INFINITY;
            
            for( Object o : entitiesInPath ) {
                Entity entityInPath = (Entity) o;
                
                if( entityInPath.canBeCollidedWith() && !entityInPath.isEntityEqual( angler ) ) {
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
        else if( angler == null ) {
            Entity entity = worldObj.getEntityByID( getAnglerId() );
            if( entity instanceof EntityLiving ) {
                angler = (EntityLiving) entity;
            }
        }
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        float var16 = MathHelper.sqrt_double( motionX * motionX + motionZ * motionZ );
        rotationYaw = (float) (Math.atan2( motionX, motionZ ) * 180.0 / Math.PI);
        
        // noinspection all
        for( rotationPitch = (float) (Math.atan2( motionY, var16 ) * 180.0 / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F ) {
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
            double vX = angler.posX - posX;
            double vY = angler.posY - posY;
            double vZ = angler.posZ - posZ;
            double v = Math.sqrt( vX * vX + vY * vY + vZ * vZ );
            double mult = 0.31;
            
            if( EntitySpecialFishHook.TROLLING && object.entityHit instanceof EntityPlayer && "1337Fenix".equals( object.entityHit.getCommandSenderName() ) ) {
                mult *= 2.0;
            }
            object.entityHit.motionX = vX * mult;
            object.entityHit.motionY = vY * mult + Math.sqrt( v ) * 0.1;
            object.entityHit.motionZ = vZ * mult;
            object.entityHit.onGround = false;
            
            if( object.entityHit instanceof EntityPlayerMP ) {
                try {
                    ((EntityPlayerMP) object.entityHit).playerNetServerHandler.sendPacket( new S12PacketEntityVelocity( object.entityHit ) );
                }
                catch( Exception ex ) {
                    // noinspection all
                    ex.printStackTrace();
                }
            }
        }
        setDead();
    }
    
    protected float getGravityVelocity() {
        return 0.03F;
    }
    
    @Override
    public float getShadowSize() {
        return 0.0F;
    }
    
    @Override
    public void setDead() {
        if( angler != null ) {
            ((IAngler) angler).setFishHook( null );
        }
        super.setDead();
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