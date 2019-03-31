package fathertoast.specialmobs.entity.witherskeleton;

import fathertoast.specialmobs.*;
import fathertoast.specialmobs.ai.*;
import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.config.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

// Essentially copy-pasted from Entity_SpecialSkeleton with different textures, collision size, family scale, etc.
public
class Entity_SpecialWitherSkeleton extends EntityWitherSkeleton implements ISpecialMob
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x474d4d );
		return info;
	}
	
	private static final int AI_PRIORITY_ATTACK = 4;
	
	private static final DataParameter< Float >   SCALE    = EntityDataManager.createKey( Entity_SpecialWitherSkeleton.class, DataSerializers.FLOAT );
	private static final DataParameter< Boolean > IS_CHILD = EntityDataManager.createKey( Entity_SpecialWitherSkeleton.class, DataSerializers.BOOLEAN );
	
	private static final UUID              BABY_SPEED_BOOST_ID = UUID.fromString( "B9766B59-9566-4402-BC1F-2EE2A276D836" );
	private static final AttributeModifier BABY_SPEED_BOOST    = new AttributeModifier(
		BABY_SPEED_BOOST_ID, "Baby speed boost", 0.5, MobHelper.AttributeModOperation.MULTIPLY_BASE.id
	);
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( "textures/entity/skeleton/wither_skeleton.png" )
	};
	
	static
	String GET_TEXTURE_PATH( String type )
	{
		return SpecialMobsMod.TEXTURE_PATH + "witherskeleton/" + type + ".png";
	}
	
	static
	void ADD_BASE_LOOT( LootTableBuilder loot )
	{
		loot.addLootTable( "main", "Base loot", LootTableList.ENTITIES_WITHER_SKELETON );
	}
	
	/** The entity's currently applied attack ai. */
	private EntityAIBase currentAttackAI;
	
	// This mob's special mob data.
	private SpecialMobData specialData;
	
	public
	Entity_SpecialWitherSkeleton( World world )
	{
		super( world );
		setCollisionSize( 1.0F );
		getSpecialData( ).setImmuneToFire( true );
	}
	
	protected
	void setCollisionSize( float multiplier ) { setSize( 0.7F * multiplier, 2.4F * multiplier ); }
	
	@Override
	protected
	void initEntityAI( )
	{
		super.initEntityAI( );
		
		SpecialMobData data = getSpecialData( );
		data.rangedAttackDamage = 3.0F;
		data.rangedAttackSpread = 14.0F;
		data.rangedWalkSpeed = 1.0F;
		data.rangedAttackCooldown = 20;
		data.rangedAttackMaxCooldown = data.rangedAttackCooldown;
		data.rangedAttackMaxRange = 15.0F;
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
		specialData = new SpecialMobData( this, SCALE, 1.2F );
		dataManager.register( IS_CHILD, Boolean.FALSE );
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
		setRangedAI( 1.0, 20, 0.0F );
	}
	
	// Helper method to set the attack AI more easily.
	protected
	void setRangedAI( double walkSpeed, int cooldownTime )
	{
		setRangedAI( walkSpeed, cooldownTime, getSpecialData( ).rangedAttackMaxRange );
	}
	
	// Helper method to set the attack AI more easily.
	protected
	void setRangedAI( double walkSpeed, int cooldownTime, float range )
	{
		SpecialMobData data = getSpecialData( );
		data.rangedWalkSpeed = (float) walkSpeed;
		data.rangedAttackCooldown = cooldownTime;
		data.rangedAttackMaxCooldown = cooldownTime;
		data.rangedAttackMaxRange = range;
	}
	
	@Override
	public
	void setCombatTask( )
	{
		if( world != null && !world.isRemote ) {
			// Remove the current attack ai
			tasks.removeTask( currentAttackAI );
			
			// Determine the new attack ai to set
			SpecialMobData data   = getSpecialData( );
			ItemStack      weapon = getHeldItemMainhand( );
			if( data.rangedAttackMaxRange > 0.0F && !weapon.isEmpty( ) && weapon.getItem( ) instanceof ItemBow ) {
				currentAttackAI = new EntityAIAttackRangedBow<>(
					this, data.rangedWalkSpeed,
					data.rangedAttackCooldown, data.rangedAttackMaxRange
				);
			}
			else {
				currentAttackAI = new EntityAISpecialAttackMelee<>( this, 1.2, false );
			}
			tasks.addTask( AI_PRIORITY_ATTACK, currentAttackAI );
		}
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
	
	@Override
	protected
	void setEquipmentBasedOnDifficulty( DifficultyInstance difficulty )
	{
		super.setEquipmentBasedOnDifficulty( difficulty );
		
		// Equip a bow weapon if this wither skeleton rolls as a bow user
		if( rand.nextFloat( ) < Config.get( ).WITHER_SKELETONS.BOW_CHANCE ) {
			setItemStackToSlot( EntityEquipmentSlot.MAINHAND, new ItemStack( Items.BOW ) );
		}
	}
	
	// END ISpecialMob ================
	
	// Overridden to modify attack effects.
	protected
	void onTypeAttack( Entity target ) { }
	
	/** Attack the specified entity using a ranged attack. */
	@Override
	public
	void attackEntityWithRangedAttack( EntityLivingBase target, float distanceFactor )
	{
		EntityArrow arrow = getArrow( distanceFactor );
		
		double dX = target.posX - posX;
		double dY = target.getEntityBoundingBox( ).minY + target.height / 3.0F - arrow.posY;
		double dZ = target.posZ - posZ;
		double dH = MathHelper.sqrt( dX * dX + dZ * dZ );
		arrow.shoot(
			dX, dY + dH * 0.2, dZ, 1.6F,
			getSpecialData( ).rangedAttackSpread * (1.0F - 0.2858F * world.getDifficulty( ).getDifficultyId( ))
		);
		
		playSound( SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (rand.nextFloat( ) * 0.4F + 0.8F) );
		world.spawnEntity( arrow );
	}
	
	@Nonnull
	@Override
	protected
	EntityArrow getArrow( float distanceFactor )
	{
		EntityArrow arrow = super.getArrow( distanceFactor );
		arrow.setDamage( getSpecialData( ).getRangedDamage( distanceFactor ) );
		return arrow;
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
		if( super.attackEntityAsMob( target ) ) {
			onTypeAttack( target );
			return true;
		}
		return false;
	}
	
	@Override
	public
	boolean attackEntityFrom( DamageSource source, float amount )
	{
		// Shield blocking logic
		if( !world.isRemote && !isEntityInvulnerable( source ) && amount > 0.0F && !source.isUnblockable( ) ) {
			ItemStack shield    = getHeldItemMainhand( );
			boolean   hasShield = !shield.isEmpty( ) && shield.getItem( ).isShield( shield, this );
			if( !hasShield ) {
				shield = getHeldItemOffhand( );
				hasShield = !shield.isEmpty( ) && shield.getItem( ).isShield( shield, this );
			}
			
			if( hasShield && (source.isProjectile( ) || rand.nextFloat( ) < 0.33F) ) {
				Vec3d hitPos = source.getDamageLocation( );
				if( hitPos != null ) {
					Vec3d lookVec = getLook( 1.0F );
					Vec3d hitVec  = hitPos.subtractReverse( new Vec3d( posX, posY, posZ ) ).normalize( );
					hitVec = new Vec3d( hitVec.x, 0.0, hitVec.z );
					
					// Successful block
					if( hitVec.dotProduct( lookVec ) < 0.0 ) {
						damageShield( amount );
						
						if( !source.isProjectile( ) ) {
							Entity entity = source.getImmediateSource( );
							if( entity instanceof EntityLivingBase ) {
								blockUsingShield( (EntityLivingBase) entity );
							}
						}
						
						playSound( SoundEvents.ITEM_SHIELD_BLOCK, 1.0F, 0.8F + world.rand.nextFloat( ) * 0.4F );
						return false;
					}
				}
			}
		}
		return super.attackEntityFrom( source, amount );
	}
	
	@Override
	public
	float getEyeHeight( )
	{
		float eyeHeight = super.getEyeHeight( ) / getSpecialData( ).getFamilyBaseScale( );
		if( isChild( ) ) {
			eyeHeight -= 0.81F;
		}
		return eyeHeight * getSpecialData( ).getBaseScale( );
	}
	
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
	
	// Called when this entity is first spawned to initialize it.
	@Override
	@Nullable
	public
	IEntityLivingData onInitialSpawn( DifficultyInstance difficulty, @Nullable IEntityLivingData data )
	{
		data = super.onInitialSpawn( difficulty, data );
		
		setChild( rand.nextFloat( ) < Config.get( ).WITHER_SKELETONS.BABY_CHANCE );
		
		return data;
	}
	
	@Override
	public
	boolean isChild( ) { return getDataManager( ).get( IS_CHILD ); }
	
	@Override
	protected
	int getExperiencePoints( EntityPlayer player )
	{
		if( isChild( ) ) {
			experienceValue = (int) ((float) experienceValue * 2.5F);
		}
		return super.getExperiencePoints( player );
	}
	
	public
	void setChild( boolean child )
	{
		boolean wasChild = getDataManager( ).get( IS_CHILD );
		if( wasChild == child ) {
			return;
		}
		getDataManager( ).set( IS_CHILD, child );
		
		if( world != null && !world.isRemote ) {
			IAttributeInstance attribute = getEntityAttribute( SharedMonsterAttributes.MOVEMENT_SPEED );
			attribute.removeModifier( BABY_SPEED_BOOST );
			
			if( child ) {
				attribute.applyModifier( BABY_SPEED_BOOST );
			}
		}
		setCollisionSize( child ? 0.5F : 1.0F );
	}
	
	@Override
	public
	void notifyDataManagerChange( DataParameter< ? > key )
	{
		if( IS_CHILD.equals( key ) ) {
			setCollisionSize( isChild( ) ? 0.5F : 1.0F );
		}
		super.notifyDataManagerChange( key );
	}
}
