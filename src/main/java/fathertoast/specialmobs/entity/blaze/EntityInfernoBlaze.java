package fathertoast.specialmobs.entity.blaze;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityInfernoBlaze extends Entity_SpecialBlaze
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xf14f00 );
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "inferno" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Blaze powder", Items.BLAZE_POWDER );
		loot.addSemicommonDrop( "semicommon", "Fire charge", Items.FIRE_CHARGE );
	}
	
	public
	EntityInfernoBlaze( World world ) { super( world ); }
	
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
		experienceValue += 2;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 10.0 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		getSpecialData( ).rangedAttackSpread *= 3.0F;
		setRangedAI( 12, 2, 80, 100, 20.0F );
	}
	
	// Overridden to modify attack effects.
	@Override
	protected
	void onTypeAttack( Entity target )
	{
		if( target instanceof EntityLivingBase ) {
			((EntityLivingBase) target).setHealth( ((EntityLivingBase) target).getHealth( ) - 2.0F );
		}
	}
}
