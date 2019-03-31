package fathertoast.specialmobs.entity.blaze;

import fathertoast.specialmobs.*;
import fathertoast.specialmobs.ai.*;
import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public
class Entity_SpecialBlaze extends EntityBlaze implements ISpecialMob, IRangedAttackMob
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xfff87e );
		return info;
	}
	
	private static final String TAG_BURST_COUNT = "FireballBurstCount";
	private static final String TAG_BURST_DELAY = "FireballBurstDelay";
	
	private static final int AI_PRIORITY_ATTACK = 4;
	
	private static final DataParameter< Float > SCALE = EntityDataManager.createKey( Entity_SpecialBlaze.class, DataSerializers.FLOAT );
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( "textures/entity/blaze.png" )
	};
	
	static
	String GET_TEXTURE_PATH( String type )
	{
		return SpecialMobsMod.TEXTURE_PATH + "blaze/" + type + ".png";
	}
	
	static
	void ADD_BASE_LOOT( LootTableBuilder loot )
	{
		loot.addLootTable( "main", "Base loot", LootTableList.ENTITIES_BLAZE );
	}
	
	// This mob's special mob data.
	private SpecialMobData specialData;
	
	// The amount of fireballs in each burst.
	public int fireballBurstCount;
	// The ticks between each shot in a burst.
	public int fireballBurstDelay;
	
	public
	Entity_SpecialBlaze( World world )
	{
		super( world );
		getSpecialData( ).setImmuneToFire( true );
		getSpecialData( ).setDamagedByWater( true );
	}
	
	@Override
	protected
	void initEntityAI( )
	{
		// Blaze attack ai is melee + ranged combined, so we don't need to store a reference for swapping
		tasks.addTask( AI_PRIORITY_ATTACK, new EntityAISpecialAttackBlaze( this ) );
		tasks.addTask( 5, new EntityAIMoveTowardsRestriction( this, 1.0 ) );
		tasks.addTask( 7, new EntityAIWanderAvoidWater( this, 1.0, 0.0F ) );
		tasks.addTask( 8, new EntityAIWatchClosest( this, EntityPlayer.class, 8.0F ) );
		tasks.addTask( 8, new EntityAILookIdle( this ) );
		targetTasks.addTask( 1, new EntityAIHurtByTarget( this, true ) );
		targetTasks.addTask( 2, new EntityAINearestAttackableTarget<>( this, EntityPlayer.class, true ) );
		
		// Blaze does not swap out its attack ai or need to reload its attack ai, so this is fine to set defaults
		setRangedAI( 3, 6, 60, 100, 48.0F );
		initTypeAI( );
	}
	
	/** Called to modify the mob's ai based on the variant. */
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
		
		float prevMax = getMaxHealth( );
		applyTypeAttributes( );
		setHealth( getMaxHealth( ) + getHealth( ) - prevMax );
	}
	
	/** Called to modify the mob's attributes based on the variant. */
	protected
	void applyTypeAttributes( ) { }
	
	// Helper method to set the attack AI more easily.
	protected
	void disableRangedAI( )
	{
		setRangedAI( 0, 6, 60, 100, 0.0F );
	}
	
	// Helper method to set the attack AI more easily.
	protected
	void setRangedAI( int burstCount, int burstDelay, int chargeTime, int cooldownTime, float range )
	{
		fireballBurstCount = burstCount;
		fireballBurstDelay = burstDelay;
		
		SpecialMobData data = getSpecialData( );
		data.rangedAttackCooldown = cooldownTime;
		data.rangedAttackMaxCooldown = cooldownTime + chargeTime;
		data.rangedAttackMaxRange = range;
	}
	
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
	
	// Overridden to modify attack effects.
	protected
	void onTypeAttack( Entity target ) { }
	
	@Override
	public
	void attackEntityWithRangedAttack( EntityLivingBase target, float distanceFactor )
	{
		world.playEvent( null, 1018, new BlockPos( this ), 0 );
		
		double dX = target.posX - posX;
		double dY = target.getEntityBoundingBox( ).minY + target.height / 2.0F - (posY + height / 2.0F);
		double dZ = target.posZ - posZ;
		
		float accelVariance = MathHelper.sqrt( MathHelper.sqrt( getDistanceSq( target ) ) ) * getSpecialData( ).rangedAttackSpread / 28.0F;
		
		EntitySmallFireball fireball = new EntitySmallFireball(
			world, this,
			dX + getRNG( ).nextGaussian( ) * accelVariance,
			dY,
			dZ + getRNG( ).nextGaussian( ) * accelVariance
		);
		fireball.posY = posY + height / 2.0F + 0.5;
		world.spawnEntity( fireball );
	}
	
	@Override
	public
	void setSwingingArms( boolean swingingArms ) { }
	
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
		getSpecialData( ).writeToNBT( saveTag );
		
		saveTag.setByte( TAG_BURST_COUNT, (byte) fireballBurstCount );
		saveTag.setByte( TAG_BURST_DELAY, (byte) fireballBurstDelay );
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		getSpecialData( ).readFromNBT( saveTag );
		
		if( saveTag.hasKey( TAG_BURST_COUNT, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			fireballBurstCount = saveTag.getByte( TAG_BURST_COUNT );
		}
		if( saveTag.hasKey( TAG_BURST_DELAY, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			fireballBurstDelay = saveTag.getByte( TAG_BURST_DELAY );
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
}
