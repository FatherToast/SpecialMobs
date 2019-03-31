package fathertoast.specialmobs.entity.spider;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityGiantSpider extends Entity_SpecialSpider
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xa80e0e );
		return info;
	}
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addGuaranteedDrop( "base", "String", Items.STRING, 2 );
	}
	
	public
	EntityGiantSpider( World world )
	{
		super( world );
		setSize( 1.9F, 1.3F );
		getSpecialData( ).setBaseScale( 1.5F );
	}
	
	@Override
	protected
	ResourceLocation getLootTable( ) { return LOOT_TABLE; }
	
	/** Called to modify the mob's attributes based on the variant. */
	@Override
	protected
	void applyTypeAttributes( )
	{
		experienceValue += 1;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 16.0 );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 2.0 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		getSpecialData( ).rangedAttackDamage += 1.0F;
	}
}
