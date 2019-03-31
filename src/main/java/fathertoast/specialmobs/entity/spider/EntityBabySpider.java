package fathertoast.specialmobs.entity.spider;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityBabySpider extends Entity_SpecialSpider
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xffc0cb );
		info.weight = 0;
		return info;
	}
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		loot.addCommonDrop( "common", "String", Items.STRING, 1 );
	}
	
	public
	EntityBabySpider( World world )
	{
		super( world );
		setSize( 0.6F, 0.4F );
		getSpecialData( ).setBaseScale( 0.4F );
	}
	
	@Override
	protected
	ResourceLocation getLootTable( ) { return LOOT_TABLE; }
	
	/** Called to modify the mob's attributes based on the variant. */
	@Override
	protected
	void applyTypeAttributes( )
	{
		experienceValue = 1;
		
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MAX_HEALTH, 1.0 / 4.0 );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, -1.0 );
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 1.3 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		getSpecialData( ).rangedAttackDamage -= 1.0F;
		getSpecialData( ).rangedAttackMaxRange = 0.0F;
	}
}
