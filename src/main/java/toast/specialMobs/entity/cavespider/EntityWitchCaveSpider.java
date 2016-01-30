package toast.specialMobs.entity.cavespider;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityWitchCaveSpider extends Entity_SpecialCaveSpider
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "cavespider/witch.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "cavespider/witch_eyes.png")
    };

    public EntityWitchCaveSpider(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityWitchCaveSpider.TEXTURES);
        for (int id = Potion.potionTypes.length; id-- > 0;) {
            this.getSpecialData().immuneToPotions.add(id);
        }
        this.experienceValue += 2;
    }

    /// Overridden to modify attack effects.
    @Override
    public void onTypeAttack(Entity target) {
        if (target instanceof EntityLivingBase) {
            int time;
            switch (target.worldObj.difficultySetting) {
                case PEACEFUL:
                    return;
                case EASY:
                    time = 15;
                    break;
                case NORMAL:
                    time = 35;
                    break;
                default:
                    time = 75;
            }
            time *= 20;
            ((EntityLivingBase)target).addPotionEffect(new PotionEffect(Potion.resistance.id, time, -3));
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        Item drop = null;
        switch (this.rand.nextInt(5)) {
            case 0:
                drop = Items.redstone;
                break;
            case 1:
                drop = Items.glowstone_dust;
                break;
            case 2:
                drop = Items.ghast_tear;
                break;
            case 3:
                drop = Items.blaze_powder;
                break;
            case 4:
                drop = Items.magma_cream;
                break;
        }
        if (drop != null) {
            this.dropItem(drop, 1);
        }
    }

    /// Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        Entity entity = damageSource.getSourceOfDamage();
        if (damageSource.isProjectile() && entity != null) {
            Entity deflected = null;
            String entityName = EntityList.getEntityString(entity);
            if (entityName != null) {
                deflected = EntityList.createEntityByName(entityName, this.worldObj);
            }
            if (deflected != null) {
                NBTTagCompound tag = new NBTTagCompound();
                entity.writeToNBT(tag);
                deflected.readFromNBT(tag);

                if (entity instanceof EntityArrow) {
                    ((EntityArrow)deflected).shootingEntity = this;
                }
                else if (entity instanceof EntityFireball) {
                    ((EntityFireball)deflected).shootingEntity = this;
                    ((EntityFireball)deflected).accelerationX *= -1.0;
                    ((EntityFireball)deflected).accelerationY *= -1.0;
                    ((EntityFireball)deflected).accelerationZ *= -1.0;
                }
                deflected.setLocationAndAngles(deflected.posX, deflected.posY, deflected.posZ, -deflected.rotationYaw, -deflected.rotationPitch);
                deflected.motionX *= -1.0;
                deflected.motionY *= -1.0;
                deflected.motionZ *= -1.0;

                this.worldObj.playSoundAtEntity(this, "random.orb", 0.3F, 1.0F);
                if (!this.worldObj.isRemote) {
                    this.worldObj.spawnEntityInWorld(deflected);
                }
                entity.setDead();
                return false;
            }
        }
        return super.attackEntityFrom(damageSource, damage);
    }
}