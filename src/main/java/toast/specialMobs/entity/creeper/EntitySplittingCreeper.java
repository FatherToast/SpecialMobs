package toast.specialMobs.entity.creeper;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntitySplittingCreeper extends Entity_SpecialCreeper
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "creeper/splitting.png")
    };

    /// The number of extra mini creepers spawned on explosion.
    private byte babies;

    public EntitySplittingCreeper(World world) {
        super(world);
        this.getSpecialData().setTextures(EntitySplittingCreeper.TEXTURES);
        this.setExplodesWhenShot(true);
        this.experienceValue += 2;
        this.babies = (byte) this.rand.nextInt(4);
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 20.0);
    }

    /// The explosion caused by this creeper.
    @Override
    public void explodeByType(boolean powered, boolean griefing) {
        float power = powered ? this.explosionRadius * 2.0F : (float)this.explosionRadius;
        this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, power - 1.0F, false);
        if (!this.worldObj.isRemote) {
            EntityMiniCreeper baby = null;
            for (int i = this.babies + (int) power; i-- > 0;) {
                baby = new EntityMiniCreeper(this.worldObj);
                baby.copyLocationAndAnglesFrom(this);
                baby.setAttackTarget(this.getAttackTarget());
                baby.onSpawnWithEgg((IEntityLivingData)null);
                baby.motionX = (this.rand.nextDouble() - 0.5) * power / 3.0;
                ///baby.motionY = 0.3 + 0.3 * rand.nextDouble(); Causes floor clip bug
                baby.motionZ = (this.rand.nextDouble() - 0.5) * power / 3.0;
                baby.onGround = false;
                if (powered) {
                    baby.getDataWatcher().updateObject(17, Byte.valueOf((byte)1));
                }
                this.worldObj.spawnEntityInWorld(baby);
            }
            this.worldObj.playSoundAtEntity(baby, "random.pop", 1.0F, 2.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
            this.dropItem(Items.gunpowder, 1);
        }
    }

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setByte("Babies", this.babies);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasKey("Babies")) {
            this.babies = tag.getByte("Babies");
        }
    }
}