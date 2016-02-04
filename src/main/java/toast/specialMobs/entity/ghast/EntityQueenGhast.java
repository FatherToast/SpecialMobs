package toast.specialMobs.entity.ghast;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.SpecialMobData;

public class EntityQueenGhast extends Entity_SpecialGhast
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "ghast/queen.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "ghast/queen_shooting.png")
    };

    /// The number of babies this ghast can spawn.
    public byte babyCount;
    /// The number of babies spawned on death.
    private byte babies;

    public EntityQueenGhast(World world) {
        super(world);
        this.setSize(5.0F, 5.0F);
        this.getSpecialData().setTextures(EntityQueenGhast.TEXTURES);
        this.getSpecialData().resetRenderScale(1.25F);
        this.experienceValue += 2;
        this.babyCount = (byte) (this.rand.nextInt(7) + 4);
        this.babies = (byte) (3 + this.rand.nextInt(4));
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 30.0);
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 4.0);
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 0.3);
        this.getSpecialData().setHealTime(20);
        this.getSpecialData().armor += 6;
    }

    /// Called to attack the target entity with a fireball.
    @Override
    public void shootFireballAtEntity(Entity target) {
        if (this.babyCount > 0 && this.rand.nextInt(3) != 0) {
            this.babyCount--;
            EntityBabyGhast baby = new EntityBabyGhast(this.worldObj);
            baby.copyLocationAndAnglesFrom(this);
            baby.targetedEntity = this.targetedEntity;
            baby.onSpawnWithEgg((IEntityLivingData)null);
            this.worldObj.spawnEntityInWorld(baby);
            this.worldObj.playSoundAtEntity(baby, "random.pop", 1.0F, 2.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
        }
        else {
            super.shootFireballAtEntity(target);
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.gold_ingot, 1);
        }
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.emerald, 1);
        }
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.entityDropItem(new ItemStack(Items.spawn_egg, 1, EntityList.getEntityID(new EntityGhast(this.worldObj))), 0.0F);
        }

        if (!this.worldObj.isRemote) {
            EntityBabyGhast baby = null;
            for (int i = this.babies; i-- > 0;) {
                baby = new EntityBabyGhast(this.worldObj);
                baby.copyLocationAndAnglesFrom(this);
                baby.targetedEntity = this.targetedEntity;
                baby.onSpawnWithEgg((IEntityLivingData)null);
                this.worldObj.spawnEntityInWorld(baby);
            }
            if (baby != null) {
            	this.worldObj.playSoundAtEntity(baby, "random.pop", 1.0F, 2.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
                baby.spawnExplosionParticle();
            }
        }
    }

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        saveTag.setByte("BabyCount", this.babyCount);
        saveTag.setByte("Babies", this.babies);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        if (saveTag.hasKey("BabyCount")) {
            this.babyCount = saveTag.getByte("BabyCount");
        }
        else if (tag.hasKey("BabyCount")) {
            this.babyCount = tag.getByte("BabyCount");
        }
        if (saveTag.hasKey("Babies")) {
            this.babies = saveTag.getByte("Babies");
        }
        else if (tag.hasKey("Babies")) {
            this.babies = tag.getByte("Babies");
        }
    }
}