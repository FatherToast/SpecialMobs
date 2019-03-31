package fathertoast.specialmobs.entity.pigzombie;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityGiantPigZombie extends Entity_SpecialPigZombie
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x4c7129 );
		return info;
	}
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addGuaranteedDrop( "base", "Rotten flesh", Items.ROTTEN_FLESH, 2 );
	}
	
	public
	EntityGiantPigZombie( World world )
	{
		super( world );
		stepHeight = 1.0F;
		setSize( 0.9F, 2.7F );
		multiplySize( 1.0F ); // Zombie thing to actually update the size
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
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 20.0 );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 2.0 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		getSpecialData( ).rangedAttackDamage += 2.0F;
	}
	
	@Override
	public
	boolean isChild( ) { return false; }
	
	@Override
	public
	void setChild( boolean child ) { }
}
