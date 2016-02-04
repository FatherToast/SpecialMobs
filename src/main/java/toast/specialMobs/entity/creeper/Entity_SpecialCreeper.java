package toast.specialMobs.entity.creeper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.DataWatcherHelper;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Entity_SpecialCreeper extends EntityCreeper implements ISpecialMob {

    // Useful properties for this class.
    private static final double CHARGED_CHANCE = Properties.getDouble(Properties.STATS, "creeper_charge_chance");

    // The data watcher key for the different exploding properties.
    public static final byte DW_EXPLODE_STATS = DataWatcherHelper.instance.CREEPER.nextKey();
    // The shift to get the bit for "can explode in water".
    public static final byte DW_CAN_EXPLODE_IN_WATER = 0;
    // The shift to get the bit for "explodes when burning".
    public static final byte DW_EXPLODE_ON_FIRE = 1;
    // The shift to get the bit for "explodes if shot".
    public static final byte DW_EXPLODE_WHEN_SHOT = 2;

    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] { new ResourceLocation("textures/entity/creeper/creeper.png") };

    // Fields taking the place of the private fields from EntityCreeper.
    // Ticks since this creeper has ignited.
    public int timeSinceIgnited;
    // Last tick's timeSinceIgnited.
    public int lastActiveTime;
    // Ticks it takes this creeper to explode.
    public int fuseTime = 30;
    // Explosion radius for this creeper.
    public int explosionRadius = 3;
    // Causes the next call to isEntityAlive() to return false. Used to prevent EntityCreeper from exploding instead of this.
    private boolean playingDead = false;

    // This mob's special mob data.
    private SpecialMobData specialData;

    public Entity_SpecialCreeper(World world) {
        super(world);
    }

    // Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        this.specialData = new SpecialMobData(this, Entity_SpecialCreeper.TEXTURES);
        super.entityInit();
        this.dataWatcher.addObject(Entity_SpecialCreeper.DW_EXPLODE_STATS, Byte.valueOf((byte) 0));
    }

    // Returns this mob's special data.
    @Override // ISpecialMob
    public SpecialMobData getSpecialData() {
        return this.specialData;
    }

    // Sets this creeper to be unable to explode while wet.
    public boolean canNotExplodeWhenWet() {
        return (this.dataWatcher.getWatchableObjectByte(Entity_SpecialCreeper.DW_EXPLODE_STATS) & 1 << Entity_SpecialCreeper.DW_CAN_EXPLODE_IN_WATER) != 0;
    }

    // Sets this creeper to be unable to explode while wet.
    public void setCanNotExplodeWhenWet(boolean value) {
        if (value != this.canNotExplodeWhenWet()) {
            this.dataWatcher.updateObject(Entity_SpecialCreeper.DW_EXPLODE_STATS, Byte.valueOf((byte) (this.dataWatcher.getWatchableObjectByte(Entity_SpecialCreeper.DW_EXPLODE_STATS) ^ 1 << Entity_SpecialCreeper.DW_CAN_EXPLODE_IN_WATER)));
        }
    }

    // Sets this creeper to be unable to explode while wet.
    public boolean explodesWhenBurning() {
        return (this.dataWatcher.getWatchableObjectByte(Entity_SpecialCreeper.DW_EXPLODE_STATS) & 1 << Entity_SpecialCreeper.DW_EXPLODE_ON_FIRE) != 0;
    }

    // Sets this creeper to be unable to explode while wet.
    public void setExplodesWhenBurning(boolean value) {
        if (value != this.explodesWhenBurning()) {
            this.dataWatcher.updateObject(Entity_SpecialCreeper.DW_EXPLODE_STATS, Byte.valueOf((byte) (this.dataWatcher.getWatchableObjectByte(Entity_SpecialCreeper.DW_EXPLODE_STATS) ^ 1 << Entity_SpecialCreeper.DW_EXPLODE_ON_FIRE)));
        }
    }

    // Sets this creeper to be unable to explode while wet.
    public boolean explodesWhenShot() {
        return (this.dataWatcher.getWatchableObjectByte(Entity_SpecialCreeper.DW_EXPLODE_STATS) & 1 << Entity_SpecialCreeper.DW_EXPLODE_WHEN_SHOT) != 0;
    }

    // Sets this creeper to be unable to explode while wet.
    public void setExplodesWhenShot(boolean value) {
        if (value != this.explodesWhenShot()) {
            this.dataWatcher.updateObject(Entity_SpecialCreeper.DW_EXPLODE_STATS, Byte.valueOf((byte) (this.dataWatcher.getWatchableObjectByte(Entity_SpecialCreeper.DW_EXPLODE_STATS) ^ 1 << Entity_SpecialCreeper.DW_EXPLODE_WHEN_SHOT)));
        }
    }

    // Called to modify inherited attributes.
    @Override // ISpecialMob
    public void adjustEntityAttributes() {
        if (this.worldObj.isThundering() && this.rand.nextDouble() < Entity_SpecialCreeper.CHARGED_CHANCE) {
            this.dataWatcher.updateObject(17, Byte.valueOf((byte) 1)); // isPowered
        }

        float prevMax = this.getMaxHealth();
        this.adjustTypeAttributes();
        this.setHealth(this.getMaxHealth() + this.getHealth() - prevMax);
    }

    // Overridden to modify inherited attribites.
    protected void adjustTypeAttributes() {
        // Override to alter attributes
    }

    // Checks whether target entity is alive.
    @Override
    public boolean isEntityAlive() {
        if (this.playingDead)
            return this.playingDead = false;
        return super.isEntityAlive();
    }

    // Called each tick while this entity exists.
    @Override
    public void onUpdate() {
        if (this.isEntityAlive()) {
            if (this.isWet() && this.canNotExplodeWhenWet()) {
                this.setCreeperState(-1);
            }
            else if (this.func_146078_ca() /*ignited*/|| this.isBurning() && this.explodesWhenBurning()) {
                this.setCreeperState(1);
            }

            this.lastActiveTime = this.timeSinceIgnited;
            int creeperState = this.getCreeperState();
            if (creeperState > 0) {
            	if (this.timeSinceIgnited == 0) {
            		this.playSound("creeper.primed", 1.0F, 0.5F);
            	}
            	this.onExplodingUpdate();
            }
            this.timeSinceIgnited += creeperState;
            if (this.timeSinceIgnited < 0) {
                this.timeSinceIgnited = 0;
            }
            if (this.timeSinceIgnited >= this.fuseTime) {
                this.timeSinceIgnited = this.fuseTime;
                if (!this.worldObj.isRemote) {
                    this.explodeByType(this.getPowered(), this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"));
                    this.setDead();
                }
            }
        }
        this.playingDead = true;
        super.onUpdate();
    }

    /** Called each tick while this creeper is exploding. */
    public void onExplodingUpdate() {
    	// To be overridden
    }

    // The explosion caused by this creeper.
    public void explodeByType(boolean powered, boolean griefing) {
        float power = powered ? this.explosionRadius * 2.0F : (float) this.explosionRadius;
        this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, power, griefing);
    }

    // Called each tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.getSpecialData().onUpdate();
    }

    // Called when this entity is first spawned to initialize it.
    @Override
    public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
        return this.getSpecialData().onSpawnWithEgg(data, new EntityCreeper(this.worldObj));
    }

    // Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (damageSource != null && damageSource.getSourceOfDamage() != damageSource.getEntity() && this.explodesWhenShot()) {
            this.func_146079_cb(); // ignite
        }
        return super.attackEntityFrom(damageSource, damage);
    }

    // Called to attack the target.
    @Override
    public boolean attackEntityAsMob(Entity target) {
        if (super.attackEntityAsMob(target)) {
            this.onTypeAttack(target);
            return true;
        }
        return false;
    }

    // Overridden to modify attack effects.
    protected void onTypeAttack(Entity target) {
        // Override to alter attack.
    }

    // Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        saveTag.setBoolean("DryExplode", this.canNotExplodeWhenWet());
        saveTag.setBoolean("BurningExplode", this.explodesWhenBurning());
        saveTag.setBoolean("ShotExplode", this.explodesWhenShot());

        this.getSpecialData().isImmuneToFire = this.isImmuneToFire;
        this.getSpecialData().writeToNBT(saveTag);

        tag.setShort("Fuse", (short) this.fuseTime);
        tag.setByte("ExplosionRadius", (byte) this.explosionRadius);
    }
    // Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        if (saveTag.hasKey("DryExplode")) {
            this.setCanNotExplodeWhenWet(saveTag.getBoolean("DryExplode"));
        }
        else if (tag.hasKey("DryExplode")) {
            this.setCanNotExplodeWhenWet(tag.getBoolean("DryExplode"));
        }
        if (saveTag.hasKey("BurningExplode")) {
            this.setExplodesWhenBurning(saveTag.getBoolean("BurningExplode"));
        }
        else if (tag.hasKey("BurningExplode")) {
            this.setExplodesWhenBurning(tag.getBoolean("BurningExplode"));
        }
        if (saveTag.hasKey("ShotExplode")) {
            this.setExplodesWhenShot(saveTag.getBoolean("ShotExplode"));
        }
        else if (tag.hasKey("ShotExplode")) {
            this.setExplodesWhenShot(tag.getBoolean("ShotExplode"));
        }

        this.getSpecialData().readFromNBT(tag);
        this.getSpecialData().readFromNBT(saveTag);
        this.isImmuneToFire = this.getSpecialData().isImmuneToFire;

        if (tag.hasKey("Fuse")) {
            this.fuseTime = tag.getShort("Fuse");
        }
        if (tag.hasKey("ExplosionRadius")) {
            this.explosionRadius = tag.getByte("ExplosionRadius");
        }
    }

    // Returns the intensity of the creeper's flash when it is ignited.
    @SideOnly(Side.CLIENT)
    @Override
    public float getCreeperFlashIntensity(float partialTick) {
        return (this.lastActiveTime + (this.timeSinceIgnited - this.lastActiveTime) * partialTick) / (this.fuseTime - 2);
    }

    // Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (_SpecialMobs.debug) {
            this.dropRareDrop(Math.max(0, this.rand.nextInt(5) - 3));
        }
    }

    // Returns the current armor level of this mob.
    @Override
    public int getTotalArmorValue() {
        return Math.min(20, super.getTotalArmorValue() + this.getSpecialData().armor);
    }

    // Sets this entity on fire.
    @Override
    public void setFire(int time) {
        if (!this.getSpecialData().isImmuneToBurning) {
            super.setFire(time);
        }
    }

    // Returns the current armor level of this mob.
    @Override
    public boolean allowLeashing() {
        return !this.getLeashed() && this.getSpecialData().allowLeashing;
    }

    // Sets the entity inside a web block.
    @Override
    public void setInWeb() {
        if (!this.getSpecialData().isImmuneToWebs) {
            super.setInWeb();
        }
    }

    // Called when the mob falls. Calculates and applies fall damage.
    @Override
    protected void fall(float distance) {
        if (!this.getSpecialData().isImmuneToFalling) {
            super.fall(distance);
        }
    }

    // Return whether this entity should NOT trigger a pressure plate or a tripwire.
    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return this.getSpecialData().ignorePressurePlates;
    }

    // True if the entity can breathe underwater.
    @Override
    public boolean canBreatheUnderwater() {
        return this.getSpecialData().canBreatheInWater;
    }

    // True if the entity can be pushed by flowing water.
    @Override
    public boolean isPushedByWater() {
        return !this.getSpecialData().ignoreWaterPush;
    }

    // Returns true if the potion can be applied.
    @Override
    public boolean isPotionApplicable(PotionEffect effect) {
        return this.getSpecialData().isPotionApplicable(effect);
    }
}