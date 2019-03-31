package fathertoast.specialmobs.entity.blaze;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityCinderBlaze extends Entity_SpecialBlaze
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
		loot.addCommonDrop( "common", "Blaze powder", Items.BLAZE_POWDER, 1 );
	}
	
	public
	EntityCinderBlaze( World world )
	{
		super( world );
		setSize( 0.5F, 0.9F );
		getSpecialData( ).setBaseScale( 0.5F );
	}
	
	@Override
	protected
	ResourceLocation getLootTable( ) { return LOOT_TABLE; }
	
	@Override
	protected
	void applyTypeAttributes( )
	{
		experienceValue = 1;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, -2.0 );
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 1.3 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		getSpecialData( ).rangedAttackDamage -= 1.0F;
		disableRangedAI( );
	}
	
	// Overridden to modify attack effects.
	@Override
	protected
	void onTypeAttack( Entity target )
	{
		target.setFire( 4 );
	}
}
