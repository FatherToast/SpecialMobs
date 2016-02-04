package toast.specialMobs.entity.zombie;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.MobHelper;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

public class Entity_SpecialZombie extends EntityZombie implements ISpecialMob, IRangedAttackMob
{
    /// Useful properties for this class.
    private static final double INFECT_CHANCE = Properties.getDouble(Properties.STATS, "villager_infection");
    private static final double BOW_CHANCE = Properties.getDouble(Properties.STATS, "bow_chance_zombie");

    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation("textures/entity/zombie/zombie.png"),
        new ResourceLocation("textures/entity/zombie/zombie_villager.png")
    };

    /// This entity's AI attack patterns.
    public EntityAIArrowAttack aiArrowAttack;
    public EntityAIAttackOnCollide[] aiAttackOnCollide;

    /// This mob's special mob data.
    private SpecialMobData specialData;

    public Entity_SpecialZombie(World world) {
        super(world);
        MobHelper.clearMeleeAttackAI(this);
        if (world != null && !world.isRemote) {
            this.setCombatTask();
        }
    }

    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        this.specialData = new SpecialMobData(this, Entity_SpecialZombie.TEXTURES);
        super.entityInit();
        this.initTypeAI();
    }

    /// Override to set the attack AI to use.
    protected void initTypeAI() {
        this.setRangedAI(0.9, 23, 70, 12.0F);
        this.setMeleeAI(1.0);
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
        this.aiAttackOnCollide = new EntityAIAttackOnCollide[] {
                new EntityAIAttackOnCollide(this, EntityPlayer.class, moveSpeed, false),
                new EntityAIAttackOnCollide(this, EntityVillager.class, moveSpeed, true)
        };
    }

    /// Returns this mob's special data.
    @Override /// ISpecialMob
    public SpecialMobData getSpecialData() {
        return this.specialData;
    }

    /// Called to modify inherited attributes.
    @Override /// ISpecialMob
    public void adjustEntityAttributes() {
        if (this.rand.nextDouble() < Entity_SpecialZombie.BOW_CHANCE) {
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
        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, this.getHeldItem()) > 0) {
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
        return this.getSpecialData().onSpawnWithEgg(data, new EntityZombie(this.worldObj));
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

    /// This method gets called when the entity kills another one.
    @Override
    public void onKillEntity(EntityLivingBase entity) {
        if (!this.worldObj.isRemote && entity instanceof EntityVillager && this.rand.nextDouble() < Entity_SpecialZombie.INFECT_CHANCE){
            Entity_SpecialZombie zombie = null;
            try {
                zombie = this.getClass().getConstructor(new Class[] { World.class }).newInstance(new Object[] { this.worldObj });
            }
            catch (Exception ex) {
                _SpecialMobs.debugException("Error infecting villager! " + ex.getClass().getName() + " @" + this.getClass().getName());
                return;
            }
            zombie.copyLocationAndAnglesFrom(entity);
            this.worldObj.spawnEntityInWorld(zombie);
            zombie.onSpawnWithEgg((IEntityLivingData)null);
            zombie.setVillager(true);
            zombie.setChild(entity.isChild());
            this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1016, (int)this.posX, (int)this.posY, (int)this.posZ, 0);
            entity.setDead();
        }
    }

    /// Sets this entity's combat AI.
    public void setCombatTask() {
        this.tasks.removeTask(this.aiAttackOnCollide[0]);
        this.tasks.removeTask(this.aiAttackOnCollide[1]);
        this.tasks.removeTask(this.aiArrowAttack);

        ItemStack itemStack = this.getHeldItem();
        if (itemStack != null && itemStack.getItem() instanceof ItemBow) {
            this.tasks.addTask(2, this.aiArrowAttack);
        }
        else {
            this.tasks.addTask(2, this.aiAttackOnCollide[0]);
            this.tasks.addTask(3, this.aiAttackOnCollide[1]);
        }
    }

    /// Sets the held item, or an armor slot.
    @Override
    public void setCurrentItemOrArmor(int slot, ItemStack itemStack) {
        super.setCurrentItemOrArmor(slot, itemStack);
        if (!this.worldObj.isRemote && slot == 0) {
            this.setCombatTask();
        }
    }

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        this.getSpecialData().isImmuneToFire = this.isImmuneToFire;
        this.getSpecialData().writeToNBT(saveTag);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        this.getSpecialData().readFromNBT(tag);
        this.getSpecialData().readFromNBT(saveTag);
        this.isImmuneToFire = this.getSpecialData().isImmuneToFire;
        this.loadRangedAI();
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (this.getHeldItem() != null && this.getHeldItem().getItem() instanceof ItemBow) {
            for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
                this.dropItem(Items.arrow, 1);
            }
        }
        if (_SpecialMobs.debug) {
            this.dropRareDrop(Math.max(0, this.rand.nextInt(5) - 3));
        }
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