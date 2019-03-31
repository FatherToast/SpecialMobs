package fathertoast.specialmobs.entity.ghast;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public
class EntityBabyGhast extends Entity_SpecialGhast
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
		loot.addCommonDrop( "common", "Gunpowder", Items.GUNPOWDER, 1 );
	}
	
	public
	EntityBabyGhast( World world )
	{
		super( world );
		setSize( 1.0F, 1.0F );
		getSpecialData( ).setBaseScale( 0.25F );
	}
	
	@Override
	protected
	ResourceLocation getLootTable( ) { return LOOT_TABLE; }
	
	@Override
	protected
	SoundEvent getAmbientSound( ) { return null; }
	
	/** Called to modify the mob's attributes based on the variant. */
	@Override
	protected
	void applyTypeAttributes( )
	{
		experienceValue = 1;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, -1.0 );
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 1.3 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		disableRangedAI( );
	}
}
