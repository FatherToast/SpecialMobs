package toast.specialMobs.entity.slime;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

public class Entity_SpecialSlime extends EntitySlime implements ISpecialMob
{
    /// Useful properties for this class.
    private static final boolean TINY_SLIME_DAMAGE = Properties.getBoolean(Properties.STATS, "tiny_slime_damage");

    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
    	new ResourceLocation("textures/entity/slime/slime.png")
    };

    /// This mob's special mob data.
    private SpecialMobData specialData;

    /// Ticks the slime must stay on the ground before it can jump again.
	public int slimeJumpDelay;

    public Entity_SpecialSlime(World world) {
        super(world);
    }

    /// Gets the multiplier for this type's size.
    protected float getSizeMultiplier() {
    	return 0.6F;
    }
    /// Gets the additional experience this slime type gives.
    protected int getTypeXp() {
    	return 0;
    }

    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        this.specialData = new SpecialMobData(this, Entity_SpecialSlime.TEXTURES);
        super.entityInit();
    }

    /// Initializes this entity's attributes.
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(0.0);
    }

    /// Returns this mob's special data.
    @Override /// ISpecialMob
    public SpecialMobData getSpecialData() {
        return this.specialData;
    }

    /// Called to modify inherited attributes.
    @Override /// ISpecialMob
    public void adjustEntityAttributes() {
        this.adjustTypeAttributes();
    }

    /// Overridden to modify inherited attribites, except for health.
    protected void adjustTypeAttributes() {
        /// Override to alter attributes.
    }
    /// Overridden to modify inherited max health.
    protected void adjustHealthAttribute() {
        /// Override to alter attribute.
    }

    /// Sets the slime's size and updates its bounding box.
    @Override
	protected void setSlimeSize(int size) {
    	super.setSlimeSize(size);

        this.setSize(size * this.getSizeMultiplier(), size * this.getSizeMultiplier());
        this.setPosition(this.posX, this.posY, this.posZ);
    	this.adjustHealthAttribute();
        this.setHealth(this.getMaxHealth());
        this.experienceValue += this.getTypeXp();
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
        EntitySlime proxy = new EntitySlime(this.worldObj);
        proxy.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage); // Vanilla slimes do not have this attribute
        return this.getSpecialData().onSpawnWithEgg(data, proxy);
    }

    /// Called to tick this entity's AI.
    @Override
	protected void updateEntityActionState() {
        /// Check for despawning.
        this.despawnEntity();
        /// Acquire target.
        EntityPlayer target = this.worldObj.getClosestVulnerablePlayerToEntity(this, 16.0);
        if (target != null) {
            this.faceEntity(target, 10.0F, 20.0F);
        }
        this.attackEntityByType(target);
        /// Update movement.
        if (this.onGround && this.slimeJumpDelay-- <= 0)  {
            this.slimeJumpDelay = this.getJumpDelay();
            if (this.jumpByType(target)) {
	            this.isJumping = true;
	            if (this.makesSoundOnJump()) {
	                this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
	            }
            }
        }
        else {
            this.isJumping = false;
            if (this.onGround) {
                this.moveStrafing = this.moveForward = 0.0F;
            }
        }
    }

    /// Called each tick to update behavior.
    protected void attackEntityByType(EntityPlayer target) {
        /// Override to alter behavior.
    }
    /// Called when this slime jumps, returns true if it successfully jumps.
    protected boolean jumpByType(EntityPlayer target) {
        if (target != null) {
            this.slimeJumpDelay /= 3;
        }
        this.moveStrafing = 1.0F - this.rand.nextFloat() * 2.0F;
        this.moveForward = this.getSlimeSize();
    	return true;
    }

    /// Called by a player entity when they collide with an entity.
    @Override
	public void onCollideWithPlayer(EntityPlayer player) {
        if (!this.worldObj.isRemote && this.attackTime <= 0 && this.canDamagePlayer() && this.canEntityBeSeen(player) && this.getDistanceSqToEntity(player) < this.width * this.width + player.width * player.width * 1.3F && this.attackEntityAsMob(player)) {
        	this.attackTime = 20;
            this.playSound("mob.slime.attack", 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
        }
    }

    /// Indicates weather the slime is able to damage players (based upon the slime's size).
    @Override
	protected boolean canDamagePlayer() {
        return Entity_SpecialSlime.TINY_SLIME_DAMAGE || this.getSlimeSize() > 1;
    }

    /// Called to attack the target.
    @Override
    public boolean attackEntityAsMob(Entity target) {
        float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue() + this.getAttackStrength();
        int knockback = 0;
        if (target instanceof EntityLivingBase) {
            damage += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase) target);
            knockback += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase) target);
        }

        if (target.attackEntityFrom(DamageSource.causeMobDamage(this), damage)) {
            if (knockback > 0) {
            	target.addVelocity(-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F, 0.1, MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F);
                this.motionX *= 0.6;
                this.motionZ *= 0.6;
            }
            int fireAspect = EnchantmentHelper.getFireAspectModifier(this);
            if (fireAspect > 0) {
            	target.setFire(fireAspect * 4);
            }
            if (target instanceof EntityLivingBase) {
                EnchantmentHelper.func_151384_a((EntityLivingBase) target, this);
            }
            EnchantmentHelper.func_151385_b(this, target);

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
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (_SpecialMobs.debug) {
            this.dropRareDrop(Math.max(0, this.rand.nextInt(5) - 3));
        }
    }

    /// Used to create smaller slimes on death.
    @Override
	protected Entity_SpecialSlime createInstance() {
        try {
            return this.getClass().getConstructor(new Class[] { World.class }).newInstance(new Object[] { this.worldObj });
        }
        catch (Exception ex) {
            _SpecialMobs.debugException("Error splitting slime! " + ex.getClass().getName() + " @" + this.getClass().getName());
            return new Entity_SpecialSlime(this.worldObj);
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