package toast.specialMobs.entity.ghast;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

public class EntityMeleeGhast extends Entity_SpecialGhast
{
    public EntityMeleeGhast(World world) {
        super(world);
    }

    /// Updates the current goal.
    @Override
    protected void updateEntityGoal() {
        // Perform movement.
        double vX = this.waypointX - this.posX;
        double vY = this.waypointY - this.posY;
        double vZ = this.waypointZ - this.posZ;
        double v = vX * vX + vY * vY + vZ * vZ;
        if (v < 0.1 || v > 3600.0) {
            if (this.targetedEntity != null) {
                this.waypointX = this.targetedEntity.posX;
                this.waypointY = this.targetedEntity.posY + this.targetedEntity.height / 2.0F;
                this.waypointZ = this.targetedEntity.posZ;
                if (!this.isCourseTraversable(Math.sqrt(v))) {
                    this.setRandomWaypoints(32.0F);
                }
            }
            else {
                this.setRandomWaypoints(32.0F);
            }
        }
        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown += this.rand.nextInt(5) + 2;
            v = Math.sqrt(v);
            if (this.isCourseTraversable(v)) {
                double speed = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() / v;
                this.motionX += vX * speed;
                this.motionY += vY * speed;
                this.motionZ += vZ * speed;
            }
            else {
                this.setRandomWaypoints(8.0F);
            }
        }
        // Update the current target.
        this.updateEntityTarget();
        // Execute goal, if able.
        if (this.attackCounter > 0) {
            this.attackCounter--;
        }
        if (this.targetedEntity != null) {
            this.renderYawOffset = this.rotationYaw = -((float)Math.atan2(this.targetedEntity.posX - this.posX, this.targetedEntity.posZ - this.posZ)) * 180.0F / (float)Math.PI;
            if (this.attackCounter <= 0) {
                double reach = this.width * this.width * 4.0F + this.targetedEntity.width;
                if (this.getDistanceSq(this.targetedEntity.posX, this.targetedEntity.posY + this.targetedEntity.height / 2.0F, this.targetedEntity.posZ) <= reach) {
                    this.attackCounter = 20;
                    this.swingItem();
                    this.attackEntityAsMob(this.targetedEntity);
                }
            }
        }
        else {
            this.renderYawOffset = this.rotationYaw = -((float)Math.atan2(this.motionX, this.motionZ)) * 180.0F / (float)Math.PI;
        }
    }
}