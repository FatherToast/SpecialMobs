package fathertoast.specialmobs.entity.ghast;

import fathertoast.specialmobs.*;
import fathertoast.specialmobs.ai.*;
import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public
class Entity_SpecialGhast extends EntityGhast implements ISpecialMob, IRangedAttackMob
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xbcbcbc );
		return info;
	}
	
	private static final int AI_PRIORITY_ATTACK = 4;
	
	private static final DataParameter< Float > SCALE = EntityDataManager.createKey( Entity_SpecialGhast.class, DataSerializers.FLOAT );
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( "textures/entity/ghast/ghast.png" ),
		null,
		new ResourceLocation( "textures/entity/ghast/ghast_shooting.png" )
	};
	
	static
	String GET_TEXTURE_PATH( String type )
	{
		return SpecialMobsMod.TEXTURE_PATH + "ghast/" + type + ".png";
	}
	
	static
	void ADD_BASE_LOOT( LootTableBuilder loot )
	{
		loot.addLootTable( "main", "Base loot", LootTableList.ENTITIES_GHAST );
	}
	
	/** The entity's currently applied attack ai. */
	private EntityAIBase currentAttackAI;
	
	// This mob's special mob data.
	private SpecialMobData specialData;
	
	public
	Entity_SpecialGhast( World world )
	{
		super( world );
		moveHelper = new EntityMoveHelperFlying( this );
		getSpecialData( ).setImmuneToFire( true );
	}
	
	@Override
	protected
	void initEntityAI( )
	{
		tasks.addTask( 5, new EntityAIFlyingWander<>( this ) );
		tasks.addTask( 7, new EntityAIFlyingLookIdle<>( this ) );
		targetTasks.addTask( 1, new EntityAIFindEntityNearestPlayer( this ) );
		
		SpecialMobData data = getSpecialData( );
		data.rangedAttackSpread = 0.0F;
		data.rangedAttackCooldown = 20;
		data.rangedAttackMaxCooldown = 60;
		data.rangedAttackMaxRange = 64.0F;
		initTypeAI( );
		setCombatTask( );
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
	
	// Helper method to set the attack AI more easily.
	protected
	void disableRangedAI( )
	{
		getSpecialData( ).rangedAttackMaxRange = 0.0F;
	}
	
	public
	void setCombatTask( )
	{
		if( world != null && !world.isRemote ) {
			// Remove the current attack ai
			tasks.removeTask( currentAttackAI );
			
			// Determine the new attack ai to set
			SpecialMobData data = getSpecialData( );
			if( data.rangedAttackMaxRange > 0.0F ) {
				currentAttackAI = new EntityAIFlyingAttackRanged<>( this );
			}
			else {
				currentAttackAI = new EntityAIFlyingAttackMelee<>( this );
			}
			tasks.addTask( AI_PRIORITY_ATTACK, currentAttackAI );
		}
	}
	
	@Override
	protected
	void applyEntityAttributes( )
	{
		super.applyEntityAttributes( );
		getAttributeMap( ).registerAttribute( SharedMonsterAttributes.ATTACK_DAMAGE );
		getEntityAttribute( SharedMonsterAttributes.ATTACK_DAMAGE ).setBaseValue( 4.0 );
		getEntityAttribute( SharedMonsterAttributes.MOVEMENT_SPEED ).setBaseValue( 0.1 );
		
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
	
	// Overridden to modify attack effects.
	protected
	void onTypeAttack( Entity target ) { }
	
	/**
	 * Attack the specified entity using a ranged attack.
	 */
	@Override
	public
	void attackEntityWithRangedAttack( EntityLivingBase target, float distanceFactor )
	{
		world.playEvent( null, 1016, new BlockPos( this ), 0 );
		
		Vec3d lookVec = getLook( 1.0F );
		
		double dX = target.posX - (posX + lookVec.x * width);
		double dY = target.getEntityBoundingBox( ).minY + target.height / 2.0F - (0.5 + posY + height / 2.0F);
		double dZ = target.posZ - (posZ + lookVec.z * width);
		
		float accelVariance = MathHelper.sqrt( MathHelper.sqrt( getDistanceSq( target ) ) ) * getSpecialData( ).rangedAttackSpread / 28.0F;
		
		EntityLargeFireball fireball = new EntityLargeFireball(
			world, this,
			dX + getRNG( ).nextGaussian( ) * accelVariance,
			dY,
			dZ + getRNG( ).nextGaussian( ) * accelVariance
		);
		fireball.explosionPower = getFireballStrength( );
		
		fireball.posX = posX + lookVec.x * width;
		fireball.posY = posY + height / 2.0F + 0.5;
		fireball.posZ = posZ + lookVec.z * width;
		
		world.spawnEntity( fireball );
	}
	
	@Override
	public
	void setSwingingArms( boolean swingingArms ) { setAttacking( swingingArms ); }
	
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
	float getEyeHeight( ) { return super.getEyeHeight( ) * getSpecialData( ).getBaseScaleForPreScaledValues( ); }
	
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
		
		setCombatTask( );
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
