package fathertoast.specialmobs.entity.pigzombie;

import fathertoast.specialmobs.ai.*;
import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.config.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.entity.projectile.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Biomes;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public
class EntityFishingPigZombie extends Entity_SpecialPigZombie implements IAngler
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x2d41f4 );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FISHING;
		return info;
	}
	
	private static final DataParameter< Boolean > IS_LINE_OUT = EntityDataManager.createKey( EntityFishingPigZombie.class, DataSerializers.BOOLEAN );
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Fish", Items.COOKED_FISH );
		loot.addPool(
			new LootPoolBuilder( "rare" )
				.addConditions( LootPoolBuilder.RARE_CONDITIONS )
				.addEntry( new LootEntryItemBuilder( "Fishing rod", Items.FISHING_ROD ).enchant( 30, true ).toLootEntry( ) )
				.toLootPool( )
		);
	}
	
	// Ticks until this angler can cast its lure again.
	private int rodTime = 0;
	
	// This angler's lure entity.
	private EntitySpecialFishHook fishHook = null;
	
	public
	EntityFishingPigZombie( World world )
	{
		super( world );
		getSpecialData( ).setCanBreatheInWater( true );
		getSpecialData( ).setIgnoreWaterPush( true );
	}
	
	@Override
	protected
	ResourceLocation getLootTable( ) { return LOOT_TABLE; }
	
	@Override
	protected
	void entityInit( )
	{
		super.entityInit( );
		getDataManager( ).register( IS_LINE_OUT, Boolean.FALSE );
	}
	
	/** Called to modify the mob's attributes based on the variant. */
	@Override
	protected
	void applyTypeAttributes( )
	{
		experienceValue += 2;
		
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 0.8 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		getSpecialData( ).rangedAttackSpread = 10.0F;
		getSpecialData( ).rangedAttackCooldown = 32;
		getSpecialData( ).rangedAttackMaxCooldown = 48;
		getSpecialData( ).rangedAttackMaxRange = 10.0F;
	}
	
	@Override
	protected
	void setEquipmentBasedOnDifficulty( DifficultyInstance difficulty )
	{
		super.setEquipmentBasedOnDifficulty( difficulty );
		
		setItemStackToSlot( EntityEquipmentSlot.MAINHAND, new ItemStack( Items.FISHING_ROD ) );
		if( getItemStackFromSlot( EntityEquipmentSlot.FEET ).isEmpty( ) ) {
			ItemStack booties = new ItemStack( Items.LEATHER_BOOTS );
			Items.LEATHER_BOOTS.setColor( booties, 0xffff00 );
			setItemStackToSlot( EntityEquipmentSlot.FEET, booties );
		}
		setCanPickUpLoot( false );
	}
	
	// IAngler implementations below this ================
	
	// Called every tick while this entity is alive.
	@Override
	public
	void onLivingUpdate( )
	{
		super.onLivingUpdate( );
		
		if( rodTime > 0 ) {
			rodTime--;
		}
		if( !world.isRemote && rodTime <= 0 && !isLineOut( ) ) {
			EntityLivingBase target = getAttackTarget( );
			if( target != null ) {
				SpecialMobData data = getSpecialData( );
				
				float distanceSq = (float) getDistanceSq( target );
				if( distanceSq > 16.0F && distanceSq < data.rangedAttackMaxRange * data.rangedAttackMaxRange && getEntitySenses( ).canSee( target ) ) {
					new EntitySpecialFishHook( world, this, target );
					world.spawnEntity( getFishHook( ) );
					playSound( SoundEvents.ENTITY_BOBBER_THROW, 0.5F, 0.4F / (rand.nextFloat( ) * 0.4F + 0.8F) );
					rodTime = rand.nextInt( Math.max( 1, data.rangedAttackMaxCooldown - data.rangedAttackCooldown ) ) + data.rangedAttackCooldown;
				}
			}
		}
	}
	
	/**
	 * Sets this angler's fish hook.
	 *
	 * @param hook The angler's new fish hook.
	 */
	@Override
	public
	void setFishHook( EntitySpecialFishHook hook )
	{
		fishHook = hook;
		getDataManager( ).set( IS_LINE_OUT, hook != null );
	}
	
	/**
	 * Gets this angler's fish hook.
	 *
	 * @return The angler's current fish hook, null if the angler does not have one out.
	 */
	@Override
	public
	EntitySpecialFishHook getFishHook( ) { return fishHook; }
	
	/**
	 * @return Whether the angler's line is out.
	 */
	@Override
	public
	boolean isLineOut( ) { return getDataManager( ).get( IS_LINE_OUT ); }
	
	@Nonnull
	@Override
	public
	ItemStack getItemStackFromSlot( EntityEquipmentSlot slot )
	{
		if( !Config.get( ).GENERAL.FANCY_FISHING_MOBS && EntityEquipmentSlot.MAINHAND.equals( slot ) ) {
			ItemStack held = super.getItemStackFromSlot( slot );
			if( world.isRemote && !held.isEmpty( ) && held.getItem( ) instanceof ItemFishingRod && isLineOut( ) ) {
				return new ItemStack( Items.STICK );
			}
			return held;
		}
		return super.getItemStackFromSlot( slot );
	}
}
