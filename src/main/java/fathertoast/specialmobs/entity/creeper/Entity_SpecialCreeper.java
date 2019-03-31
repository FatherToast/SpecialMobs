package fathertoast.specialmobs.entity.creeper;

import fathertoast.specialmobs.*;
import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.config.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;

public
class Entity_SpecialCreeper extends EntityCreeper implements ISpecialMob
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x000000 );
		return info;
	}
	
	private static final String TAG_DRY_EXPLODE          = "DryExplode";
	private static final String TAG_WHEN_BURNING_EXPLODE = "BurningExplode";
	private static final String TAG_WHEN_SHOT_EXPLODE    = "ShotExplode";
	private static final String TAG_FUSE_TIME            = "Fuse";
	private static final String TAG_EXPLOSION_RADIUS     = "ExplosionRadius";
	
	private static final DataParameter< Float > SCALE         = EntityDataManager.createKey( Entity_SpecialCreeper.class, DataSerializers.FLOAT );
	/** The parameter for creeper explosion capabilities. This is a combination of boolean flags. */
	private static final DataParameter< Byte >  EXPLODE_FLAGS = EntityDataManager.createKey( Entity_SpecialCreeper.class, DataSerializers.BYTE );
	
	/** Vanilla data parameter. Already registered. */
	static final DataParameter< Boolean > POWERED = ObfuscationHelper.EntityCreeper_POWERED.get( );
	
	// The bit for "can explode in water".
	private static final byte EXPLODE_FLAG_DEFUSE_IN_WATER = 1;
	// The bit for "explodes when burning".
	private static final byte EXPLODE_FLAG_ON_FIRE         = 1 << 1;
	// The bit for "explodes if shot".
	private static final byte EXPLODE_FLAG_WHEN_SHOT       = 1 << 2;
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( "textures/entity/creeper/creeper.png" )
	};
	
	static
	String GET_TEXTURE_PATH( String type )
	{
		return SpecialMobsMod.TEXTURE_PATH + "creeper/" + type + ".png";
	}
	
	static
	void ADD_BASE_LOOT( LootTableBuilder loot )
	{
		loot.addLootTable( "main", "Base loot", LootTableList.ENTITIES_CREEPER );
	}
	
	// This mob's special mob data.
	private SpecialMobData specialData;
	
	// Fields taking the place of the private fields from EntityCreeper.
	// Ticks since this creeper has ignited.
	public  int     timeSinceIgnited;
	// Last tick's timeSinceIgnited.
	public  int     lastActiveTime;
	// Ticks it takes this creeper to explode.
	public  int     fuseTime        = 30;
	// Explosion radius for this creeper.
	public  int     explosionRadius = 3;
	// Causes the next call to isEntityAlive() to return false. Used to prevent EntityCreeper from exploding instead of
	private boolean playingDead     = false;
	
	public
	Entity_SpecialCreeper( World world ) { super( world ); }
	
	@Override
	protected
	void initEntityAI( )
	{
		super.initEntityAI( );
		initTypeAI( );
	}
	
	protected
	void initTypeAI( ) { }
	
	@Override
	protected
	void entityInit( )
	{
		super.entityInit( );
		specialData = new SpecialMobData( this, SCALE );
		dataManager.register( EXPLODE_FLAGS, (byte) 0 );
	}
	
	@Override
	protected
	void applyEntityAttributes( )
	{
		super.applyEntityAttributes( );
		
		float prevMax = getMaxHealth( );
		applyTypeAttributes( );
		setHealth( getMaxHealth( ) + getHealth( ) - prevMax );
	}
	
	/** Called to modify the mob's attributes based on the variant. */
	protected
	void applyTypeAttributes( ) { }
	
	// START ISpecialMob ================
	
	/**
	 * @return this mob's special data
	 */
	@Override
	public final
	SpecialMobData getSpecialData( ) { return specialData; }
	
	/**
	 * Gets the experience that should be dropped by the entity.
	 */
	@Override
	public final
	int getExperience( ) { return experienceValue; }
	
	/**
	 * Sets the experience that should be dropped by the entity.
	 */
	@Override
	public final
	void setExperience( int xp ) { experienceValue = xp; }
	
	/**
	 * Sets the entity's immunity to fire status.
	 */
	@Override
	public final
	void setImmuneToFire( boolean immune ) { isImmuneToFire = immune; }
	
	/**
	 * @return All the textures for the entity.
	 */
	@Override
	public
	ResourceLocation[] getDefaultTextures( ) { return TEXTURES; }
	
	// END ISpecialMob ================
	
	// Sets this creeper to be unable to explode while wet.
	public
	boolean canNotExplodeWhenWet( ) { return getExplodeFlag( EXPLODE_FLAG_DEFUSE_IN_WATER ); }
	
	// Sets this creeper to be unable to explode while wet.
	public
	void setCanNotExplodeWhenWet( boolean value )
	{
		setExplodeFlag( EXPLODE_FLAG_DEFUSE_IN_WATER, value );
		if( value ) {
			setPathPriority( PathNodeType.WATER, PathNodeType.LAVA.getPriority( ) );
		}
		else {
			setPathPriority( PathNodeType.WATER, PathNodeType.WATER.getPriority( ) );
		}
	}
	
	// Sets this creeper to be unable to explode while wet.
	public
	boolean explodesWhenBurning( ) { return getExplodeFlag( EXPLODE_FLAG_ON_FIRE ); }
	
	// Sets this creeper to be unable to explode while wet.
	public
	void setExplodesWhenBurning( boolean value )
	{
		setExplodeFlag( EXPLODE_FLAG_ON_FIRE, value );
		if( value ) {
			setPathPriority( PathNodeType.DANGER_FIRE, PathNodeType.DAMAGE_FIRE.getPriority( ) );
			setPathPriority( PathNodeType.DAMAGE_FIRE, PathNodeType.BLOCKED.getPriority( ) );
		}
		else {
			setPathPriority( PathNodeType.DANGER_FIRE, PathNodeType.DANGER_FIRE.getPriority( ) );
			setPathPriority( PathNodeType.DAMAGE_FIRE, PathNodeType.DAMAGE_FIRE.getPriority( ) );
		}
	}
	
	// Sets this creeper to be unable to explode while wet.
	public
	boolean explodesWhenShot( ) { return getExplodeFlag( EXPLODE_FLAG_WHEN_SHOT ); }
	
	// Sets this creeper to be unable to explode while wet.
	public
	void setExplodesWhenShot( boolean value ) { setExplodeFlag( EXPLODE_FLAG_WHEN_SHOT, value ); }
	
	// Gets the explode flag value.
	private
	boolean getExplodeFlag( byte flag ) { return (dataManager.get( EXPLODE_FLAGS ) & flag) != 0; }
	
	// Sets the explode flag value.
	private
	void setExplodeFlag( byte flag, boolean value )
	{
		byte allFlags = dataManager.get( EXPLODE_FLAGS );
		if( value == ((allFlags & flag) == 0) ) {
			dataManager.set( EXPLODE_FLAGS, (byte) (allFlags ^ flag) );
		}
	}
	
	@Override
	public
	void onStruckByLightning( EntityLightningBolt lightningBolt )
	{
		super.onStruckByLightning( lightningBolt );
		if( explodesWhenBurning( ) ) {
			extinguish( ); // Make it less likely for the charged form to immediately explode
		}
	}
	
	// Checks whether target entity is alive.
	@Override
	public
	boolean isEntityAlive( )
	{
		if( playingDead )
			return playingDead = false;
		return super.isEntityAlive( );
	}
	
	// Called each tick while this entity exists.
	@Override
	public
	void onUpdate( )
	{
		if( isEntityAlive( ) ) {
			if( isWet( ) && canNotExplodeWhenWet( ) ) {
				setCreeperState( -1 );
			}
			else if( hasIgnited( ) || isBurning( ) && explodesWhenBurning( ) ) {
				setCreeperState( 1 );
			}
			
			lastActiveTime = timeSinceIgnited;
			int creeperState = getCreeperState( );
			if( creeperState > 0 ) {
				if( timeSinceIgnited == 0 ) {
					playSound( SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F );
				}
				onExplodingUpdate( );
			}
			timeSinceIgnited += creeperState;
			if( timeSinceIgnited < 0 ) {
				timeSinceIgnited = 0;
			}
			if( timeSinceIgnited >= fuseTime ) {
				timeSinceIgnited = fuseTime;
				if( !world.isRemote ) {
					boolean powered  = getPowered( );
					boolean griefing = ForgeEventFactory.getMobGriefingEvent( world, this );
					dead = true;
					explodeByType( powered, griefing );
					setDead( );
					spawnLingeringCloud( powered );
				}
			}
		}
		playingDead = true;
		super.onUpdate( );
	}
	
	private
	void spawnLingeringCloud( boolean powered )
	{
		Collection< PotionEffect > activePotions = getActivePotionEffects( );
		
		if( alwaysMakePotionCloud( ) || !activePotions.isEmpty( ) ) {
			EntityAreaEffectCloud potionCloud = new EntityAreaEffectCloud( world, posX, posY, posZ );
			potionCloud.setRadiusOnUse( -0.5F );
			potionCloud.setWaitTime( 10 );
			potionCloud.setDuration( potionCloud.getDuration( ) / 2 );
			potionCloud.setRadiusPerTick( -potionCloud.getRadius( ) / (float) potionCloud.getDuration( ) );
			
			for( PotionEffect potionEffect : activePotions ) {
				potionCloud.addEffect( new PotionEffect( potionEffect ) );
			}
			potionCloudByType( potionCloud, powered );
			
			potionCloud.setOwner( this );
			world.spawnEntity( potionCloud );
		}
	}
	
	/** Called each tick while this creeper is exploding. */
	public
	void onExplodingUpdate( ) { } // Override this to do something while exploding
	
	// The explosion caused by this creeper.
	public
	void explodeByType( boolean powered, boolean griefing )
	{
		float power = (float) explosionRadius * (powered ? 2.0F : 1.0F);
		world.createExplosion( this, posX, posY, posZ, power, griefing );
	}
	
	// Return true here only if you are adding extra potion effects to potionCloudByType().
	public
	boolean alwaysMakePotionCloud( ) { return false; }
	
	// The explosion caused by this creeper.
	public
	void potionCloudByType( EntityAreaEffectCloud potionCloud, boolean powered )
	{
		potionCloud.setRadius( ((float) explosionRadius - 0.5F) * (powered ? 2.0F : 1.0F) );
	}
	
	// Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
	@Override
	public
	boolean attackEntityFrom( DamageSource damageSource, float damage )
	{
		if( damageSource != null && damageSource.getImmediateSource( ) != damageSource.getTrueSource( ) && explodesWhenShot( ) ) {
			ignite( );
		}
		return super.attackEntityFrom( damageSource, damage );
	}
	
	// Overridden to modify attack effects.
	protected
	void onTypeAttack( Entity target ) { }
	
	// START SpecialMobData integration ================
	
	// Called each tick while this entity is alive.
	@Override
	public
	void onLivingUpdate( )
	{
		super.onLivingUpdate( );
		getSpecialData( ).onUpdate( );
	}
	
	// Called to attack the target.
	@Override
	public
	boolean attackEntityAsMob( Entity target )
	{
		if( super.attackEntityAsMob( target ) ) {
			onTypeAttack( target );
			return true;
		}
		return false;
	}
	
	@Override
	public
	float getEyeHeight( ) { return super.getEyeHeight( ); } // Uses boundingbox-scaled eye height
	
	// Saves this entity to NBT.
	@Override
	public
	void writeEntityToNBT( NBTTagCompound tag )
	{
		super.writeEntityToNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		saveTag.setBoolean( TAG_DRY_EXPLODE, canNotExplodeWhenWet( ) );
		saveTag.setBoolean( TAG_WHEN_BURNING_EXPLODE, explodesWhenBurning( ) );
		saveTag.setBoolean( TAG_WHEN_SHOT_EXPLODE, explodesWhenShot( ) );
		
		getSpecialData( ).writeToNBT( saveTag );
		
		tag.setShort( TAG_FUSE_TIME, (short) fuseTime );
		tag.setByte( TAG_EXPLOSION_RADIUS, (byte) explosionRadius );
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		if( saveTag.hasKey( TAG_DRY_EXPLODE ) ) {
			setCanNotExplodeWhenWet( saveTag.getBoolean( TAG_DRY_EXPLODE ) );
		}
		if( saveTag.hasKey( TAG_WHEN_BURNING_EXPLODE ) ) {
			setExplodesWhenBurning( saveTag.getBoolean( TAG_WHEN_BURNING_EXPLODE ) );
		}
		if( saveTag.hasKey( TAG_WHEN_SHOT_EXPLODE ) ) {
			setExplodesWhenShot( saveTag.getBoolean( TAG_WHEN_SHOT_EXPLODE ) );
		}
		
		getSpecialData( ).readFromNBT( saveTag );
		
		if( tag.hasKey( TAG_FUSE_TIME ) ) {
			fuseTime = tag.getShort( TAG_FUSE_TIME );
		}
		if( tag.hasKey( TAG_EXPLOSION_RADIUS ) ) {
			explosionRadius = tag.getByte( TAG_EXPLOSION_RADIUS );
		}
	}
	
	// Sets this entity on fire.
	@Override
	public
	void setFire( int time )
	{
		if( !getSpecialData( ).isImmuneToBurning( ) ) {
			super.setFire( time );
		}
	}
	
	// Returns the current armor level of this mob.
	@Override
	public
	boolean canBeLeashedTo( EntityPlayer player ) { return !getLeashed( ) && getSpecialData( ).allowLeashing( ); }
	
	// Sets the entity inside a web block.
	@Override
	public
	void setInWeb( )
	{
		if( !getSpecialData( ).isImmuneToWebs( ) ) {
			super.setInWeb( );
		}
	}
	
	// Called when the mob falls. Calculates and applies fall damage.
	@Override
	public
	void fall( float distance, float damageMultiplier ) { super.fall( distance, damageMultiplier * getSpecialData( ).getFallDamageMultiplier( ) ); }
	
	// Return whether this entity should NOT trigger a pressure plate or a tripwire.
	@Override
	public
	boolean doesEntityNotTriggerPressurePlate( ) { return getSpecialData( ).ignorePressurePlates( ); }
	
	// True if the entity can breathe underwater.
	@Override
	public
	boolean canBreatheUnderwater( ) { return getSpecialData( ).canBreatheInWater( ); }
	
	// True if the entity can be pushed by flowing water.
	@Override
	public
	boolean isPushedByWater( ) { return !getSpecialData( ).ignoreWaterPush( ); }
	
	// Returns true if the potion can be applied.
	@Override
	public
	boolean isPotionApplicable( PotionEffect effect ) { return getSpecialData( ).isPotionApplicable( effect ); }
	
	// END SpecialMobData integration ================
	
	// Called when this entity is first spawned to initialize it.
	@Override
	@Nullable
	public
	IEntityLivingData onInitialSpawn( DifficultyInstance difficulty, @Nullable IEntityLivingData data )
	{
		data = super.onInitialSpawn( difficulty, data );
		
		if( Entity_SpecialCreeper.POWERED != null && world.isThundering( ) && rand.nextDouble( ) < Config.get( ).CREEPERS.CHARGED_CHANCE ) {
			dataManager.set( Entity_SpecialCreeper.POWERED, true );
		}
		
		return data;
	}
	
	// Returns the intensity of the creeper's flash when it is ignited.
	@SideOnly( Side.CLIENT )
	@Override
	public
	float getCreeperFlashIntensity( float partialTick )
	{
		return (lastActiveTime + (timeSinceIgnited - lastActiveTime) * partialTick) / (fuseTime - 2);
	}
}
