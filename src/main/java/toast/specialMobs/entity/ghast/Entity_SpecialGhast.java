package toast.specialMobs.entity.ghast;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

public class Entity_SpecialGhast extends EntityGhast implements ISpecialMob {
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] { new ResourceLocation("textures/entity/ghast/ghast.png"), new ResourceLocation("textures/entity/ghast/ghast_shooting.png") };

    /// The base explosion strength of this ghast's fireballs.
    public int explosionStrength = 1;
    /// The currently targeted entity.
    public Entity targetedEntity;
    /// Cooldown time between target loss and new target aquirement.
    public int aggroCooldown;

    /// This mob's special mob data.
    private SpecialMobData specialData;

    public Entity_SpecialGhast(World world) {
        super(world);
        this.getSpecialData().isImmuneToFire = this.isImmuneToFire;
    }

    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        this.specialData = new SpecialMobData(this, Entity_SpecialGhast.TEXTURES);
        super.entityInit();
        this.setRangedAI(20, 60, 64.0F);
    }

    /// Helper method to set the attack AI more easily.
    protected void setRangedAI(int minDelay, int maxDelay, float range) {
        SpecialMobData data = this.getSpecialData();
        data.arrowRefireMin = (short) minDelay;
        data.arrowRefireMax = (short) maxDelay;
        data.arrowRange = range;
    }

    /// Initializes this entity's attributes.
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(4.0);
    }

    @Override
    public SpecialMobData getSpecialData() {
        return this.specialData;
    }

    @Override
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
        EntityGhast proxy = new EntityGhast(this.worldObj);
        proxy.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage); // Vanilla ghasts do not have this attribute
        return this.getSpecialData().onSpawnWithEgg(data, proxy);
    }

    /// Called to tick this entity's AI.
    @Override
    protected void updateEntityActionState() {
        /// Check for despawning.
        if (!this.worldObj.isRemote && this.worldObj.difficultySetting.getDifficultyId() == 0) {
            this.setDead();
        }
        this.despawnEntity();
        this.prevAttackCounter = this.attackCounter;
        /// Update movement, targets, and attacks.
        this.updateEntityGoal();
        /// Update the texture.
        if (!this.worldObj.isRemote) {
            boolean shooting = this.getFireTexture() == 1;
            boolean shouldBeShooting = this.attackCounter > 10;
            if (shooting != shouldBeShooting) {
                this.setFireTexture(shouldBeShooting);
            }
        }
    }

    // Updates the current goal.
    protected void updateEntityGoal() {
        // Perform movement.
        double vX = this.waypointX - this.posX;
        double vY = this.waypointY - this.posY;
        double vZ = this.waypointZ - this.posZ;
        double v = vX * vX + vY * vY + vZ * vZ;
        if (v < 1.0 || v > 3600.0) {
            this.setRandomWaypoints(32.0F);
        }
        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown += this.rand.nextInt(5) + 2;
            v = Math.sqrt(v);
            if (this.isCourseTraversable(v)) {
                double speed = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() / v;
                this.motionX += vX * speed;
                this.motionY += vY * speed;
                this.motionZ += vZ * speed;
            }
            else {
                this.clearWaypoints();
            }
        }
        // Update the current target.
        this.updateEntityTarget();
        // Execute goal, if able.
        if (this.targetedEntity != null && this.targetedEntity.getDistanceSqToEntity(this) < this.getSpecialData().arrowRange * this.getSpecialData().arrowRange) {
            double x = this.targetedEntity.posX - this.posX;
            double z = this.targetedEntity.posZ - this.posZ;
            this.renderYawOffset = this.rotationYaw = -((float) Math.atan2(x, z)) * 180.0F / (float) Math.PI;
            if (this.canEntityBeSeen(this.targetedEntity)) {
                if (this.attackCounter == this.getSpecialData().arrowRefireMin >> 1) {
                    this.worldObj.playAuxSFXAtEntity((EntityPlayer) null, 1007, (int) this.posX, (int) this.posY, (int) this.posZ, 0);
                }
                this.attackCounter++;
                if (this.attackCounter >= this.getSpecialData().arrowRefireMin) {
                    if (!this.worldObj.isRemote) {
                        this.shootFireballAtEntity(this.targetedEntity);
                    }
                    this.attackCounter = this.getSpecialData().arrowRefireMin - this.getSpecialData().arrowRefireMax;
                }
            }
            else if (this.attackCounter > 0) {
                this.attackCounter--;
            }
        }
        else {
            this.renderYawOffset = this.rotationYaw = - ((float) Math.atan2(this.motionX, this.motionZ)) * 180.0F / (float) Math.PI;
            if (this.attackCounter > 0) {
                this.attackCounter--;
            }
        }
    }

    // Sets a random waypoint within range.
    public void setRandomWaypoints(float range) {
        this.waypointX = this.posX + (this.rand.nextFloat() - 0.5F) * range;
        this.waypointY = this.posY + (this.rand.nextFloat() - 0.5F) * range;
        this.waypointZ = this.posZ + (this.rand.nextFloat() - 0.5F) * range;
    }

    // Sets a random waypoint within range.
    public void clearWaypoints() {
        this.waypointX = this.posX;
        this.waypointY = this.posY;
        this.waypointZ = this.posZ;
    }

    // True if the ghast has an unobstructed line of travel to the waypoint.
    public boolean isCourseTraversable(double v) {
        double dX = (this.waypointX - this.posX) / v;
        double dY = (this.waypointY - this.posY) / v;
        double dZ = (this.waypointZ - this.posZ) / v;
        AxisAlignedBB aabb = this.boundingBox.copy();
        for (int i = 1; i < v; i++) {
            aabb.offset(dX, dY, dZ);
            if (!this.worldObj.getCollidingBoundingBoxes(this, aabb).isEmpty())
                return false;
        }
        return true;
    }

    /// Updates this entity's target.
    protected void updateEntityTarget() {
        if (this.targetedEntity != null && this.targetedEntity.isDead) {
            this.targetedEntity = null;
        }
        if (this.targetedEntity == null || this.aggroCooldown-- <= 0) {
            this.targetedEntity = this.worldObj.getClosestVulnerablePlayerToEntity(this, 100.0);
            if (this.targetedEntity != null) {
                this.aggroCooldown = 20;
            }
        }
    }

    /// Get/set functions for the texture.
    public byte getFireTexture() {
        return this.dataWatcher.getWatchableObjectByte(16);
    }

    public void setFireTexture(boolean fire) {
        this.dataWatcher.updateObject(16, Byte.valueOf(fire ? (byte) 1 : (byte) 0));
    }

    // Called to attack the target entity with a fireball.
    public void shootFireballAtEntity(Entity target) {
        double dX = target.posX - this.posX;
        double dY = target.boundingBox.minY + target.height / 2.0F - this.posY - this.height / 2.0F;
        double dZ = target.posZ - this.posZ;
        this.worldObj.playAuxSFXAtEntity((EntityPlayer) null, 1008, (int) this.posX, (int) this.posY, (int) this.posZ, 0);
        EntityLargeFireball fireball = new EntityLargeFireball(this.worldObj, this, dX, dY, dZ);
        fireball.field_92057_e = Math.round(this.explosionStrength * this.getTypeExplosionMult()); // Sets the fireball's explosion strength
        Vec3 vec3 = this.getLook(1.0F);
        fireball.posX = this.posX + vec3.xCoord * this.width;
        fireball.posY = this.posY + this.height / 2.0F + 0.5;
        fireball.posZ = this.posZ + vec3.zCoord * this.width;
        this.worldObj.spawnEntityInWorld(fireball);
    }

    /// Called to attack the target entity.
    @Override
    public boolean attackEntityAsMob(Entity target) {
        float attackDamage = (float) this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        int knockback = 0;
        if (target instanceof EntityLivingBase) {
            attackDamage += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase) target);
            knockback = EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase) target);
        }

        boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), attackDamage);
        if (hit) {
            if (knockback > 0) {
                target.addVelocity(-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F, 0.1, MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F);
                this.motionX *= 0.6;
                this.motionZ *= 0.6;
            }

            int fireAspect = EnchantmentHelper.getFireAspectModifier(this);
            if (fireAspect > 0) {
                target.setFire(fireAspect << 2);
            }
            if (target instanceof EntityLivingBase) {
                EnchantmentHelper.func_151384_a((EntityLivingBase) target, this); // Triggers hit entity's enchants.
            }
            EnchantmentHelper.func_151385_b(this, target); // Triggers attacker's enchants.

            this.onTypeAttack(target);
        }
        return hit;
    }

    /// Overridden to modify attack effects.
    protected void onTypeAttack(Entity target) {
        /// Override to alter attack.
    }

    /// Returns the multiplier this ghast has for its explosion size.
    protected float getTypeExplosionMult() {
        return 1.0F;
    }

    /// Called by a special fireball's onImpact().
    public void onImpact(Entity fireball, Entity entityHit, double x, double y, double z) {
        /// Override to alter impact.
    }

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        saveTag.setInteger("ExplosionPower", this.explosionStrength);

        this.getSpecialData().isImmuneToFire = this.isImmuneToFire;
        this.getSpecialData().writeToNBT(saveTag);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        if (saveTag.hasKey("ExplosionPower")) {
            this.explosionStrength = saveTag.getInteger("ExplosionPower");
        }
        else if (tag.hasKey("ExplosionPower")) {
            this.explosionStrength = tag.getInteger("ExplosionPower");
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