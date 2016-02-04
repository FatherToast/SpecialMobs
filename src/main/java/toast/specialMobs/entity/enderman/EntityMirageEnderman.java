package toast.specialMobs.entity.enderman;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.SpecialMobData;

public class EntityMirageEnderman extends Entity_SpecialEnderman
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "enderman/mirage.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "enderman/mirage_eyes.png")
    };

    /// Whether this mirage enderman is fake.
    public boolean isFake = false;
    /// How long this mirage enderman has been alive if fake.
    private int ticksAlive = 0;

    public EntityMirageEnderman(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityMirageEnderman.TEXTURES);
        this.experienceValue += 2;
    }

    /// Sets this mirage enderman to be fake.
    public void setFake() {
        this.isFake = true;
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(0.0);
        this.experienceValue = 0;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 20.0);
    }

    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack(Entity target) {
        if (!this.isFake) {
            for (int i = 64; i-- > 0;) if (this.teleportRandomly()) {
                break;
            }
        }
    }

    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.isFake) {
            if (this.ticksAlive++ > 200) {
                this.setDead();
            }
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (!this.isFake) {
            super.dropFewItems(hit, looting);
            if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
                this.entityDropItem(new ItemStack(Blocks.monster_egg, 1, this.rand.nextInt(3)), 0.0F);
            }
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        if (!this.isFake) {
            ItemStack drop = new ItemStack(Items.golden_hoe);
            EffectHelper.setItemName(drop, "Ender Scythe", 13);
            drop.addEnchantment(Enchantment.looting, 5);
            this.entityDropItem(drop, 0.0F);
        }
    }

    /// Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (this.isFake) {
            this.setHealth(0);
            return true;
        }
        return super.attackEntityFrom(damageSource, damage);
    }

    /// Teleports this enderman to the given coordinates. Returns true if this entity teleports.
    @Override
    protected boolean teleportTo(double x, double y, double z) {
        EntityMirageEnderman mirage = null;
        double xI = 0.0;
        double yI = 0.0;
        double zI = 0.0;
        if (!this.isFake && this.entityToAttack != null) {
            mirage = new EntityMirageEnderman(this.worldObj);
            mirage.setFake();
            mirage.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(this.getEntityAttribute(SharedMonsterAttributes.maxHealth).getAttributeValue());
            mirage.setHealth(this.getHealth());
            mirage.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            mirage.setTarget(this.entityToAttack);
            xI = this.posX;
            yI = this.posY;
            zI = this.posZ;
        }
        if (super.teleportTo(x, y, z)) {
            if (mirage != null) {
                if (this.rand.nextInt(2) == 0) {
                    mirage.setPosition(x, y, z);
                    this.setPosition(xI, yI, zI);
                }
                this.worldObj.spawnEntityInWorld(mirage);
            }
            return true;
        }
        return false;
    }

    /// Saves the entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        saveTag.setBoolean("IsFake", this.isFake);
    }

    /// Loads the entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        if (saveTag.getBoolean("IsFake")) {
            this.setFake();
        }
        else if (tag.getBoolean("IsFake")) {
            this.setFake();
        }
    }
}