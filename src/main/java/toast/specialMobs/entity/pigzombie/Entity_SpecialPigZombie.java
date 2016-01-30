package toast.specialMobs.entity.pigzombie;

import java.util.UUID;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

public class Entity_SpecialPigZombie extends EntityPigZombie implements ISpecialMob, IRangedAttackMob {
    /// Useful properties for this class.
    private static final double HOSTILE_CHANCE = Properties.getDouble(Properties.STATS, "hostile_pigzombies");
    private static final double BOW_CHANCE = Properties.getDouble(Properties.STATS, "bow_chance_pigzombie");
    /// Attacking speed boost modifier.
    private static final UUID stopModifierUUID = UUID.fromString("70A57A49-7EC5-45BA-B886-3B90B23A1718");
    private static final AttributeModifier stopModifier = new AttributeModifier(Entity_SpecialPigZombie.stopModifierUUID, "Attacking speed boost", -1.0, 2).setSaved(false);

    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] { new ResourceLocation("textures/entity/zombie_pigman.png") };

    /// Extra data to make sure anger level is saved.
    public boolean isHostile;

    /// Ticks until this pig zombie stops moving once in range for a bow attack.
    public byte sightDelay = 20;
    /// True if the pig zombie can see its target (used for bow pigmen).
    public boolean seesTarget = false;

    /// This mob's special mob data.
    private SpecialMobData specialData;

    public Entity_SpecialPigZombie(World world) {
        super(world);
        this.getSpecialData().isImmuneToFire = this.isImmuneToFire;
        this.setRangedAI(20, 60, 13.0F);
    }

    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        this.specialData = new SpecialMobData(this, Entity_SpecialPigZombie.TEXTURES);
        super.entityInit();
    }

    /// Helper method to set the attack AI more easily.
    protected void setRangedAI(int minDelay, int maxDelay, float range) {
        SpecialMobData data = this.getSpecialData();
        data.arrowRefireMin = (short) minDelay;
        data.arrowRefireMax = (short) maxDelay;
        data.arrowRange = range;
    }

    /// Returns this mob's special data.
    @Override
    /// ISpecialMob
    public SpecialMobData getSpecialData() {
        return this.specialData;
    }

    /// Called to modify inherited attributes.
    @Override
    /// ISpecialMob
    public void adjustEntityAttributes() {
        if (!this.isHostile && this.rand.nextDouble() < Entity_SpecialPigZombie.HOSTILE_CHANCE) {
            NBTTagCompound entityData = new NBTTagCompound();
            this.writeToNBT(entityData);
            entityData.setShort("Anger", (short) (400 + this.rand.nextInt(400)));
            this.readFromNBT(entityData);
        }
        if (this.rand.nextDouble() < Entity_SpecialPigZombie.BOW_CHANCE) {
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

    /// Called to update this entity's AI.
    @Override
    protected void updateEntityActionState() {
    	this.seesTarget = false;
        super.updateEntityActionState();
        if (!this.seesTarget) {
            this.sightDelay = 20;
            IAttributeInstance attribute = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
            if (attribute.getModifier(Entity_SpecialPigZombie.stopModifierUUID) != null) {
                attribute.removeModifier(Entity_SpecialPigZombie.stopModifier);
            }
        }

        if (!this.isHostile && this.entityToAttack instanceof EntityPlayer) {
            this.isHostile = true;
        }
        if (this.isHostile && !this.hasPath()) {
            this.rotationYaw = MathHelper.wrapAngleTo180_float(this.rotationYaw + (float) this.rand.nextGaussian() * 20.0F);
        }
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
        return this.getSpecialData().onSpawnWithEgg(data, new EntityPigZombie(this.worldObj));
    }

    /// Called each tick this entity's attack target can be seen.
    @Override
    protected void attackEntity(Entity target, float distance) {
    	this.seesTarget = true;
        IAttributeInstance attribute = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        boolean stopped = attribute.getModifier(Entity_SpecialPigZombie.stopModifierUUID) != null;
        if (this.willShootBow() && this.getHeldItem() != null && this.getHeldItem().getItem() instanceof ItemBow) {
            if (!this.worldObj.isRemote && distance < this.getSpecialData().arrowRange) {
                if (target instanceof EntityLivingBase && this.attackTime <= 0) {
                    float damage = distance / this.getSpecialData().arrowRange;
                    this.attackTime = (int) (damage * (this.getSpecialData().arrowRefireMax - this.getSpecialData().arrowRefireMin) + this.getSpecialData().arrowRefireMin);
                    damage = Math.max(0.1F, Math.min(1.0F, damage));
                    this.attackEntityWithRangedAttack((EntityLivingBase) target, damage);
                }
                if (this.sightDelay > 0) {
                    this.sightDelay--;
                }
                boolean shouldStop = this.sightDelay <= 0;
                if (stopped != shouldStop) {
                    if (shouldStop) {
                        attribute.applyModifier(Entity_SpecialPigZombie.stopModifier);
                    }
                    else {
                        attribute.removeModifier(Entity_SpecialPigZombie.stopModifier);
                    }
                }
            }
            else {
                this.sightDelay = 20;
            }
        }
        else {
            if (stopped) {
                attribute.removeModifier(Entity_SpecialPigZombie.stopModifier);
            }
            super.attackEntity(target, distance);
        }
    }

    /// If this returns false, this mob will not shoot with a bow.
    public boolean willShootBow() {
        return true;
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

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        if (this.isHostile) {
        	saveTag.setBoolean("SMAnger", true);
        }

        this.getSpecialData().isImmuneToFire = this.isImmuneToFire;
        this.getSpecialData().writeToNBT(saveTag);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        if (saveTag.hasKey("SMAnger")) {
            this.isHostile = saveTag.getBoolean("SMAnger");
        }
        else if (tag.hasKey("SMAnger")) {
            this.isHostile = tag.getBoolean("SMAnger");
        }
        else {
            this.isHostile = tag.getShort("Anger") != 0;
        }

        this.getSpecialData().readFromNBT(tag);
        this.getSpecialData().readFromNBT(saveTag);
        this.isImmuneToFire = this.getSpecialData().isImmuneToFire;
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
    public void setInWeb() {
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