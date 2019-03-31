package fathertoast.specialmobs.entity.zombie;

import fathertoast.specialmobs.*;
import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.config.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
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
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.ForgeModContainer;

import javax.annotation.Nullable;
import java.util.List;

public
class Entity_SpecialZombie extends EntityZombie implements ISpecialMob, IRangedAttackMob
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x799c65 );
		return info;
	}
	
	private static final int AI_PRIORITY_ATTACK = 2;
	
	private static final DataParameter< Float > SCALE = EntityDataManager.createKey( Entity_SpecialZombie.class, DataSerializers.FLOAT );
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( "textures/entity/zombie/zombie.png" )
	};
	
	static
	String GET_TEXTURE_PATH( String type )
	{
		return SpecialMobsMod.TEXTURE_PATH + "zombie/" + type + ".png";
	}
	
	static
	void ADD_BASE_LOOT( LootTableBuilder loot )
	{
		loot.addLootTable( "main", "Base loot", LootTableList.ENTITIES_ZOMBIE );
	}
	
	/** The entity's currently applied attack ai. */
	private EntityAIBase currentAttackAI;
	
	// This mob's special mob data.
	private SpecialMobData specialData;
	
	public
	Entity_SpecialZombie( World world ) { super( world ); }
	
	@Override
	protected
	void initEntityAI( )
	{
		tasks.addTask( 0, new EntityAISwimming( this ) );
		tasks.addTask( 5, new EntityAIMoveTowardsRestriction( this, 1.0 ) );
		tasks.addTask( 7, new EntityAIWanderAvoidWater( this, 1.0 ) );
		tasks.addTask( 8, new EntityAIWatchClosest( this, EntityPlayer.class, 8.0F ) );
		tasks.addTask( 8, new EntityAILookIdle( this ) );
		applyEntityAI( ); // Zombie targeting tasks
		
		SpecialMobData data = getSpecialData( );
		data.rangedAttackDamage = 2.0F;
		data.rangedAttackSpread = 21.0F;
		data.rangedWalkSpeed = 0.8F;
		data.rangedAttackCooldown = 40;
		data.rangedAttackMaxCooldown = data.rangedAttackCooldown;
		data.rangedAttackMaxRange = 10.0F;
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
				currentAttackAI = new EntityAIZombieAttack( this, 1.0, false );
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
		
		if( rand.nextFloat( ) < Config.get( ).ZOMBIES.BOW_CHANCE ) {
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
		double dY = target.getEntityBoundingBox( ).minY + target.height / 2.0F - arrow.posY;
		double dZ = target.posZ - posZ;
		double dH = MathHelper.sqrt( dX * dX + dZ * dZ );
		arrow.shoot(
			dX, dY + dH * 0.2, dZ, 1.6F,
			getSpecialData( ).rangedAttackSpread * (1.0F - 0.2858F * world.getDifficulty( ).getDifficultyId( ))
		);
		
		playSound( SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (rand.nextFloat( ) * 0.4F + 0.8F) );
		world.spawnEntity( arrow );
	}
	
	protected
	EntityArrow getArrow( float distanceFactor )
	{
		EntityArrow arrow;
		
		ItemStack quiverSlot = getItemStackFromSlot( EntityEquipmentSlot.OFFHAND );
		if( quiverSlot.getItem( ) == Items.SPECTRAL_ARROW ) {
			arrow = new EntitySpectralArrow( world, this );
			arrow.setEnchantmentEffectsFromEntity( this, distanceFactor );
		}
		else {
			arrow = new EntityTippedArrow( world, this );
			arrow.setEnchantmentEffectsFromEntity( this, distanceFactor );
			if( quiverSlot.getItem( ) == Items.TIPPED_ARROW ) {
				((EntityTippedArrow) arrow).setPotionEffect( quiverSlot );
			}
		}
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
			
			if( hasShield && rand.nextFloat( ) < 0.33F ) {
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
		return super.getEyeHeight( ) * getSpecialData( ).getBaseScaleForPreScaledValues( );
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
		if( data == null ) {
			data = new NonnullGroupData( );
		}
		data = super.onInitialSpawn( difficulty, data );
		
		setChild( rand.nextFloat( ) < ForgeModContainer.zombieBabyChance );
		
		// Handle chicken jockey stuff
		if( isChild( ) ) {
			if( world.rand.nextFloat( ) < Config.get( ).ZOMBIES.CHICKEN_JOCKEY_CHANCE ) {
				List< EntityChicken > list = world.getEntitiesWithinAABB(
					EntityChicken.class, getEntityBoundingBox( ).grow( 5.0, 3.0, 5.0 ), EntitySelectors.IS_STANDALONE
				);
				if( !list.isEmpty( ) ) {
					EntityChicken entitychicken = list.get( 0 );
					entitychicken.setChickenJockey( true );
					startRiding( entitychicken );
				}
			}
			else if( world.rand.nextFloat( ) < Config.get( ).ZOMBIES.CHICKEN_JOCKEY_FORCED_CHANCE ) {
				EntityChicken chickenMount = new EntityChicken( world );
				chickenMount.setLocationAndAngles( posX, posY, posZ, rotationYaw, 0.0F );
				chickenMount.onInitialSpawn( difficulty, null );
				chickenMount.setChickenJockey( true );
				world.spawnEntity( chickenMount );
				startRiding( chickenMount );
			}
		}
		
		return data;
	}
	
	@Override
	public
	void setItemStackToSlot( EntityEquipmentSlot slot, ItemStack stack )
	{
		super.setItemStackToSlot( slot, stack );
		
		if( !world.isRemote && slot == EntityEquipmentSlot.MAINHAND ) {
			setCombatTask( );
		}
	}
	
	@Override
	public
	void setSwingingArms( boolean swingingArms )
	{
		//super.setArmsRaised( swingingArms );
	}
	
	// Used to cancel vanilla logic for baby zombies.
	private static
	class NonnullGroupData implements IEntityLivingData { }
}
