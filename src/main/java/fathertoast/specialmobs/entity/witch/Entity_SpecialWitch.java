package fathertoast.specialmobs.entity.witch;

import fathertoast.specialmobs.*;
import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public
class Entity_SpecialWitch extends EntityWitch implements ISpecialMob
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x51a03e );
		return info;
	}
	
	private static final String TAG_POTION_USE_TIME = "PotionUseTimer";
	private static final String TAG_SHEATHED_ITEM   = "SheathedItem";
	
	private static final DataParameter< Float > SCALE = EntityDataManager.createKey( Entity_SpecialWitch.class, DataSerializers.FLOAT );
	
	private static final UUID              DRINKING_POTION_UUID     = UUID.fromString( "5CD17E52-A79A-43D3-A529-90FDE04B181E" );
	private static final AttributeModifier DRINKING_POTION_MODIFIER = new AttributeModifier(
		DRINKING_POTION_UUID, "Drinking speed penalty", -0.25, MobHelper.AttributeModOperation.ADDITION.id
	).setSaved( false );
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( "textures/entity/witch.png" )
	};
	
	static
	String GET_TEXTURE_PATH( String type )
	{
		return SpecialMobsMod.TEXTURE_PATH + "witch/" + type + ".png";
	}
	
	static
	void ADD_BASE_LOOT( LootTableBuilder loot )
	{
		loot.addLootTable( "main", "Base loot", LootTableList.ENTITIES_WITCH );
	}
	
	{
		// Prevent vanilla witch potion-drinking code from running
		ObfuscationHelper.EntityWitch_potionUseTimer.set( this, Integer.MAX_VALUE );
	}
	
	// Used to prevent vanilla code from handling potion-drinking.
	private boolean fakeDrinkingPotion;
	
	// This mob's special mob data.
	private SpecialMobData specialData;
	
	protected int potionDrinkTimer;
	protected int potionThrowTimer;
	
	// While the witch is drinking a potion, it stores its actual held item here.
	public ItemStack sheathedItem = ItemStack.EMPTY;
	
	public
	Entity_SpecialWitch( World world ) { super( world ); }
	
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
	
	// Called each server tick to update potion-drinking behavior.
	public
	void drinkPotionUpdate( )
	{
		potionThrowTimer--;
		
		if( isDrinkingPotion( ) ) {
			potionDrinkTimer--;
			
			// Complete potion drinking
			if( potionDrinkTimer <= 0 ) {
				ItemStack drinkingItem = getHeldItemMainhand( );
				usePotion( ItemStack.EMPTY );
				
				if( drinkingItem.getItem( ) == Items.POTIONITEM ) {
					List< PotionEffect > list = PotionUtils.getEffectsFromStack( drinkingItem );
					if( !list.isEmpty( ) ) {
						for( PotionEffect potioneffect : list ) {
							addPotionEffect( new PotionEffect( potioneffect ) );
						}
					}
				}
			}
		}
		else {
			tryDrinkPotion( );
		}
	}
	
	/**
	 * Attack the specified entity using a ranged attack.
	 */
	@Override
	public
	void attackEntityWithRangedAttack( EntityLivingBase target, float distanceFactor )
	{
		if( !isDrinkingPotion( ) ) {
			double dX = target.posX + target.motionX - posX;
			double dY = target.posY + (double) target.getEyeHeight( ) - 1.1 - posY;
			double dZ = target.posZ + target.motionZ - posZ;
			float  dH = MathHelper.sqrt( dX * dX + dZ * dZ );
			
			ItemStack potion = pickThrownPotion( target, distanceFactor, dH );
			
			if( !potion.isEmpty( ) ) {
				EntityPotion thrownPotion = new EntityPotion( world, this, potion );
				thrownPotion.rotationPitch += 20.0F;
				thrownPotion.shoot( dX, dY + (double) (dH * 0.2F), dZ, 0.75F, 8.0F );
				world.playSound( null, posX, posY, posZ, SoundEvents.ENTITY_WITCH_THROW, getSoundCategory( ), 1.0F, 0.8F + rand.nextFloat( ) * 0.4F );
				world.spawnEntity( thrownPotion );
			}
		}
	}
	
	// Changes the default splash potion into another befitting the situation.
	private
	ItemStack pickThrownPotion( EntityLivingBase target, float distanceFactor, float distance )
	{
		ItemStack potion;
		
		// Default potion
		if( target.isEntityUndead( ) ) {
			potion = makeSplashPotion( PotionTypes.HEALING );
		}
		else {
			potion = makeSplashPotion( PotionTypes.HARMING );
		}
		
		// Special cases potions
		if( distance >= 8.0F && !target.isPotionActive( MobEffects.SLOWNESS ) ) {
			potion = makeSplashPotion( PotionTypes.SLOWNESS );
		}
		else if( target.getHealth( ) >= 8.0F && !target.isPotionActive( MobEffects.POISON ) ) {
			potion = makeSplashPotion( PotionTypes.POISON );
		}
		else if( distance <= 3.0F && !target.isPotionActive( MobEffects.WEAKNESS ) && rand.nextFloat( ) < 0.25F ) {
			potion = makeSplashPotion( PotionTypes.WEAKNESS );
		}
		
		// Let the variant type change the choice or cancel potion throwing
		return pickThrownPotionByType( potion, target, distanceFactor, distance );
	}
	
	// Overridden to modify potion attacks. Return ItemStack.EMPTY to cancel the potion throw.
	protected
	ItemStack pickThrownPotionByType( ItemStack potion, EntityLivingBase target, float distanceFactor, float distance ) { return potion; }
	
	// Called when the witch is looking for a potion to drink.
	public
	void tryDrinkPotion( )
	{
		if( potionThrowTimer <= 0 ) {
			if( rand.nextFloat( ) < 0.15F && (isBurning( ) || getLastDamageSource( ) != null && getLastDamageSource( ).isFireDamage( )) && !isPotionActive( MobEffects.FIRE_RESISTANCE ) ) {
				usePotion( makePotion( PotionTypes.FIRE_RESISTANCE ) );
			}
			else if( rand.nextFloat( ) < 0.15F && isInsideOfMaterial( Material.WATER ) && !isPotionActive( MobEffects.WATER_BREATHING ) ) {
				usePotion( makePotion( PotionTypes.WATER_BREATHING ) );
			}
			else if( rand.nextFloat( ) < 0.05F && getHealth( ) < getMaxHealth( ) ) {
				usePotion( makePotion( PotionTypes.HEALING ) );
			}
			else if( rand.nextFloat( ) < 0.5F && getAttackTarget( ) != null && !isPotionActive( MobEffects.SPEED ) && getAttackTarget( ).getDistanceSq( this ) > 121.0 ) {
				usePotion( makeSplashPotion( PotionTypes.SWIFTNESS ) );
			}
			else {
				tryDrinkPotionByType( );
			}
		}
	}
	
	// Overridden to add additional potions this witch can drink. Sometimes the main method is overridden instead.
	protected
	void tryDrinkPotionByType( ) { }
	
	// START SpecialMobData integration ================
	
	// Called each tick while this entity is alive.
	@Override
	public
	void onLivingUpdate( )
	{
		if( !world.isRemote ) {
			drinkPotionUpdate( );
			fakeDrinkingPotion = true;
		}
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
	float getEyeHeight( ) { return super.getEyeHeight( ) * getSpecialData( ).getBaseScaleForPreScaledValues( ); }
	
	// Saves this entity to NBT.
	@Override
	public
	void writeEntityToNBT( NBTTagCompound tag )
	{
		super.writeEntityToNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		getSpecialData( ).writeToNBT( saveTag );
		
		saveTag.setByte( TAG_POTION_USE_TIME, (byte) potionDrinkTimer );
		if( potionDrinkTimer > 0 ) {
			saveTag.setTag( TAG_SHEATHED_ITEM, sheathedItem.serializeNBT( ) );
		}
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		getSpecialData( ).readFromNBT( saveTag );
		
		if( saveTag.hasKey( TAG_POTION_USE_TIME, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			potionDrinkTimer = saveTag.getByte( TAG_POTION_USE_TIME );
		}
		if( potionDrinkTimer <= 0 || !saveTag.hasKey( TAG_SHEATHED_ITEM, tag.getId( ) ) ) {
			sheathedItem = ItemStack.EMPTY;
		}
		else {
			sheathedItem = new ItemStack( saveTag.getCompoundTag( TAG_SHEATHED_ITEM ) );
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
	
	// Overridden to prevent vanilla witch code from handling potion-drinking.
	@Override
	public
	boolean isDrinkingPotion( )
	{
		if( fakeDrinkingPotion ) {
			fakeDrinkingPotion = false;
			return true;
		}
		return super.isDrinkingPotion( );
	}
	
	// Create a regular potion.
	public
	ItemStack makePotion( PotionType type ) { return PotionUtils.addPotionToItemStack( new ItemStack( Items.POTIONITEM ), type ); }
	
	// Create a regular potion with custom effects.
	public
	ItemStack makePotion( Collection< PotionEffect > effects ) { return PotionUtils.appendEffects( new ItemStack( Items.POTIONITEM ), effects ); }
	
	// Create a splash potion on self.
	public
	ItemStack makeSplashPotion( PotionType type ) { return PotionUtils.addPotionToItemStack( new ItemStack( Items.SPLASH_POTION ), type ); }
	
	// Create a splash potion on self with custom effects.
	public
	ItemStack makeSplashPotion( Collection< PotionEffect > effects ) { return PotionUtils.appendEffects( new ItemStack( Items.SPLASH_POTION ), effects ); }
	
	// Create a lingering splash potion.
	public
	ItemStack makeLingeringPotion( PotionType type ) { return PotionUtils.addPotionToItemStack( new ItemStack( Items.LINGERING_POTION ), type ); }
	
	// Create a lingering splash potion with custom effects.
	public
	ItemStack makeLingeringPotion( Collection< PotionEffect > effects ) { return PotionUtils.appendEffects( new ItemStack( Items.LINGERING_POTION ), effects ); }
	
	// Use a potion on self.
	public
	void usePotion( ItemStack potion )
	{
		if( potion.isEmpty( ) ) {
			// Cancel drinking the current potion and re-equip the sheathed item
			if( isDrinkingPotion( ) ) {
				potionDrinkTimer = 0;
				setDrinkingPotion( false );
				
				setItemStackToSlot( EntityEquipmentSlot.MAINHAND, sheathedItem );
				sheathedItem = ItemStack.EMPTY;
				
				getEntityAttribute( SharedMonsterAttributes.MOVEMENT_SPEED ).removeModifier( DRINKING_POTION_MODIFIER );
			}
		}
		else if( potion.getItem( ) == Items.POTIONITEM ) {
			// It is a normal potion, start drinking and sheathe the held item
			sheathedItem = getHeldItemMainhand( );
			setItemStackToSlot( EntityEquipmentSlot.MAINHAND, potion );
			
			potionDrinkTimer = getHeldItemMainhand( ).getMaxItemUseDuration( );
			setDrinkingPotion( true );
			
			world.playSound( null, posX, posY, posZ, SoundEvents.ENTITY_WITCH_DRINK, getSoundCategory( ), 1.0F, 0.8F + rand.nextFloat( ) * 0.4F );
			IAttributeInstance attribute = getEntityAttribute( SharedMonsterAttributes.MOVEMENT_SPEED );
			attribute.removeModifier( DRINKING_POTION_MODIFIER );
			attribute.applyModifier( DRINKING_POTION_MODIFIER );
		}
		else {
			// It is a splash or lingering potion, throw it straight down to apply to self
			potionThrowTimer = 40;
			
			SoundEvent sound = null;
			if( potion.getItem( ) == Items.SPLASH_POTION ) {
				sound = SoundEvents.ENTITY_SPLASH_POTION_THROW;
			}
			else if( potion.getItem( ) == Items.LINGERING_POTION ) {
				sound = SoundEvents.ENTITY_LINGERINGPOTION_THROW;
			}
			if( sound != null ) {
				world.playSound( null, posX, posY, posZ, sound, getSoundCategory( ), 0.5F, 0.4F / (rand.nextFloat( ) * 0.4F + 0.8F) );
				
				EntityPotion thrownPotion = new EntityPotion( world, this, potion );
				thrownPotion.rotationPitch += 20.0F;
				thrownPotion.motionX = thrownPotion.motionZ = 0.0;
				thrownPotion.motionY = -0.2;
				world.spawnEntity( thrownPotion );
			}
		}
	}
}
