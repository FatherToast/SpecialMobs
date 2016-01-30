package toast.specialMobs.entity.slime;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityCaramelSlime extends Entity_SpecialSlime
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "slime/caramel.png")
    };

    public EntityCaramelSlime(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityCaramelSlime.TEXTURES);
    }

    /// Gets the additional experience this slime type gives.
    @Override
	protected int getTypeXp() {
    	return 2;
    }

    /// Overridden to modify inherited max health.
    @Override
	protected void adjustHealthAttribute() {
    	this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 4.0);
    }

    /// Called each tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.riddenByEntity != null) {
        	this.riddenByEntity.attackEntityFrom(DamageSource.causeThornsDamage(this), 1.0F);
        	this.attackTime = 20;
        }
    }

    /// Overridden to modify attack effects.
    @Override
	protected void onTypeAttack(Entity target) {
    	if (this.riddenByEntity == null || target.ridingEntity instanceof EntityCaramelSlime) {
			target.mountEntity(this);
		}
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (this.getSlimeSize() == 1) {
	        for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
	            this.dropItem(Items.sugar, 1);
	        }
	        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
	            this.entityDropItem(new ItemStack(Items.dye, 1, 14), 0.0F);
	        }
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        if (this.getSlimeSize() == 1) {
	        ItemStack drop = new ItemStack(Items.stick);
	        EffectHelper.setItemName(drop, "Sticky Sword");
	        drop.addEnchantment(Enchantment.sharpness, 3);
	        this.entityDropItem(drop, 0.0F);
        }
    }
}