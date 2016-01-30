package toast.specialMobs.entity.blaze;

import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityJoltBlaze extends Entity_SpecialBlaze
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "blaze/jolt.png")
    };

    public EntityJoltBlaze(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityJoltBlaze.TEXTURES);
        this.experienceValue += 2;
    }

    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if (!this.worldObj.isRemote && this.isEntityAlive() && this.entityToAttack != null && this.rand.nextInt(10) == 0 && this.entityToAttack.getDistanceSqToEntity(this) > 256.0) {
            this.teleportToEntity(this.entityToAttack);
            this.attackTime = Math.max(20, this.attackTime);
        }
        super.onLivingUpdate();
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit) {
            for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
                this.dropItem(Items.redstone, 1);
            }
        }
    }

    /// Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
    	boolean hit = damageSource instanceof EntityDamageSourceIndirect ? true : super.attackEntityFrom(damageSource, damage);
    	if (!this.worldObj.isRemote && !DamageSource.drown.damageType.equals(damageSource.damageType) && this.getHealth() > 0.0F) {
			for (int i = 0; i < 64; i++) {
			    if (this.teleportRandomly()) {
			        break;
			    }
			}
		}
        return hit;
    }

    /// Teleports this enderman to a random nearby location. Returns true if this entity teleports.
    protected boolean teleportRandomly() {
        double x = this.posX + (this.rand.nextDouble() - 0.5) * 16.0;
        double y = this.posY + (this.rand.nextInt(12) - 4);
        double z = this.posZ + (this.rand.nextDouble() - 0.5) * 16.0;
        return this.teleportTo(x, y, z, false);
    }

    /// Teleports this enderman to the given entity. Returns true if this entity teleports.
    protected boolean teleportToEntity(Entity entity) {
        double x = entity.posX + (this.rand.nextDouble() - 0.5) * 8.0;
        double y = entity.posY + this.rand.nextInt(8) - 2;
        double z = entity.posZ + (this.rand.nextDouble() - 0.5) * 8.0;
        return this.teleportTo(x, y, z, true);
    }

    /// Teleports this enderman to the given coordinates. Returns true if this entity teleports.
    protected boolean teleportTo(double x, double y, double z, boolean strikeDestination) {
        double xI = this.posX;
        double yI = this.posY;
        double zI = this.posZ;
        this.setPosition(x, y, z);
        if (this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() != 0 || this.worldObj.isAnyLiquid(this.boundingBox)) {
            this.setPosition(xI, yI, zI);
            return false;
        }
        for (int i = 0; i < 128; i++) {
            double posRelative = i / 127.0;
            float vX = (this.rand.nextFloat() - 0.5F) * 0.2F;
            float vY = (this.rand.nextFloat() - 0.5F) * 0.2F;
            float vZ = (this.rand.nextFloat() - 0.5F) * 0.2F;
            double dX = xI + (this.posX - xI) * posRelative + (this.rand.nextDouble() - 0.5) * this.width * 2.0;
            double dY = yI + (this.posY - yI) * posRelative + this.rand.nextDouble() * this.height;
            double dZ = zI + (this.posZ - zI) * posRelative + (this.rand.nextDouble() - 0.5) * this.width * 2.0;
            this.worldObj.spawnParticle("smoke", dX, dY, dZ, vX, vY, vZ);
        }
        if (strikeDestination) {
        	MobHelper.lightningExplode(this, 0);
        }
        else {
        	this.setPosition(xI, yI, zI);
        	MobHelper.lightningExplode(this, 0);
        	this.setPosition(x, y, z);
        }
        return true;
    }
}