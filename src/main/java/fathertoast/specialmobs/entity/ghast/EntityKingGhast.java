package fathertoast.specialmobs.entity.ghast;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityKingGhast extends Entity_SpecialGhast
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xe8c51a );
		info.weight = BestiaryInfo.BASE_WEIGHT_UNCOMMON;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "king" ) ),
		null,
		new ResourceLocation( GET_TEXTURE_PATH( "king_shooting" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addSemicommonDrop( "semicommon", "Gold ingot", Items.GOLD_INGOT );
		loot.addUncommonDrop( "uncommon", "Emerald", Items.EMERALD );
	}
	
	public
	EntityKingGhast( World world )
	{
		super( world );
		setSize( 6.0F, 6.0F );
		getSpecialData( ).setBaseScale( 1.5F );
		
		getSpecialData( ).setRegenerationTime( 30 );
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
		experienceValue += 4;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 20.0 );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ARMOR, 10.0 );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 4.0 );
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 0.6 );
	}
	
	@Override
	public
	int getFireballStrength( )
	{
		return Math.round( super.getFireballStrength( ) * 2.5F );
	}
}
