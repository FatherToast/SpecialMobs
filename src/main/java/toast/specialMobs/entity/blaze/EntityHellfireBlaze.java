package toast.specialMobs.entity.blaze;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityHellfireBlaze extends Entity_SpecialBlaze
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "blaze/hellfire.png")
    };

    /// The base explosion strength of this blaze's fireballs.
    public int explosionStrength = 2;

    public EntityHellfireBlaze(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityHellfireBlaze.TEXTURES);
        this.experienceValue += 1;
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 10.0);
        this.setRangedAI(1, 0, 60, 100, 40.0F);
        this.getSpecialData().arrowSpread = 0.0F;
    }

    // Called to attack the target entity with a fireball.
    @Override
	public void shootFireballAtEntity(Entity target, float distance) {
        double dX = target.posX - this.posX;
        double dY = target.boundingBox.minY + target.height / 2.0F - this.posY - this.height / 2.0F;
        double dZ = target.posZ - this.posZ;
        float spread = (float) Math.sqrt(distance) * this.getSpecialData().arrowSpread;
        this.worldObj.playAuxSFXAtEntity((EntityPlayer) null, 1009, (int) this.posX, (int) this.posY, (int) this.posZ, 0);
        EntityLargeFireball fireball = new EntityLargeFireball(this.worldObj, this, dX + this.rand.nextGaussian() * spread, dY, dZ + this.rand.nextGaussian() * spread);
        fireball.field_92057_e = this.explosionStrength;
        fireball.posY = this.posY + this.height / 2.0F + 0.5;
        this.worldObj.spawnEntityInWorld(fireball);
    }

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("ExplosionPower", this.explosionStrength);
    }
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasKey("ExplosionPower")) {
            this.explosionStrength = tag.getInteger("ExplosionPower");
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit) {
            for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
                this.dropItem(Items.gunpowder, 1);
            }
        }
    }
}