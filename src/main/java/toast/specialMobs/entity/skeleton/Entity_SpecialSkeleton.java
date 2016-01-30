package toast.specialMobs.entity.skeleton;

import java.util.UUID;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

public class Entity_SpecialSkeleton extends EntitySkeleton implements ISpecialMob
{
    /// Useful properties for this class.
    private static final double BABY_CHANCE = Properties.getDouble(Properties.STATS, "baby_skeleton_chance");
    private static final double BOW_CHANCE = Properties.getDouble(Properties.STATS, "bow_chance_skeleton");
    private static final double BOW_CHANCE_WITHER = Properties.getDouble(Properties.STATS, "bow_chance_wither");
    /// Baby speed boost modifier.
    private static final UUID babySpeedBoostUUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier babySpeedBoostModifier = new AttributeModifier(Entity_SpecialSkeleton.babySpeedBoostUUID, "Baby speed boost", 0.5, 1);
    /// The position of isBaby within the data watcher.
    private static final byte DW_IS_BABY = 12;

    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation("textures/entity/skeleton/skeleton.png"),
        new ResourceLocation("textures/entity/skeleton/wither_skeleton.png")
    };

    /// Adult width and height.
    private float adultWidth = -1.0F;
    private float adultHeight;

    /// This entity's AI attack patterns.
    public EntityAIArrowAttack aiArrowAttack;
    public EntityAIAttackOnCollide aiAttackOnCollide;

    /// This mob's special mob data.
    private  SpecialMobData specialData;

    public Entity_SpecialSkeleton(World world) {
        super(world);
    }

    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        this.specialData = new SpecialMobData(this, Entity_SpecialSkeleton.TEXTURES);
        super.entityInit();
        this.dataWatcher.addObject(Entity_SpecialSkeleton.DW_IS_BABY, Byte.valueOf((byte) 0));

        this.initTypeAI();
    }

    /// Override to set the attack AI to use.
    protected void initTypeAI() {
        this.setRangedAI(1.0, 20, 60, 15.0F);
        this.setMeleeAI(1.2);
    }

    /// Helper methods to set the attack AI more easily.
    protected void loadRangedAI() {
        this.tasks.removeTask(this.aiArrowAttack);
        SpecialMobData data = this.getSpecialData();
        this.aiArrowAttack = new EntityAIArrowAttack(this, data.arrowMoveSpeed, data.arrowRefireMin, data.arrowRefireMax, data.arrowRange);
        this.setCombatTask();
    }
    protected void setRangedAI(double moveSpeed, int minDelay, int maxDelay, float range) {
        SpecialMobData data = this.getSpecialData();
        data.arrowMoveSpeed = (float) moveSpeed;
        data.arrowRefireMin = (short) minDelay;
        data.arrowRefireMax = (short) maxDelay;
        data.arrowRange = range;
        this.aiArrowAttack = new EntityAIArrowAttack(this, data.arrowMoveSpeed, data.arrowRefireMin, data.arrowRefireMax, data.arrowRange);
    }
    protected void setMeleeAI(double moveSpeed) {
        this.aiAttackOnCollide = new EntityAIAttackOnCollide(this, EntityPlayer.class, moveSpeed, false);
    }

    /// Returns this mob's special data.
    @Override /// ISpecialMob
    public SpecialMobData getSpecialData() {
        return this.specialData;
    }

    /// Called to modify inherited attributes.
    @Override /// ISpecialMob
    public void adjustEntityAttributes() {
        if (this.rand.nextDouble() < Entity_SpecialSkeleton.BABY_CHANCE) {
            this.setChild(true);
        }
        if (this.getSkeletonType() == 1) {
        	this.isImmuneToFire = true;
            if (this.rand.nextDouble() < Entity_SpecialSkeleton.BOW_CHANCE_WITHER) {
                ItemStack itemStack = new ItemStack(Items.bow);
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
        }
        else {
            if (this.rand.nextDouble() >= Entity_SpecialSkeleton.BOW_CHANCE) {
                ItemStack itemStack = new ItemStack(Items.stone_sword);
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
        }

        float prevMax = this.getMaxHealth();
        this.adjustTypeAttributes();
        this.setHealth(this.getMaxHealth() + this.getHealth() - prevMax);
    }

    /// Overridden to modify inherited attribites.
    protected void adjustTypeAttributes() {
        /// Override to alter attributes.
    }

    /// Attack the specified entity using a ranged attack.
    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float range) {
        EntityArrow arrow = new EntityArrow(this.worldObj, this, target, 1.6F, this.getTypeArrowSpread());
        arrow.setDamage(range * this.getSpecialData().arrowDamage + this.rand.nextGaussian() * 0.25 + this.worldObj.difficultySetting.getDifficultyId() * 0.11F);

        int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, this.getHeldItem());
        int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, this.getHeldItem());
        if (power > 0) {
            arrow.setDamage(arrow.getDamage() + power * 0.5 + 0.5);
        }
        if (punch > 0) {
            arrow.setKnockbackStrength(punch);
        }
        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, this.getHeldItem()) > 0 || this.getSkeletonType() == 1) {
            arrow.setFire(100);
        }

        this.playSound("random.bow", 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
        this.worldObj.spawnEntityInWorld(arrow);
    }

    /// Overridden to modify the base arrow variance.
    protected float getTypeArrowSpread() {
        return this.getSpecialData().arrowSpread - this.worldObj.difficultySetting.getDifficultyId() * (this.getSpecialData().arrowSpread / 4.0F + 0.5F);
    }

    /// Called each tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.getSpecialData().onUpdate();
    }

    /// Called when this entity is first spawned to initialize it.
    @Override
    public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
        return this.getSpecialData().onSpawnWithEgg(data, new EntitySkeleton(this.worldObj));
    }

    /// If true, this entity is a baby.
    @Override
    public boolean isChild() {
        return this.getChild();
    }

    /// Returns true if this entity is a baby.
    public boolean getChild() {
        return this.dataWatcher.getWatchableObjectByte(Entity_SpecialSkeleton.DW_IS_BABY) == 1;
    }
    /// Sets this mob as a baby.
    public void setChild(boolean value) {
        this.dataWatcher.updateObject(Entity_SpecialSkeleton.DW_IS_BABY, Byte.valueOf((byte)(value ? 1 : 0)));
        if (this.worldObj != null && !this.worldObj.isRemote) {
            IAttributeInstance attribute = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
            attribute.removeModifier(Entity_SpecialSkeleton.babySpeedBoostModifier);
            if (value) {
                attribute.applyModifier(Entity_SpecialSkeleton.babySpeedBoostModifier);
            }
        }
        this.updateScale(value);
    }

    /// Sets the width and height of the entity.
    @Override
	protected void setSize(float width, float height) {
        boolean alreadyScaled = this.adultWidth > 0.0F && this.adultHeight > 0.0F;
        this.adultWidth = width;
        this.adultHeight = height;

        if (!alreadyScaled) {
            this.updateScale();
        }
    }

    /// Updates the entity size scaled by the normal adult size.
    protected void updateScale() {
    	this.updateScale(this.isChild());
    }
    protected void updateScale(boolean isChild) {
    	float scale = !this.worldObj.isRemote && this.isChild() ? 0.5F : 1.0F;
        super.setSize(this.adultWidth * scale, this.adultHeight * scale);
    }

    /// Called to attack the target.
    @Override
    public boolean attackEntityAsMob(Entity target) {
        this.swingItem();
        if (super.attackEntityAsMob(target)) {
            this.onTypeAttack(target);
            return true;
        }
        return false;
    }

    /// Overridden to modify attack effects.
    protected void onTypeAttack(Entity target) {
        /// Override to alter attack.
    }

    /// Sets this entity's combat AI.
    @Override
    public void setCombatTask() {
        this.tasks.removeTask(this.aiAttackOnCollide);
        this.tasks.removeTask(this.aiArrowAttack);

        ItemStack itemStack = this.getHeldItem();
        if (itemStack != null && itemStack.getItem() instanceof ItemBow) {
            this.tasks.addTask(4, this.aiArrowAttack);
        }
        else {
            this.tasks.addTask(4, this.aiAttackOnCollide);
        }
    }

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        saveTag.setBoolean("IsBaby", this.getChild());

        this.getSpecialData().isImmuneToFire = this.isImmuneToFire;
        this.getSpecialData().writeToNBT(saveTag);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        if (saveTag.hasKey("IsBaby")) {
            this.setChild(saveTag.getBoolean("IsBaby"));
        }
        else if (tag.hasKey("IsBaby")) {
            this.setChild(tag.getBoolean("IsBaby"));
        }

        this.getSpecialData().readFromNBT(tag);
        this.getSpecialData().readFromNBT(saveTag);
        this.isImmuneToFire = this.getSpecialData().isImmuneToFire;
        this.loadRangedAI();
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (_SpecialMobs.debug) {
            this.dropRareDrop(Math.max(0, this.rand.nextInt(5) - 3));
        }
    }

    /// Set this skeleton's type.
    @Override
    public void setSkeletonType(int type) {
        super.setSkeletonType(type);
        this.isImmuneToFire = this.getSpecialData().isImmuneToFire;
    }

    /// Returns the current armor level of this mob.
    @Override
    public int getTotalArmorValue() {
        return Math.min(20, super.getTotalArmorValue() + this.getSpecialData().armor);
    }

    /// Sets this entity on fire.
    @Override
    public void setFire(int time) {
        if (!this.getSpecialData().isImmuneToBurning) {
            super.setFire(time);
        }
    }

    /// Returns the current armor level of this mob.
    @Override
    public boolean allowLeashing() {
        return !this.getLeashed() && this.getSpecialData().allowLeashing;
    }

    /// Sets the entity inside a web block.
    @Override
    public void setInWeb()  {
        if (!this.getSpecialData().isImmuneToWebs) {
            super.setInWeb();
        }
    }

    /// Called when the mob falls. Calculates and applies fall damage.
    @Override
    protected void fall(float distance) {
        if (!this.getSpecialData().isImmuneToFalling) {
            super.fall(distance);
        }
    }

    /// Return whether this entity should NOT trigger a pressure plate or a tripwire.
    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return this.getSpecialData().ignorePressurePlates;
    }

    /// True if the entity can breathe underwater.
    @Override
    public boolean canBreatheUnderwater() {
        return this.getSpecialData().canBreatheInWater;
    }

    /// True if the entity can be pushed by flowing water.
    @Override
    public boolean isPushedByWater() {
        return !this.getSpecialData().ignoreWaterPush;
    }

    /// Returns true if the potion can be applied.
    @Override
    public boolean isPotionApplicable(PotionEffect effect) {
        return this.getSpecialData().isPotionApplicable(effect);
    }
}