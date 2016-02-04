package toast.specialMobs.entity.pigzombie;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import toast.specialMobs.DataWatcherHelper;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.EntitySpecialFishHook;
import toast.specialMobs.entity.IAngler;

public class EntityFishingPigZombie extends Entity_SpecialPigZombie implements IAngler
{
	/// The position of hasFishingRod within the data watcher.
    private static final byte DW_FISHING_ROD = DataWatcherHelper.instance.PIG_ZOMBIE_FISHING.nextKey();

    /// Ticks until this fishing zombie can cast its lure again.
    public int rodTime = 0;
    /// This fishing zombie's lure entity.
    private EntitySpecialFishHook fishHook = null;

    public EntityFishingPigZombie(World world) {
        super(world);
        this.experienceValue += 2;
    }

    /// Used to intialize dataWatcher variables.
    @Override
    protected void entityInit() {
        super.entityInit();
        /// heldItem; (boolean) If this is is not true, the heldItem is rendered as a stick.
        this.dataWatcher.addObject(EntityFishingPigZombie.DW_FISHING_ROD, Byte.valueOf((byte) 1));
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 0.9);
        this.setCanPickUpLoot(false);

        ItemStack itemStack = new ItemStack(Items.fishing_rod);
        float tension = this.worldObj.func_147462_b(this.posX, this.posY, this.posZ);
        if (this.rand.nextFloat() < 0.25F * tension) {
            try {
            	EnchantmentHelper.addRandomEnchantment(this.rand, itemStack, (int) (5.0F + tension * this.rand.nextInt(18)));
            }
            catch (Exception ex) {
            	_SpecialMobs.console("Error applying enchantments! entity:" + this.toString());
            	ex.printStackTrace();
            }
        }
        this.setCurrentItemOrArmor(0, itemStack);
    }

    /// Set/get functions for this angler's fishHook.
    @Override /// IAngler
    public void setFishHook(EntitySpecialFishHook hook) {
        this.fishHook = hook;
        this.setFishingRod(hook == null);
    }
    @Override /// IAngler
    public EntitySpecialFishHook getFishHook() {
        return this.fishHook;
    }

    /// Get/set functions for heldItem. fishing rod == true, stick == false.
    public void setFishingRod(boolean rod) {
        this.dataWatcher.updateObject(EntityFishingPigZombie.DW_FISHING_ROD, Byte.valueOf(rod ? (byte)1 : (byte)0));
    }
    public boolean getFishingRod() {
        return this.dataWatcher.getWatchableObjectByte(EntityFishingPigZombie.DW_FISHING_ROD) == 1;
    }

    /// Returns the heldItem.
    @Override
    public ItemStack getHeldItem() {
        if (this.worldObj.isRemote && !this.getFishingRod())
            return new ItemStack(Items.stick);
        return super.getHeldItem();
    }

    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if (this.rodTime > 0) {
            this.rodTime--;
        }
        super.onLivingUpdate();
    }

    /// Called each tick this entity's attack target can be seen.
    @Override
    protected void attackEntity(Entity target, float distance) {
        super.attackEntity(target, distance);
        if (!this.worldObj.isRemote && this.rodTime <= 0 && this.getFishingRod()) {
            if (distance > 3.0F && distance < 10.0F) {
                new EntitySpecialFishHook(this.worldObj, this, target);
                this.worldObj.spawnEntityInWorld(this.getFishHook());
                this.worldObj.playSoundAtEntity(this, "random.bow", 0.5F, 0.4F / (this.rand.nextFloat() * 0.4F + 0.8F));
                this.setFishingRod(false);
                this.rodTime = this.rand.nextInt(11) + 32;
            }
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.cooked_fished, 1);
        }
    }
}