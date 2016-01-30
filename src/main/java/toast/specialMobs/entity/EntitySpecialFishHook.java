package toast.specialMobs.entity;

import java.util.List;

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

public class EntitySpecialFishHook extends Entity {

    /// Useful properties for this class.
    private static final boolean TROLLING = Properties.getBoolean(Properties.GENERAL, "trolling");

    // The data watcher key for the angler id.
    public static final byte DW_ANGLER_ID = 24;

    public EntityLiving angler = null;

    public EntitySpecialFishHook(World world) {
        super(world);
    }

    public EntitySpecialFishHook(World world, EntityLiving entity, Entity target) {
        super(world);
        this.angler = entity;
        ((IAngler) entity).setFishHook(this);
        this.setLocationAndAngles(entity.posX, entity.posY + 1.62 - entity.yOffset, entity.posZ, entity.rotationYaw, entity.rotationPitch);
        this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F;
        this.posY -= 0.1;
        this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F;
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionX = (target.posX - entity.posX) * 0.7;
        this.motionY = (target.posY + target.getEyeHeight() - 0.7 - this.posY) * 0.7;
        this.motionZ = (target.posZ - entity.posZ) * 0.7;
        double vH = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
        if (vH >= 1E-7) {
            this.rotationYaw = (float)(Math.atan2(this.motionZ, this.motionX) * 180.0 / Math.PI) - 90.0F;
            this.rotationPitch = (float)(-Math.atan2(this.motionY, vH) * 180.0 / Math.PI);
            double dX = this.motionX / vH;
            double dZ = this.motionZ / vH;
            this.setLocationAndAngles(entity.posX + dX, this.posY, entity.posZ + dZ, this.rotationYaw, this.rotationPitch);
            this.yOffset = 0.0F;
            this.calculateVelocity(this.motionX, this.motionY + vH * 0.2, this.motionZ, 1.0F, 14 - (this.worldObj.difficultySetting.getDifficultyId() << 2));
        }
        this.updateAnglerId();
    }

    @Override
    protected void entityInit() {
        this.setSize(0.25F, 0.25F);
        this.dataWatcher.addObject(EntitySpecialFishHook.DW_ANGLER_ID, Integer.valueOf(0));
    }

    // Gets the angler's entity id.
    public int getAnglerId() {
        return this.dataWatcher.getWatchableObjectInt(EntitySpecialFishHook.DW_ANGLER_ID);
    }
    // Sets the saved angler's entity id to the current angler's.
    public void updateAnglerId() {
        if (this.angler != null && this.angler.getEntityId() != this.getAnglerId()) {
            this.dataWatcher.updateObject(EntitySpecialFishHook.DW_ANGLER_ID, Integer.valueOf(this.angler.getEntityId()));
        }
    }

    @Override
    public boolean isInRangeToRenderDist(double d) {
        double d1 = this.boundingBox.getAverageEdgeLength() * 256.0;
        return d < d1 * d1;
    }

