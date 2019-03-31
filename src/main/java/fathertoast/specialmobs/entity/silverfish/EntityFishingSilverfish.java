package fathertoast.specialmobs.entity.silverfish;

import fathertoast.specialmobs.ai.*;
import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.entity.projectile.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityFishingSilverfish extends Entity_SpecialSilverfish implements IAngler
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x2d41f4 );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FISHING;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "fishing" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addPool(
			new LootPoolBuilder( "common" )
				.addEntry( new LootEntryItemBuilder( "Fish", Items.FISH ).setCount( 0, 2 ).addLootingBonus( 0, 1 ).smeltIfBurning( ).toLootEntry( ) )
				.toLootPool( )
		);
	}
	
	// Ticks until this angler can cast its lure again.
	private int rodTime = 0;
	
	// This angler's lure entity.
	private EntitySpecialFishHook fishHook = null;
	
	public
	EntityFishingSilverfish( World world )
	{
		super( world );
		setSize( 0.4F, 0.8F );
		getSpecialData( ).setBaseScale( 1.2F );
		
		getSpecialData( ).setCanBreatheInWater( true );
		getSpecialData( ).setIgnoreWaterPush( true );
	}
	
	@Override
	public
	ResourceLocation[] getDefaultTextures( ) { return TEXTURES; }
	
	@Override
	protected
	ResourceLocation getLootTable( ) { return LOOT_TABLE; }
	
	/** Called to modify the mob's attributes based on the variant. */
	@Override
	protected
	void applyTypeAttributes( )
	{
		experienceValue += 2;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 4.0 );
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 0.9 );
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
	boolean isLineOut( ) { return false; }
}
