package toast.specialMobs.entity.blaze;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.SpecialMobData;

public class EntityConflagrationBlaze extends Entity_SpecialBlaze
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "blaze/conflagration.png")
    };

    /// The feeding level of this conflagration blaze.
    private byte feedingLevel;

    public EntityConflagrationBlaze(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityConflagrationBlaze.TEXTURES);
        this.experienceValue += 4;
    }

    /// Called when the entity is attacked.
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (!damageSource.isFireDamage() && !damageSource.isExplosion() && !damageSource.isMagicDamage() && !DamageSource.drown.damageType.equals(damageSource.damageType) && !(damageSource.getSourceOfDamage() instanceof EntitySnowball)) {
            damage = Math.min(MobHelper.isCritical(damageSource) ? 2.0F : 1.0F, damage);
        	if (!this.worldObj.isRemote && this.feedingLevel < 7) {
	            this.setFeedingLevel(this.feedingLevel + 1, true);
	            SpecialMobData data = this.getSpecialData();
	            data.addAttribute(SharedMonsterAttributes.attackDamage, 1.0);
	            data.arrowDamage += 0.5F;
	            data.arrowRefireMin -= 4;
	            data.arrowRefireMax -= 4;
	            if (this.feedingLevel == 7) {
					this.fireballBurstCount++;
				}
        	}
        }
    	return super.attackEntityFrom(damageSource, damage);
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit) {
	        for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
	            this.dropItem(Items.fire_charge, 1);
	        }
	        if (this.rand.nextInt(5) == 0 || this.rand.nextInt(1 + looting) > 0) {
		        ItemStack itemStack = new ItemStack(Items.potionitem, 1, 8195);
		        EffectHelper.addPotionEffect(itemStack, Potion.fireResistance, 600, 0);
		        this.entityDropItem(itemStack, 0.0F);
	        }
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        ItemStack drop = new ItemStack(Items.wooden_sword);
        EffectHelper.setItemName(drop, "Pyre", 0xd);
        drop.addEnchantment(Enchantment.fireAspect, 10);
        drop.addEnchantment(Enchantment.unbreaking, 6);
        this.entityDropItem(drop, 0.0F);
    }

    /// Sets the feeding level of this conflagration blaze.
    private void setFeedingLevel(int level, boolean updateScale) {
        if (level < 0) {
            level = 0;
        }
        else if (level > 7) {
            level = 7;
        }
        int diff = level - this.feedingLevel;
        if (diff != 0) {
            this.feedingLevel = (byte) level;
            this.setSize(0.8F + 0.01429F * level, 1.8F + 0.12857F * level);
            if (updateScale) {
                this.getSpecialData().setRenderScale(this.getSpecialData().getRenderScale() + 0.1F * diff);
            }
        }
    }

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setByte("FeedLevel", this.feedingLevel);
    }
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasKey("FeedLevel")) {
            this.setFeedingLevel(tag.getByte("FeedLevel"), false);
        }
    }
}