    public void calculateVelocity(double vX, double vY, double vZ, float v, float variance) {
        float vi = MathHelper.sqrt_double(vX * vX + vY * vY + vZ * vZ);
        vX /= vi;
        vY /= vi;
        vZ /= vi;
        vX += this.rand.nextGaussian() * 0.0075 * variance;
        vY += this.rand.nextGaussian() * 0.0075 * variance;
        vZ += this.rand.nextGaussian() * 0.0075 * variance;
        vX *= v;
        vY *= v;
        vZ *= v;
        this.motionX = vX;
        this.motionY = vY;
        this.motionZ = vZ;
        float vH = MathHelper.sqrt_double(vX * vX + vZ * vZ);
        this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(vX, vZ) * 180.0 / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(vY, vH) * 180.0 / Math.PI);
    }

    @Override
    public void setVelocity(double vX, double vY, double vZ) {
        this.motionX = vX;
        this.motionY = vY;
        this.motionZ = vZ;
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float vH = MathHelper.sqrt_double(vX * vX + vZ * vZ);
            this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(vX, vZ) * 180.0 / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(vY, vH) * 180.0 / Math.PI);
        }
    }

    @Override
    public void onUpdate() {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        super.onUpdate();
        if (!this.worldObj.isRemote) {
	        if (this.angler == null || this.angler.isDead || this.getDistanceSqToEntity(this.angler) > 1024.0) {
	            this.setDead();
	        }
	        Vec3 posVec = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
	        Vec3 motionVec = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
	        MovingObjectPosition object = this.worldObj.rayTraceBlocks(posVec, motionVec);
	        posVec = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
	        motionVec = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
	        if (object != null) {
	            motionVec = Vec3.createVectorHelper(object.hitVec.xCoord, object.hitVec.yCoord, object.hitVec.zCoord);
	        }
            Entity entityHit = null;
            List entitiesInPath = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0, 1.0, 1.0));
            double d = Double.POSITIVE_INFINITY;
            for (int i = 0; i < entitiesInPath.size(); i++) {
                Entity entityInPath = (Entity) entitiesInPath.get(i);
                if (entityInPath.canBeCollidedWith() && !entityInPath.isEntityEqual(this.angler)) {
                    AxisAlignedBB aabb = entityInPath.boundingBox.expand(0.3, 0.3, 0.3);
                    MovingObjectPosition object1 = aabb.calculateIntercept(posVec, motionVec);
                    if (object1 != null) {
                        double d1 = posVec.distanceTo(object1.hitVec);
                        if (d1 < d) {
                            entityHit = entityInPath;
                            d = d1;
                        }
                    }
                }
            }
            if (entityHit != null) {
                object = new MovingObjectPosition(entityHit);
            }
	        if (object != null) {
	            this.onImpact(object);
	        }
        }
        else if (this.angler == null) {
        	Entity entity = this.worldObj.getEntityByID(this.getAnglerId());
        	if (entity instanceof EntityLiving) {
        		this.angler = (EntityLiving) entity;
        	}
        }
        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        float var16 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0 / Math.PI);
        for (this.rotationPitch = (float)(Math.atan2(this.motionY, var16) * 180.0 / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
            // Do nothing
        }
        while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
            this.prevRotationPitch += 360.0F;
        }
        while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
            this.prevRotationYaw -= 360.0F;
        }
        while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
            this.prevRotationYaw += 360.0F;
        }
        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
        if (this.isInWater()) {
            this.setDead();
        }
        this.motionX *= 0.99;
        this.motionY *= 0.99;
        this.motionZ *= 0.99;
        this.motionY -= this.getGravityVelocity();
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    public void onImpact(MovingObjectPosition object) {
        if (object.entityHit != null) {
            double vX = this.angler.posX - this.posX;
            double vY = this.angler.posY - this.posY;
            double vZ = this.angler.posZ - this.posZ;
            double v = Math.sqrt(vX * vX + vY * vY + vZ * vZ);
            double mult = 0.31;
            if (EntitySpecialFishHook.TROLLING && object.entityHit instanceof EntityPlayer && "1337Fenix".equals(((EntityPlayer)object.entityHit).getCommandSenderName())) {
                mult *= 2.0;
            }
            object.entityHit.motionX = vX * mult;
            object.entityHit.motionY = vY * mult + Math.sqrt(v) * 0.1;
            object.entityHit.motionZ = vZ * mult;
            object.entityHit.onGround = false;
            if (object.entityHit instanceof EntityPlayerMP) {
                try {
                    ((EntityPlayerMP) object.entityHit).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(object.entityHit));
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        this.setDead();
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
        if (this.angler != null) {
            ((IAngler) this.angler).setFishHook(null);
        }
        super.setDead();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
    	// Nothing to save
    }
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
    	// Nothing to load
    }
}