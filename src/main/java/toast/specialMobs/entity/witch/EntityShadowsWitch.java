package toast.specialMobs.entity.witch;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.SpecialMobData;

public class EntityShadowsWitch extends Entity_SpecialWitch
{
    @SuppressWarnings("hiding")
	public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "witch/shadows.png")
    };

    /// The number of spiders this witch can spawn.
    public byte spiderCount;

    public EntityShadowsWitch(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityShadowsWitch.TEXTURES);
        this.getSpecialData().immuneToPotions.add(Potion.blindness.id);
    }

    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if (!this.worldObj.isRemote && this.isEntityAlive() && this.getAttackTarget() != null && this.rand.nextInt(10) == 0 && this.getAttackTarget().isPotionActive(Potion.blindness)) {
        	this.getAttackTarget().removePotionEffect(Potion.nightVision.id);
        }
        super.onLivingUpdate();
    }

    /// Changes the default splash potion into another befitting the situation.
    @Override
	protected EntityPotion adjustSplashPotion(EntityPotion thrownPotion, EntityLivingBase target, float range, float distance) {
        if (this.adjustSplashPotionByType(thrownPotion, target, range, distance))
        	return thrownPotion;
        if (target.getHealth() >= 4.0F && !target.isPotionActive(Potion.blindness))
			return new EntityPotion(this.worldObj, this, this.makeShadowPotion());
        if (target.getHealth() >= 8.0F && !target.isPotionActive(Potion.poison)) {
            thrownPotion.setPotionDamage(16388); // Splash Poison
            return thrownPotion;
        }
        if (distance <= 3.0F && !target.isPotionActive(Potion.weakness) && this.rand.nextFloat() < 0.25F) {
            thrownPotion.setPotionDamage(16392); // Splash Weakness
            return thrownPotion;
        }
        return thrownPotion;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
			this.dropItem(Items.dye, 1); // Ink sac
		}
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        this.entityDropItem(this.makeShadowPotion(), 0.0F);
    }

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        saveTag.setByte("Spiders", this.spiderCount);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        if (saveTag.hasKey("Spiders")) {
            this.spiderCount = saveTag.getByte("Spiders");
        }
        else if (tag.hasKey("Spiders")) {
            this.spiderCount = tag.getByte("Spiders");
        }
    }

    private ItemStack makeShadowPotion() {
    	ItemStack potion = new ItemStack(Items.potionitem, 1, 16396);
        EffectHelper.setItemName(potion, "Splash Potion of Blindness", 0xf);
        EffectHelper.addPotionEffect(potion, Potion.blindness, 200, 0);
        return potion;
    }
}