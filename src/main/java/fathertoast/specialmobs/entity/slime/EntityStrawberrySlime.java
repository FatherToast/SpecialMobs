package fathertoast.specialmobs.entity.slime;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public
class EntityStrawberrySlime extends Entity_SpecialSlime
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xbe696b );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FIRE;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "strawberry" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Fire charges", Items.FIRE_CHARGE, 1 );
		
		loot.addUncommonDrop( "uncommon", "Slime color", new ItemStack( Items.DYE, 1, EnumDyeColor.RED.getDyeDamage( ) ) );
	}
	
	public
	EntityStrawberrySlime( World world )
	{
		super( world );
		getSpecialData( ).setImmuneToFire( true );
		getSpecialData( ).setDamagedByWater( true );
	}
	
	@Override
	protected
	EntitySlime getSplitSlime( ) { return new EntityStrawberrySlime( world ); }
	
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
		slimeExperienceValue += 1;
	}
	
	// Overridden to modify attack effects.
	@Override
	protected
	void onTypeAttack( Entity target )
	{
		target.setFire( getSlimeSize( ) * 3 );
	}
}
