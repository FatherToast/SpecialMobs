package fathertoast.specialmobs.entity.lavaslime;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import java.util.List;

public
class EntityStickyLavaSlime extends Entity_SpecialLavaSlime
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x9d733f );
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "sticky" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Slime balls", Items.SLIME_BALL );
	}
	
	private int grabTime;
	
	public
	EntityStickyLavaSlime( World world ) { super( world ); }
	
	@Override
	protected
	EntitySlime getSplitSlime( ) { return new EntityStickyLavaSlime( world ); }
	
	@Override
	public
	ResourceLocation[] getDefaultTextures( ) { return TEXTURES; }
	
	@Override
	protected
	ResourceLocation getLootTable( ) { return isSmallSlime( ) ? LOOT_TABLE : LootTableList.EMPTY; }
	
	/** Called to modify the mob's attributes based on the variant. */
	@Override
	protected
	void applyTypeAttributes( )
	{
		slimeExperienceValue += 2;
	}
	
	/** Called to modify the mob's attributes based on the variant. Health, damage, and speed must be modified here for slimes. */
	@Override
	protected
	void adjustTypeAttributesForSize( int size )
	{
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 4.0 * size );
	}
	
	// Called each tick while this entity is alive.
	@Override
	public
	void onLivingUpdate( )
	{
		super.onLivingUpdate( );
		
		grabTime--;
		List< Entity > riders = getPassengers( );
		if( grabTime <= 0 && !riders.isEmpty( ) ) {
			for( Entity rider : riders ) {
				if( rider instanceof EntityLivingBase ) {
					rider.attackEntityFrom( DamageSource.causeMobDamage( this ).setDamageBypassesArmor( ), 1.0F );
					grabTime = 20;
				}
			}
		}
	}
	
	// Overridden to modify attack effects.
	@Override
	protected
	void onTypeAttack( Entity target )
	{
		if( grabTime <= -20 && getPassengers( ).isEmpty( ) ) {
			if( target.startRiding( this, true ) ) {
				grabTime = 20;
			}
		}
	}
}
