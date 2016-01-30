package toast.specialMobs.entity.ghast;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import toast.specialMobs.MobHelper;

public class EntityMountGhast extends Entity_SpecialGhast
{
    /// The target rider.
    public EntityLiving targetedRider;
    /// Whether the target was in range last tick.
    public boolean prevInRange;

    /// The last known rider.
    private Entity lastRiddenByEntity;
    /// Whether the current rider has a ranged attack.
    private boolean riderIsRanged;

    public EntityMountGhast(World world) {
        super(world);
    }

    /// Updates the current goal.
    @Override
    protected void updateEntityGoal() {
        // Update the current target.
        this.updateEntityTarget();
        // Determine goal: melee attack, float in range, or pickup rider.
        float distanceSq = Float.POSITIVE_INFINITY;
        if (this.targetedEntity != null) {
            distanceSq = (float)this.targetedEntity.getDistanceSqToEntity(this);
        }
        boolean inRange = false;
        if (this.riddenByEntity != null) {
            if (this.riddenByEntity.isEntityAlive() && this.targetedEntity != null && this.isRiderRanged()) {
                inRange = distanceSq < 64.0;
            }
            this.targetedRider = null;
        }
        else if (this.targetedEntity == null && (this.targetedRider == null || this.targetedRider.ridingEntity != null || !this.targetedRider.isEntityAlive()) && this.rand.nextInt(100) == 0) {
            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(100.0, 100.0, 100.0));
            double closestDistance = Double.POSITIVE_INFINITY;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof EntityLiving) {
                    EntityLiving entity = (EntityLiving)list.get(i);
                    if (entity instanceof IMob && !(entity instanceof EntityFlying) && entity.ridingEntity == null && entity.riddenByEntity == null && entity != this.targetedEntity && this.getEntitySenses().canSee(entity)) {
                        double distance = entity.getDistanceSqToEntity(this);
                        if (distance < closestDistance) {
                            this.targetedRider = entity;
                            closestDistance = distance;
                        }
                    }
                }
            }
        }
        // Perform movement.
        double vX = this.waypointX - this.posX;
        double vY = this.waypointY - this.posY;
        double vZ = this.waypointZ - this.posZ;
        double v = vX * vX + vY * vY + vZ * vZ;
        if (v < 0.1 || v > 3600.0 || inRange != this.prevInRange) {
            if (inRange) {
                this.setRandomWaypoints(4.0F);
            }
            else if (this.targetedRider != null) {
                this.waypointX = this.targetedRider.posX;
                this.waypointY = this.targetedRider.posY + this.targetedRider.height / 2.0F;
                this.waypointZ = this.targetedRider.posZ;
                if (!this.isCourseTraversable(Math.sqrt(v))) {
                    this.setRandomWaypoints(32.0F);
                }
            }
            else if (this.targetedEntity != null) {
                this.waypointX = this.targetedEntity.posX;
                this.waypointY = this.targetedEntity.posY + this.targetedEntity.height / 2.0F;
                this.waypointZ = this.targetedEntity.posZ;
                if (!this.isCourseTraversable(Math.sqrt(v))) {
                    this.setRandomWaypoints(32.0F);
                }
            }
            else {
                this.setRandomWaypoints(32.0F);
                this.waypointY = Math.max(this.waypointY, Math.max(70.0, this.worldObj.getHeightValue((int)Math.floor(this.waypointX), (int)Math.floor(this.waypointZ)) + 16.0));
            }
        }
        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown += this.rand.nextInt(5) + 2;
            v = Math.sqrt(v);
            if (this.isCourseTraversable(v)) {
                double speed = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() / v;
                if (this.targetedEntity == null && this.targetedRider == null) {
                    speed *= 0.3;
                }
                this.motionX += vX * speed;
                this.motionY += vY * speed;
                this.motionZ += vZ * speed;
            }
            else {
                this.setRandomWaypoints(8.0F);
            }
        }
        // Execute goal, if able.
        if (this.attackCounter > 0) {
            this.attackCounter--;
        }
        if (this.targetedRider != null) {
            this.renderYawOffset = this.rotationYaw = (float)Math.atan2(this.targetedRider.posX - this.posX, this.targetedRider.posZ - this.posZ) * -180.0F / (float)Math.PI;

            double reach = this.width * this.width * 4.0F + this.targetedRider.width;
            if (this.getDistanceSq(this.targetedRider.posX, this.targetedRider.posY + this.targetedRider.height / 2.0F, this.targetedRider.posZ) <= reach) {
                this.targetedRider.mountEntity(this);
                this.targetedRider = null;
            }
        }
        else if (this.targetedEntity != null) {
            this.renderYawOffset = this.rotationYaw = (float)Math.atan2(this.targetedEntity.posX - this.posX, this.targetedEntity.posZ - this.posZ) * -180.0F / (float)Math.PI;

            if (this.attackCounter <= 0) {
                double reach = this.width * this.width * 4.0F + this.targetedEntity.width;
                if (this.getDistanceSq(this.targetedEntity.posX, this.targetedEntity.posY + this.targetedEntity.height / 2.0F, this.targetedEntity.posZ) <= reach) {
                    this.attackCounter = 20;
                    this.swingItem();
                    this.attackEntityAsMob(this.targetedEntity);
                }
            }

            if (this.riddenByEntity instanceof EntityLiving) {
                if (this.targetedEntity instanceof EntityLivingBase) {
                    ((EntityLiving)this.riddenByEntity).setAttackTarget((EntityLivingBase)this.targetedEntity);
                }
                if (this.riddenByEntity instanceof EntityCreature) {
                    ((EntityCreature)this.riddenByEntity).setTarget(this.targetedEntity);
                }
            }
        }
        else {
            this.renderYawOffset = this.rotationYaw = -((float)Math.atan2(this.motionX, this.motionZ)) * 180.0F / (float)Math.PI;
        }
        this.prevInRange = inRange;
    }

    /// Updates this entity's target.
    @Override
    protected void updateEntityTarget() {
        if (this.targetedEntity != null && this.targetedEntity.isDead) {
            this.targetedEntity = null;
        }
        if (this.targetedEntity == null || this.aggroCooldown-- <= 0) {
            this.targetedEntity = this.worldObj.getClosestVulnerablePlayerToEntity(this, 100.0);
            if (this.targetedEntity != null && this.dimension == 0) {
                double dX = this.targetedEntity.posX - this.posX;
                double dZ = this.targetedEntity.posZ - this.posZ;
                if (dX * dX + dZ * dZ > 256.0) {
                    this.targetedEntity = null;
                }
            }
            if (this.targetedEntity != null) {
                this.aggroCooldown = 20;
            }
        }
        if (this.targetedRider != null && (this.targetedRider.ridingEntity != null || this.targetedRider.riddenByEntity != null || !this.targetedRider.isEntityAlive())) {
            this.targetedRider = null;
        }
    }

    /// Returns true if the rider has a ranged attack.
    public boolean isRiderRanged() {
        if (this.lastRiddenByEntity != this.riddenByEntity) {
            this.riderIsRanged = this.riddenByEntity instanceof EntityLiving && MobHelper.hasRangedAttack((EntityLiving)this.riddenByEntity);
            this.lastRiddenByEntity = this.riddenByEntity;
        }
        return this.riderIsRanged;
    }

    /// True if the ghast has an unobstructed line of travel to the waypoint.
    @Override
    public boolean isCourseTraversable(double v) {
        double dX = (this.waypointX - this.posX) / v;
        double dY = (this.waypointY - this.posY) / v;
        double dZ = (this.waypointZ - this.posZ) / v;
        AxisAlignedBB aabb;
        /// Check to not suffocate rider.
        if (this.riddenByEntity != null && this.riddenByEntity.isEntityAlive()) {
            aabb = this.riddenByEntity.boundingBox.copy();
            for (int i = 1; i < v; i++) {
                aabb.offset(dX, dY, dZ);
                if (!this.worldObj.getCollidingBoundingBoxes(this.riddenByEntity, aabb).isEmpty())
                    return false;
            }
        }
        /// Check for self.
        aabb = this.boundingBox.copy();
        for (int i = 1; i < v; i++) {
            aabb.offset(dX, dY, dZ);
            if (!this.worldObj.getCollidingBoundingBoxes(this, aabb).isEmpty())
                return false;
        }
        return true;
    }
}