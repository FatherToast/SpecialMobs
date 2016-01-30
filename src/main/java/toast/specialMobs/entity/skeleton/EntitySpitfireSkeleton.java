package toast.specialMobs.entity.skeleton;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;

public class EntitySpitfireSkeleton extends Entity_SpecialSkeleton {
    public EntitySpitfireSkeleton(World world) {
        super(world);
        this.getNavigator().setAvoidsWater(true);
        this.stepHeight = 1.0F;
        this.setSize(0.9F, 2.7F);
        this.updateScale();
        this.getSpecialData().resetRenderScale(1.5F);
        this.getSpecialData().setTextures(EntityFireSkeleton.TEXTURES);
        this.getSpecialData().isImmuneToFire = this.isImmuneToFire = true;
        this.getSpecialData().isDamagedByWater = true;
        this.experienceValue += 4;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 20.0);
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 2.0);
        this.getSpecialData().arrowSpread = 2.0F;

        ItemStack itemStack = this.getHeldItem();
        if (itemStack != null) {
            if (itemStack.getItem() instanceof ItemBow) {
                EffectHelper.overrideEnchantment(itemStack, Enchantment.flame, this.rand.nextInt(Enchantment.flame.getMaxLevel()) + 1);
            }
            else {
                EffectHelper.overrideEnchantment(itemStack, Enchantment.fireAspect, this.rand.nextInt(Enchantment.fireAspect.getMaxLevel()) + 1);
            }
        }
    }

    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack(Entity target) {
        if (this.getHeldItem() == null) {
            target.setFire(10);
        }
    }

    /// Attack the specified entity using a ranged attack.
    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float range) {
        double dX = target.posX - this.posX;
        double dY = target.boundingBox.minY + target.height / 2.0F - this.posY - this.height / 2.0F;
        double dZ = target.posZ - this.posZ;
        float spread = (float) Math.sqrt(range) * this.getSpecialData().arrowSpread;
        this.worldObj.playAuxSFXAtEntity((EntityPlayer) null, 1009, (int) this.posX, (int) this.posY, (int) this.posZ, 0);
        EntitySmallFireball fireball;
        for (int i = 0; i < 4; i++) {
	        fireball = new EntitySmallFireball(this.worldObj, this, dX + this.rand.nextGaussian() * spread, dY, dZ + this.rand.nextGaussian() * spread);
	        fireball.posY = this.posY + this.height / 2.0F + 0.5;
	        this.worldObj.spawnEntityInWorld(fireball);
        }
    }

    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.worldObj.isRemote && this.getSkeletonType() == 1) {
            this.setSize(0.95F, 3.24F);
            this.updateScale();
        }
    }

    /// If true, this entity is a baby.
    @Override
    public boolean isChild() {
        return false;
    }

    /// Called when the entity is attacked.
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (damageSource.getSourceOfDamage() instanceof EntitySnowball) {
            damage = Math.max(2.0F, damage);
        }
        return super.attackEntityFrom(damageSource, damage);
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2) + 1; i-- > 0;) {
            this.dropItem(Items.bone, 1);
        }
        for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
            this.dropItem(Items.fire_charge, 1);
        }
    }

    /// Set this skeleton's type.
    @Override
    public void setSkeletonType(int type) {
        super.setSkeletonType(type);
        if (type == 1) {
            this.setSize(0.95F, 3.24F);
        }
        else {
            this.setSize(0.9F, 2.7F);
        }
        this.updateScale();
    }

    /// Returns true if this mob should be rendered on fire.
    @Override
    public boolean isBurning() {
        return this.isEntityAlive() && !this.isWet();
    }
}