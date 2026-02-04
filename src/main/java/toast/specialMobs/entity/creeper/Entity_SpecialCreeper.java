package toast.specialMobs.entity.creeper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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

public class Entity_SpecialCreeper extends EntityCreeper implements ISpecialMob {
    
    // Useful properties for this class.
    private static final double CHARGED_CHANCE = Properties.getDouble( Properties.STATS, "creeper_charge_chance" );
    
    // The data watcher key for the different exploding properties.
    public static final byte DW_EXPLODE_STATS = DataWatcherHelper.instance.CREEPER.nextKey();
    // The shift to get the bit for "can explode in water".
    public static final byte DW_CAN_EXPLODE_IN_WATER = 0;
    // The shift to get the bit for "explodes when burning".
    public static final byte DW_EXPLODE_ON_FIRE = 1;
    // The shift to get the bit for "explodes if shot".
    public static final byte DW_EXPLODE_WHEN_SHOT = 2;
    
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] { new ResourceLocation( "textures/entity/creeper/creeper.png" ) };
    
    // Fields taking the place of the private fields from EntityCreeper.
    // Ticks since this creeper has ignited.
    public int timeSinceIgnited;
    // Last tick's timeSinceIgnited.
    public int lastActiveTime;
    // Ticks it takes this creeper to explode.
    public int fuseTime = 30;
    // Explosion radius for this creeper.
    public int explosionRadius = 3;
    // Causes the next call to isEntityAlive() to return false. Used to prevent EntityCreeper from exploding instead of 
    private boolean playingDead = false;
    
    // This mob's special mob data.
    private SpecialMobData specialData;
    
    public Entity_SpecialCreeper( World world ) {
        super( world );
    }
    
    // Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        specialData = new SpecialMobData( this, Entity_SpecialCreeper.TEXTURES );
        super.entityInit();
        dataWatcher.addObject( Entity_SpecialCreeper.DW_EXPLODE_STATS, (byte) 0 );
    }
    
    // Returns this mob's special data.
    @Override // ISpecialMob
    public SpecialMobData getSpecialData() {
        return specialData;
    }
    
    // Sets this creeper to be unable to explode while wet.
    public boolean canNotExplodeWhenWet() {
        return (dataWatcher.getWatchableObjectByte( Entity_SpecialCreeper.DW_EXPLODE_STATS ) & 1 << Entity_SpecialCreeper.DW_CAN_EXPLODE_IN_WATER) != 0;
    }
    
    // Sets this creeper to be unable to explode while wet.
    public void setCanNotExplodeWhenWet( boolean value ) {
        if( value != canNotExplodeWhenWet() ) {
            dataWatcher.updateObject( Entity_SpecialCreeper.DW_EXPLODE_STATS, (byte) (dataWatcher.getWatchableObjectByte( Entity_SpecialCreeper.DW_EXPLODE_STATS ) ^ 1 << Entity_SpecialCreeper.DW_CAN_EXPLODE_IN_WATER) );
        }
    }
    
    // Sets this creeper to be unable to explode while wet.
    public boolean explodesWhenBurning() {
        return (dataWatcher.getWatchableObjectByte( Entity_SpecialCreeper.DW_EXPLODE_STATS ) & 1 << Entity_SpecialCreeper.DW_EXPLODE_ON_FIRE) != 0;
    }
    
    // Sets this creeper to be unable to explode while wet.
    public void setExplodesWhenBurning( boolean value ) {
        if( value != explodesWhenBurning() ) {
            dataWatcher.updateObject( Entity_SpecialCreeper.DW_EXPLODE_STATS, (byte) (dataWatcher.getWatchableObjectByte( Entity_SpecialCreeper.DW_EXPLODE_STATS ) ^ 1 << Entity_SpecialCreeper.DW_EXPLODE_ON_FIRE) );
        }
    }
    
    // Sets this creeper to be unable to explode while wet.
    public boolean explodesWhenShot() {
        return (dataWatcher.getWatchableObjectByte( Entity_SpecialCreeper.DW_EXPLODE_STATS ) & 1 << Entity_SpecialCreeper.DW_EXPLODE_WHEN_SHOT) != 0;
    }
    
    // Sets this creeper to be unable to explode while wet.
    public void setExplodesWhenShot( boolean value ) {
        if( value != explodesWhenShot() ) {
            dataWatcher.updateObject( Entity_SpecialCreeper.DW_EXPLODE_STATS, (byte) (dataWatcher.getWatchableObjectByte( Entity_SpecialCreeper.DW_EXPLODE_STATS ) ^ 1 << Entity_SpecialCreeper.DW_EXPLODE_WHEN_SHOT) );
        }
    }
    
    // Called to modify inherited attributes.
    @Override // ISpecialMob
    public void adjustEntityAttributes() {
        if( worldObj.isThundering() && rand.nextDouble() < Entity_SpecialCreeper.CHARGED_CHANCE ) {
            dataWatcher.updateObject( 17, (byte) 1 ); // isPowered
        }
        
        float prevMax = getMaxHealth();
        adjustTypeAttributes();
        setHealth( getMaxHealth() + getHealth() - prevMax );
    }
    
    // Overridden to modify inherited attribites.
    protected void adjustTypeAttributes() {
        // Override to alter attributes
    }
    
    // Checks whether target entity is alive.
    @Override
    public boolean isEntityAlive() {
        if( playingDead )
            return playingDead = false;
        return super.isEntityAlive();
    }
    
    // Called each tick while this entity exists.
    @Override
    public void onUpdate() {
        if( isEntityAlive() ) {
            if( isWet() && canNotExplodeWhenWet() ) {
                setCreeperState( -1 );
            }
            else if( func_146078_ca() /*ignited*/ || isBurning() && explodesWhenBurning() ) {
                setCreeperState( 1 );
            }
            
            lastActiveTime = timeSinceIgnited;
            int creeperState = getCreeperState();
            if( creeperState > 0 ) {
                if( timeSinceIgnited == 0 ) {
                    playSound( "creeper.primed", 1.0F, 0.5F );
                }
                onExplodingUpdate();
            }
            timeSinceIgnited += creeperState;
            if( timeSinceIgnited < 0 ) {
                timeSinceIgnited = 0;
            }
            if( timeSinceIgnited >= fuseTime ) {
                timeSinceIgnited = fuseTime;
                if( !worldObj.isRemote ) {
                    explodeByType( getPowered(), worldObj.getGameRules().getGameRuleBooleanValue( "mobGriefing" ) );
                    setDead();
                }
            }
        }
        playingDead = true;
        super.onUpdate();
    }
    
    /** Called each tick while this creeper is exploding. */
    public void onExplodingUpdate() {
        // To be overridden
    }
    
    // The explosion caused by this creeper.
    public void explodeByType( boolean powered, boolean griefing ) {
        float power = powered ? explosionRadius * 2.0F : (float) explosionRadius;
        worldObj.createExplosion( this, posX, posY, posZ, power, griefing );
    }
    
    // Called each tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        getSpecialData().onUpdate();
    }
    
    // Called when this entity is first spawned to initialize it.
    @Override
    public IEntityLivingData onSpawnWithEgg( IEntityLivingData data ) {
        return getSpecialData().onSpawnWithEgg( data, new EntityCreeper( worldObj ) );
    }
    
    // Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
    @Override
    public boolean attackEntityFrom( DamageSource damageSource, float damage ) {
        if( damageSource != null && damageSource.getSourceOfDamage() != damageSource.getEntity() && explodesWhenShot() ) {
            func_146079_cb(); // ignite
        }
        return super.attackEntityFrom( damageSource, damage );
    }
    
    // Called to attack the target.
    @Override
    public boolean attackEntityAsMob( Entity target ) {
        if( super.attackEntityAsMob( target ) ) {
            onTypeAttack( target );
            return true;
        }
        return false;
    }
    
    // Overridden to modify attack effects.
    protected void onTypeAttack( Entity target ) {
        // Override to alter attack.
    }
    
    // Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        saveTag.setBoolean( "DryExplode", canNotExplodeWhenWet() );
        saveTag.setBoolean( "BurningExplode", explodesWhenBurning() );
        saveTag.setBoolean( "ShotExplode", explodesWhenShot() );
        
        getSpecialData().isImmuneToFire = isImmuneToFire;
        getSpecialData().writeToNBT( saveTag );
        
        tag.setShort( "Fuse", (short) fuseTime );
        tag.setByte( "ExplosionRadius", (byte) explosionRadius );
    }
    
    // Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        if( saveTag.hasKey( "DryExplode" ) ) {
            setCanNotExplodeWhenWet( saveTag.getBoolean( "DryExplode" ) );
        }
        else if( tag.hasKey( "DryExplode" ) ) {
            setCanNotExplodeWhenWet( tag.getBoolean( "DryExplode" ) );
        }
        if( saveTag.hasKey( "BurningExplode" ) ) {
            setExplodesWhenBurning( saveTag.getBoolean( "BurningExplode" ) );
        }
        else if( tag.hasKey( "BurningExplode" ) ) {
            setExplodesWhenBurning( tag.getBoolean( "BurningExplode" ) );
        }
        if( saveTag.hasKey( "ShotExplode" ) ) {
            setExplodesWhenShot( saveTag.getBoolean( "ShotExplode" ) );
        }
        else if( tag.hasKey( "ShotExplode" ) ) {
            setExplodesWhenShot( tag.getBoolean( "ShotExplode" ) );
        }
        
        getSpecialData().readFromNBT( tag );
        getSpecialData().readFromNBT( saveTag );
        isImmuneToFire = getSpecialData().isImmuneToFire;
        
        if( tag.hasKey( "Fuse" ) ) {
            fuseTime = tag.getShort( "Fuse" );
        }
        if( tag.hasKey( "ExplosionRadius" ) ) {
            explosionRadius = tag.getByte( "ExplosionRadius" );
        }
    }
    
    // Returns the intensity of the creeper's flash when it is ignited.
    @SideOnly( Side.CLIENT )
    @Override
    public float getCreeperFlashIntensity( float partialTick ) {
        return (lastActiveTime + (timeSinceIgnited - lastActiveTime) * partialTick) / (fuseTime - 2);
    }
    
    // Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( _SpecialMobs.debug ) {
            dropRareDrop( Math.max( 0, rand.nextInt( 5 ) - 3 ) );
        }
    }
    
    // Returns the current armor level of this mob.
    @Override
    public int getTotalArmorValue() {
        return Math.min( 20, super.getTotalArmorValue() + getSpecialData().armor );
    }
    
    // Sets this entity on fire.
    @Override
    public void setFire( int time ) {
        if( !getSpecialData().isImmuneToBurning ) {
            super.setFire( time );
        }
    }
    
    // Returns the current armor level of this mob.
    @Override
    public boolean allowLeashing() {
        return !getLeashed() && getSpecialData().allowLeashing;
    }
    
    // Sets the entity inside a web block.
    @Override
    public void setInWeb() {
        if( !getSpecialData().isImmuneToWebs ) {
            super.setInWeb();
        }
    }
    
    // Called when the mob falls. Calculates and applies fall damage.
    @Override
    protected void fall( float distance ) {
        if( !getSpecialData().isImmuneToFalling ) {
            super.fall( distance );
        }
    }
    
    // Return whether this entity should NOT trigger a pressure plate or a tripwire.
    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return getSpecialData().ignorePressurePlates;
    }
    
    // True if the entity can breathe underwater.
    @Override
    public boolean canBreatheUnderwater() {
        return getSpecialData().canBreatheInWater;
    }
    
    // True if the entity can be pushed by flowing water.
    @Override
    public boolean isPushedByWater() {
        return !getSpecialData().ignoreWaterPush;
    }
    
    // Returns true if the potion can be applied.
    @Override
    public boolean isPotionApplicable( PotionEffect effect ) {
        return getSpecialData().isPotionApplicable( effect );
    }
}