package toast.specialMobs.entity.blaze;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

public class Entity_SpecialBlaze extends EntityBlaze implements ISpecialMob {
    /// Useful properties for this class.
    //private static final double HOSTILE_CHANCE = Properties.getDouble(Properties.STATS, "hostile_pigzombies");

    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] { new ResourceLocation("textures/entity/blaze.png") };

    /// This mob's special mob data.
    private SpecialMobData specialData;

    /// The state of this blaze's attack.
	public int attackState;
    /// The amount of fireballs in each burst.
	public short fireballBurstCount;
    /// The ticks between each shot in a burst.
	public short fireballBurstDelay;

    public Entity_SpecialBlaze(World world) {
        super(world);
        this.getSpecialData().isImmuneToFire = this.isImmuneToFire;
        this.setRangedAI(3, 6, 60, 100, 30.0F);
        this.getSpecialData().arrowSpread = 0.5F;
    }

    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        this.specialData = new SpecialMobData(this, Entity_SpecialBlaze.TEXTURES);
        super.entityInit();
    }

    /// Helper method to set the attack AI more easily.
    protected void setRangedAI(int burstCount, int burstDelay, int chargeTime, int cooldownTime, float range) {
    	this.fireballBurstCount = (short) burstCount;
    	this.fireballBurstDelay = (short) burstDelay;

        SpecialMobData data = this.getSpecialData();
        data.arrowRefireMin = (short) chargeTime;
        data.arrowRefireMax = (short) (chargeTime + cooldownTime);
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
        float prevMax = this.getMaxHealth();
        this.adjustTypeAttributes();
        this.setHealth(this.getMaxHealth() + this.getHealth() - prevMax);
    }

    /// Overridden to modify inherited attribites.
    protected void adjustTypeAttributes() {
        /// Override to alter attributes.
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
        return this.getSpecialData().onSpawnWithEgg(data, new EntityBlaze(this.worldObj));
    }

    /// Called each tick this entity's attack target can be seen.
    @Override
    protected void attackEntity(Entity target, float distance) {
    	SpecialMobData data = this.getSpecialData();
        if (this.attackTime <= 0 && distance < 2.0F && target.boundingBox.maxY > this.boundingBox.minY && target.boundingBox.minY < this.boundingBox.maxY) {
            this.attackTime = 20;
            this.attackEntityAsMob(target);
        }
        else if (this.canBeHurtByFire(target) && distance < data.arrowRange) {
            if (this.attackTime == 0) {
                this.attackState++;
                if (this.attackState == 1) {
                    this.attackTime = data.arrowRefireMin;
                    this.func_70844_e(true); // setRenderBurning
                }
                else if (this.attackState <= this.fireballBurstCount + 1) {
                    this.attackTime = this.fireballBurstDelay;
                }
                else {
                    this.attackTime = data.arrowRefireMax - data.arrowRefireMin;
                    this.attackState = 0;
                    this.func_70844_e(false); // setRenderBurning
                }

                if (this.attackState > 1) {
					this.shootFireballAtEntity(target, distance);
                }
            }
            this.rotationYaw = (float) (Math.atan2(target.posZ - this.posZ, target.posX - this.posX) * 180.0 / Math.PI) - 90.0F;
            this.hasAttacked = true;
        }
        else {
        	if (this.onGround) {
        		this.moveEntityWithHeading(0.0F, (float) this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() * 7.0F);
        	}
        	else {
        		this.moveFlying(0.0F, (float) this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() * 7.0F, 0.03F);
        	}
        }
    }

    // Returns true if the target can be hurt by fireballs.
    protected boolean canBeHurtByFire(Entity entity) {
    	return !entity.isImmuneToFire() && (!(entity instanceof EntityLivingBase) || !((EntityLivingBase) entity).isPotionActive(Potion.fireResistance));
    }

    // Called to attack the target entity with a fireball.
    public void shootFireballAtEntity(Entity target, float distance) {
        double dX = target.posX - this.posX;
        double dY = target.boundingBox.minY + target.height / 2.0F - this.posY - this.height / 2.0F;
        double dZ = target.posZ - this.posZ;
        float spread = (float) Math.sqrt(distance) * this.getSpecialData().arrowSpread;
        this.worldObj.playAuxSFXAtEntity((EntityPlayer) null, 1009, (int) this.posX, (int) this.posY, (int) this.posZ, 0);
        EntitySmallFireball fireball = new EntitySmallFireball(this.worldObj, this, dX + this.rand.nextGaussian() * spread, dY, dZ + this.rand.nextGaussian() * spread);
        fireball.posY = this.posY + this.height / 2.0F + 0.5;
        this.worldObj.spawnEntityInWorld(fireball);
    }

    /// Called to attack the target.
    @Override
    public boolean attackEntityAsMob(Entity target) {
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
        saveTag.setShort("SMFireballBurstCount", this.fireballBurstCount);
        saveTag.setShort("SMFireballBurstDelay", this.fireballBurstDelay);

        this.getSpecialData().isImmuneToFire = this.isImmuneToFire;
        this.getSpecialData().writeToNBT(saveTag);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        if (saveTag.hasKey("SMFireballBurstCount")) {
            this.fireballBurstCount = saveTag.getShort("SMFireballBurstCount");
        }
        else if (tag.hasKey("SMFireballBurstCount")) {
            this.fireballBurstCount = tag.getShort("SMFireballBurstCount");
        }
        if (saveTag.hasKey("SMFireballBurstDelay")) {
            this.fireballBurstDelay = saveTag.getShort("SMFireballBurstDelay");
        }
        else if (tag.hasKey("SMFireballBurstDelay")) {
            this.fireballBurstDelay = tag.getShort("SMFireballBurstDelay");
        }

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