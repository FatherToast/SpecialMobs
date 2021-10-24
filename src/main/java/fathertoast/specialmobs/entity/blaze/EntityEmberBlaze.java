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
class EntityEmberBlaze extends Entity_SpecialBlaze
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x000000 );
		return info;
	}
	
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "ember" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Coal", Items.COAL, 1 );
	}
	
	public
	EntityEmberBlaze( World world ) { super( world ); }
	
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
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ARMOR, 10.0 );
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 1.3 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		disableRangedAI( );
	}
	
	// Overridden to modify attack effects.
	@Override
	protected
	void onTypeAttack( Entity target )
	{
		target.attackEntityFrom(DamageSource.causeMobDamage(this).setDamageBypassesArmor(), 2.0F);
	}
}
