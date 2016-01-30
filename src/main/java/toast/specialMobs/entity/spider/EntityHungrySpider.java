package toast.specialMobs.entity.spider;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityHungrySpider extends Entity_SpecialSpider
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "spider/hungry.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "spider/hungry_eyes.png")
    };

    /// The feeding level of this hungry spider.
    private byte feedingLevel;
    /// The amount of times this hungry spider has gained health.
    private int gainedHealth;
    /// The items this spider has eaten.
    private final ArrayList<ItemStack> stomach = new ArrayList<ItemStack>();

    public EntityHungrySpider(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityHungrySpider.TEXTURES);
        this.getSpecialData().resetRenderScale(0.8F);
        this.experienceValue += 4;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 4.0);
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, -1.0);
        this.getSpecialData().setHealTime(40);
        this.getSpecialData().arrowRange = 0.0F;
        this.setCanPickUpLoot(true);
    }

    /// Overridden to modify attack effects.
    @Override
    public void onTypeAttack(Entity target) {
        if (target instanceof EntityPlayer) {
    		ItemStack drop = MobHelper.removeRandomItem((EntityPlayer) target);
    		if (drop != null) {
		    	if (this.canPickUpLoot()) {
					this.setCurrentItemOrArmor(0, drop);
				}
		    	else {
					this.entityDropItem(drop, 0.0F);
	                this.worldObj.playSoundAtEntity(this, "random.burp", 0.5F, this.rand.nextFloat() * 0.1F + 0.9F);
				}
        	}
        }
    }

    /// Sets the held item, or an armor slot.
    @Override
    public void setCurrentItemOrArmor(int slot, ItemStack itemStack) {
        if (this.worldObj.isRemote)
        	return;
		if (itemStack != null && this.gainedHealth < 64) {
            this.gainedHealth++;
            float maxHealth = this.getMaxHealth();
            this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 4.0);
            this.setHealth(this.getHealth() + this.getMaxHealth() - maxHealth);
            if (this.feedingLevel < 7) {
                this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 1.0);
                this.getSpecialData().arrowDamage += 0.5F;
            }
            this.setFeedingLevel(this.feedingLevel + 1, true);
        }
		if (this.gainedHealth >= 64) {
			this.setCanPickUpLoot(false);
		}

		if (itemStack != null) {
	        if (itemStack.getItem() instanceof ItemFood) {
	            this.heal(((ItemFood) itemStack.getItem()).func_150905_g(itemStack));
	        }
	        else {
	            this.stomach.add(itemStack);
	        }
	        this.worldObj.playSoundAtEntity(this, "random.burp", 0.5F, this.rand.nextFloat() * 0.1F + 0.9F);
		}
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.apple, 1);
        }

        for (ItemStack itemStack : this.stomach) {
            this.entityDropItem(itemStack, 0.0F);
        }
        this.stomach.clear();
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        ItemStack itemStack = new ItemStack(Items.potionitem, 1, 8196);
        EffectHelper.setItemName(itemStack, "Potion of Hunger", 0xf);
        EffectHelper.addPotionEffect(itemStack, Potion.damageBoost, 1200, 0);
        EffectHelper.addPotionEffect(itemStack, Potion.regeneration, 1200, 0);
        EffectHelper.addPotionEffect(itemStack, Potion.hunger, 600, 1);
        this.entityDropItem(itemStack, 0.0F);
    }

    /// Sets the feeding level of this hungry spider.
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
            this.setSize(1.0F + 0.12857F * level, 0.8F + 0.07142F * level);
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
        tag.setByte("GrowCount", (byte) this.gainedHealth);

        NBTTagList tagList = new NBTTagList();
        NBTTagCompound tagItem;
        for (ItemStack itemStack : this.stomach) {
            tagItem = new NBTTagCompound();
            itemStack.writeToNBT(tagItem);
            tagList.appendTag(tagItem);
        }
        tag.setTag("Stomach", tagList);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasKey("FeedLevel")) {
            this.setFeedingLevel(tag.getByte("FeedLevel"), false);
        }
        if (tag.hasKey("GrowCount")) {
            this.gainedHealth = tag.getByte("GrowCount") & 0xff;
        }

        if (tag.hasKey("Stomach")) {
            NBTTagList tagList = tag.getTagList("Stomach", new NBTTagCompound().getId());
            this.stomach.clear();
            for (int i = 0; i < tagList.tagCount(); i++) {
                this.stomach.add(ItemStack.loadItemStackFromNBT(tagList.getCompoundTagAt(i)));
            }
        }
    }
}