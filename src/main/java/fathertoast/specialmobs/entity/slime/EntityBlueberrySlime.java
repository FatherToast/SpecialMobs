package fathertoast.specialmobs.entity.slime;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public
class EntityBlueberrySlime extends Entity_SpecialSlime
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x766bbc );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_WATER;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "blueberry" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addRareDrop( "rare", "Undersea loot", Items.GOLD_NUGGET, Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS );
		
		loot.addUncommonDrop( "uncommon", "Slime color", new ItemStack( Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage( ) ) );
	}
	
	public
	EntityBlueberrySlime( World world )
	{
		super( world );
		getSpecialData( ).setImmuneToBurning( true );
		getSpecialData( ).setCanBreatheInWater( true );
	}
	
	@Override
	protected
	EntitySlime getSplitSlime( ) { return new EntityBlueberrySlime( world ); }
	
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
	
	/** Called to modify the mob's attributes based on the variant. Health, damage, and speed must be modified here for slimes. */
	@Override
	protected
	void adjustTypeAttributesForSize( int size )
	{
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 1.0 * size );
	}
	
	@Override
	public
	boolean handleWaterMovement( )
	{
		if( world.isMaterialInBB( getEntityBoundingBox( ).grow( 0.0, -0.4, 0.0 ).shrink( 0.001 ), Material.WATER ) ) {
			fallDistance = 0.0F;
			extinguish( );
		}
		return inWater = false;
	}
}
