package fathertoast.specialmobs.entity.slime;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public
class EntityLemonSlime extends Entity_SpecialSlime
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xe6e861 );
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "lemon" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Redstone dust", Items.REDSTONE, 1 );
		
		loot.addUncommonDrop( "uncommon", "Slime color", new ItemStack( Items.DYE, 1, EnumDyeColor.YELLOW.getDyeDamage( ) ) );
	}
	
	public
	EntityLemonSlime( World world )
	{
		super( world );
		getSpecialData( ).setImmuneToFire( true );
	}
	
	@Override
	protected
	EntitySlime getSplitSlime( ) { return new EntityLemonSlime( world ); }
	
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
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 2.0 * size );
	}
	
	// Overridden to modify attack effects.
	@Override
	protected
	void onTypeAttack( Entity target )
	{
		world.addWeatherEffect( new EntityLightningBolt( world, target.posX, target.posY, target.posZ, false ) );
		
		// Knock self back
		double power = 1.0;
		double vX    = posX - target.posX;
		double vZ    = posZ - target.posZ;
		double vH    = Math.sqrt( vX * vX + vZ * vZ );
		motionX = vX / vH * power + motionX * 0.2;
		motionY = 0.42 * power;
		motionZ = vZ / vH * power + motionZ * 0.2;
		isAirBorne = true;
	}
	
	@Override
	public
	void onStruckByLightning( EntityLightningBolt lightningBolt ) { }
}
