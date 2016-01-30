package toast.specialMobs.entity.cavespider;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.EntitySpecialSpitball;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

public class Entity_SpecialCaveSpider extends EntityCaveSpider implements ISpecialMob, IRangedAttackMob
{
    /// Useful properties for this class.
    private static final double HOSTILE_CHANCE = Properties.getDouble(Properties.STATS, "hostile_cavespiders");
    private static final double SPIT_CHANCE = Properties.getDouble(Properties.STATS, "spit_chance_cavespider");
    /// Attacking speed boost modifier.
    private static final UUID stopModifierUUID = UUID.fromString("70A57A49-7EC5-45BA-B886-3B90B23A1718");
    private static final AttributeModifier stopModifier = new AttributeModifier(Entity_SpecialCaveSpider.stopModifierUUID, "Attacking speed boost", -1.0, 2).setSaved(false);

    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation("textures/entity/spider/cave_spider.png"),
        new ResourceLocation("textures/entity/spider_eyes.png")
    };

    /// If true, this spider will attack regardless of light level.
    public boolean isHostile;
    /// Used to trick the spider into thinking it is in darkness when isHostile is true.
    public boolean fakeDarkness;

    /// Delay until the next spit attack.
    public int spitDelay = 0;
    /// Ticks until this spider stops moving once in range for a spit attack.
    public byte sightDelay = 20;

    /// This mob's special mob data.
    private SpecialMobData specialData;

    public Entity_SpecialCaveSpider(World world) {
        super(world);
        this.getSpecialData().resetRenderScale(0.7F);
        this.getSpecialData().immuneToPotions.add(Potion.poison.id);
        this.getSpecialData().isImmuneToWebs = true;
    }

    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        this.specialData = new SpecialMobData(this, Entity_SpecialCaveSpider.TEXTURES);
        super.entityInit();
        if (this.rand.nextDouble() < Entity_SpecialCaveSpider.SPIT_CHANCE) {
            this.setRangedAI(15, 40, 10.0F);
        }
    }

    /// Helper method to set the attack AI more easily.
    protected void setRangedAI(int minDelay, int maxDelay, float range) {
        SpecialMobData data = this.getSpecialData();
        data.arrowRefireMin = (short) minDelay;
        data.arrowRefireMax = (short) maxDelay;
        data.arrowRange = range;
    }

    /// Returns this mob's special data.
    @Override /// ISpecialMob
    public SpecialMobData getSpecialData() {
        return this.specialData;
    }

    /// Called to modify inherited attributes.
    @Override /// ISpecialMob
    public void adjustEntityAttributes() {
        if (this.rand.nextDouble() < Entity_SpecialCaveSpider.HOSTILE_CHANCE) {
            this.isHostile = true;
        }

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
        if (this.spitDelay > 0) {
            this.spitDelay--;
        }
        super.onLivingUpdate();
        this.getSpecialData().onUpdate();
    }

    /// Called to update this entity's AI.
    @Override
    protected void updateEntityActionState() {
        super.updateEntityActionState();
        if (this.isHostile && !this.hasPath()) {
            this.rotationYaw = MathHelper.wrapAngleTo180_float(this.rotationYaw + (float) this.rand.nextGaussian() * 20.0F);
        }
    }

    /// Finds the closest player within 16 blocks to attack, or null if this Entity isn't interested in attacking.
    @Override
    protected Entity findPlayerToAttack() {
        if (this.isHostile) {
            this.fakeDarkness = true;
        }
        return super.findPlayerToAttack();
    }

    /// Called each tick this entity's attack target can be seen.
    @Override
    protected void attackEntity(Entity target, float distance) {
        IAttributeInstance attribute = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        boolean stopped = attribute.getModifier(Entity_SpecialCaveSpider.stopModifierUUID) != null;
        if (this.getSpecialData().arrowRange > 0.0F) {
            if (!this.worldObj.isRemote && this.spitDelay <= 0 && distance < this.getSpecialData().arrowRange) {
                if (target instanceof EntityLivingBase && this.spitDelay <= 0) {
                    float damage = distance / this.getSpecialData().arrowRange;
                    this.spitDelay = (int) (damage * (this.getSpecialData().arrowRefireMax - this.getSpecialData().arrowRefireMin) + this.getSpecialData().arrowRefireMin);
                    damage = Math.max(0.1F, Math.min(1.0F, damage));
                    this.attackEntityWithRangedAttack((EntityLivingBase) target, damage);
                }
                if (this.sightDelay > 0) {
                    this.sightDelay--;
                }
                boolean shouldStop = this.sightDelay <= 0;
                if (stopped != shouldStop) {
                    if (shouldStop) {
                        attribute.applyModifier(Entity_SpecialCaveSpider.stopModifier);
                    }
                    else {
                        attribute.removeModifier(Entity_SpecialCaveSpider.stopModifier);
                    }
                }
            }
            else {
                this.sightDelay = 20;
            }
        }
        else {
            if (stopped) {
                attribute.removeModifier(Entity_SpecialCaveSpider.stopModifier);
            }
            if (this.isHostile) {
                this.fakeDarkness = true;
            }
            super.attackEntity(target, distance);
        }
    }

    /// Attack the specified entity using a ranged attack.
    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float range) {
        EntitySpecialSpitball spitball = new EntitySpecialSpitball(this.worldObj, this, target, 1.6F, this.getTypeArrowSpread());
        spitball.setDamage(range * this.getSpecialData().arrowDamage + (float) this.rand.nextGaussian() * 0.25F + this.worldObj.difficultySetting.getDifficultyId() * 0.11F);

        this.playSound("mob.slimeattack", 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
        this.worldObj.spawnEntityInWorld(spitball);
    }

    /// Overridden to modify the base arrow variance.
    protected float getTypeArrowSpread() {
        return this.getSpecialData().arrowSpread - this.worldObj.difficultySetting.getDifficultyId() * (this.getSpecialData().arrowSpread / 4.0F + 0.5F);
    }

    /// Gets how bright this entity is.
    @Override
    public float getBrightness(float partialTick) {
        if (this.fakeDarkness) {
            this.fakeDarkness = false;
            return 0.0F;
        }
        return super.getBrightness(partialTick);
    }

    /// Called when this entity is first spawned to initialize it.
    @Override
    public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
        return this.getSpecialData().onSpawnWithEgg(data, new EntityCaveSpider(this.worldObj));
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
    public void onTypeAttack(Entity target) {
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