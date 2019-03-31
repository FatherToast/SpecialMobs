package fathertoast.specialmobs.entity.lavaslime;

import fathertoast.specialmobs.*;
import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public
class Entity_SpecialLavaSlime extends EntityMagmaCube implements ISpecialMob
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xfcfc00 );
		return info;
	}
	
	private static final DataParameter< Float > SCALE = EntityDataManager.createKey( Entity_SpecialLavaSlime.class, DataSerializers.FLOAT );
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( "textures/entity/slime/magmacube.png" )
	};
	
	static
	String GET_TEXTURE_PATH( String type )
	{
		return SpecialMobsMod.TEXTURE_PATH + "lavaslime/" + type + ".png";
	}
	
	static
	void ADD_BASE_LOOT( LootTableBuilder loot )
	{
		loot.addLootTable( "main", "Base loot", LootTableList.ENTITIES_MAGMA_CUBE );
	}
	
	protected int slimeExperienceValue = 0;
	
	// This mob's special mob data.
	private SpecialMobData specialData;
	
	public
	Entity_SpecialLavaSlime( World world )
	{
		super( world );
		getSpecialData( ).setImmuneToFire( true );
	}
	
	// Only used to create the slimes that are spawned in onDeath().
	@Override
	protected final
	EntitySlime createInstance( )
	{
		EntitySlime newSlime = getSplitSlime( );
		if( newSlime instanceof ISpecialMob ) {
			ISpecialMob specialSlime = (ISpecialMob) newSlime;
			specialSlime.getSpecialData( ).copyDataFrom( this, false );
		}
		
		return newSlime;
	}
	
	/** Creates the slimes that split off of this one on death. */
	protected
	EntitySlime getSplitSlime( ) { return new Entity_SpecialLavaSlime( world ); }
	
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
	}
	
	@Override
	protected
	void applyEntityAttributes( )
	{
		super.applyEntityAttributes( );
		getAttributeMap( ).registerAttribute( SharedMonsterAttributes.ATTACK_DAMAGE );
		
		float prevMax = getMaxHealth( );
		applyTypeAttributes( );
		setHealth( getMaxHealth( ) + getHealth( ) - prevMax );
		setExperience( slimeExperienceValue ); // Update underlying experience value
	}
	
	/** Called to modify the mob's attributes based on the variant. */
	protected
	void applyTypeAttributes( ) { }
	
	@Override
	public
	void notifyDataManagerChange( DataParameter< ? > key )
	{
		super.notifyDataManagerChange( key );
		
		int size = getSlimeSize( );
		setSize( size * typeSizeMultiplier( ), size * typeSizeMultiplier( ) );
	}
	
	@Override
	protected
	void setSlimeSize( int size, boolean resetHealth )
	{
		super.setSlimeSize( size, resetHealth );
		
		setSize( size * typeSizeMultiplier( ), size * typeSizeMultiplier( ) );
		setPosition( posX, posY, posZ );
		getEntityAttribute( SharedMonsterAttributes.MAX_HEALTH ).setBaseValue( Math.max( size * size, 2.0 ) );
		getEntityAttribute( SharedMonsterAttributes.ATTACK_DAMAGE ).setBaseValue( super.getAttackStrength( ) );
		getEntityAttribute( SharedMonsterAttributes.ARMOR ).setBaseValue( size * 3.0 );
		adjustTypeAttributesForSize( size );
		
		if( resetHealth ) {
			setHealth( getMaxHealth( ) );
		}
		
		setExperience( slimeExperienceValue ); // Update underlying experience value
	}
	
	/** Entity size scale must be modified here for slimes. */
	protected
	float typeSizeMultiplier( ) { return 0.51F; }
	
	/** Called to modify the mob's attributes based on the variant. Health, armor, damage, and speed must be modified here for magma cubes. */
	protected
	void adjustTypeAttributesForSize( int size ) { }
	
	@Override
	protected
	ResourceLocation getLootTable( ) { return isSmallSlime( ) ? LootTableList.ENTITIES_MAGMA_CUBE : LootTableList.EMPTY; }
	
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
	int getExperience( ) { return slimeExperienceValue; }
	
	/**
	 * Sets the experience that should be dropped by the entity.
	 */
	@Override
	public final
	void setExperience( int xp )
	{
		slimeExperienceValue = xp;
		experienceValue = getSlimeSize( ) + xp;
	}
	
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
	
	// Overridden to modify attack effects.
	protected
	void onTypeAttack( Entity target ) { }
	
	@Override
	protected
	void dealDamage( EntityLivingBase target )
	{
		double reachSq = width * width + target.width * target.width;
		if( canEntityBeSeen( target ) && getDistanceSq( target ) < reachSq && attackEntityAsMob( target ) ) {
			playSound( SoundEvents.ENTITY_SLIME_ATTACK, 1.0F, (rand.nextFloat( ) - rand.nextFloat( )) * 0.2F + 1.0F );
		}
	}
	
	@Override
	protected
	int getAttackStrength( )
	{
		return (int) getEntityAttribute( SharedMonsterAttributes.ATTACK_DAMAGE ).getAttributeValue( );
	}
	
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
		if( MobHelper.attackEntityAsMob( this, target ) ) {
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
		getSpecialData( ).writeToNBT( saveTag );
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		getSpecialData( ).readFromNBT( saveTag );
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
}
