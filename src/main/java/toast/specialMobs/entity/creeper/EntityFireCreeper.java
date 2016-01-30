package toast.specialMobs.entity.creeper;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityFireCreeper extends Entity_SpecialCreeper {
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] { new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "creeper/fire.png") };

    public EntityFireCreeper(World world) {
        super(world);
        this.getNavigator().setAvoidsWater(true);
        this.getSpecialData().setTextures(EntityFireCreeper.TEXTURES);
        this.getSpecialData().isImmuneToFire = this.isImmuneToFire = true;
        this.getSpecialData().isDamagedByWater = true;
        this.setCanNotExplodeWhenWet(true);
        this.experienceValue += 1;
    }

    /// The explosion caused by this creeper.
    @Override
    public void explodeByType(boolean powered, boolean griefing) {
        float power = powered ? this.explosionRadius * 2.0F : (float) this.explosionRadius;
        this.worldObj.newExplosion(this, this.posX, this.posY, this.posZ, power, true, griefing);

        if (powered) {
        	if (this.worldObj.getWorldInfo().isRaining()) {
                this.worldObj.getWorldInfo().setRainTime(0);
                this.worldObj.getWorldInfo().setRaining(false);
            }
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
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.fire_charge, 1);
        }
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.coal, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        ItemStack drop = new ItemStack(Items.flint_and_steel);
        drop.addEnchantment(Enchantment.unbreaking, 10);
        this.entityDropItem(drop, 0.0F);
    }
}