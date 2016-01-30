package toast.specialMobs.entity.skeleton;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityFireSkeleton extends Entity_SpecialSkeleton {
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] { new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "skeleton/fire.png"), new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "skeleton/fire_wither.png") };

    public EntityFireSkeleton(World world) {
        super(world);
        this.getNavigator().setAvoidsWater(true);
        this.getSpecialData().setTextures(EntityFireSkeleton.TEXTURES);
        this.getSpecialData().isImmuneToFire = this.isImmuneToFire = true;
        this.getSpecialData().isDamagedByWater = true;
        this.experienceValue += 1;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
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
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.coal, 1);
        }
    }

    /// Returns true if this mob should be rendered on fire.
    @Override
    public boolean isBurning() {
        return this.isEntityAlive() && !this.isWet();
    }
}