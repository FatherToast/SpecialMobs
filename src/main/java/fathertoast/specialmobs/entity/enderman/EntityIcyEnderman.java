package fathertoast.specialmobs.entity.enderman;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityIcyEnderman extends Entity_SpecialEnderman
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x72959c );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_ICE;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "icy" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "icy_eyes" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addClusterDrop( "common", "Snowballs", Items.SNOWBALL );
		loot.addUncommonDrop( "uncommon", "Ice block", Blocks.ICE );
	}
	
	public
	EntityIcyEnderman( World world )
	{
		super( world );
		getSpecialData( ).addPotionImmunity( MobEffects.SLOWNESS );
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
		experienceValue += 1;
	}
	
	// Overridden to modify attack effects.
	@Override
	public
	void onTypeAttack( Entity target )
	{
		if( target instanceof EntityLivingBase ) {
			int duration;
			switch( target.world.getDifficulty( ) ) {
				case PEACEFUL:
				case EASY:
					duration = 40;
					break;
				case NORMAL:
					duration = 60;
					break;
				default:
					duration = 80;
			}
			
			// -75% movespeed
			((EntityLivingBase) target).addPotionEffect( new PotionEffect( MobEffects.SLOWNESS, duration, 4 ) );
			// -30% attackspeed
			((EntityLivingBase) target).addPotionEffect( new PotionEffect( MobEffects.MINING_FATIGUE, duration, 2 ) );
		}
	}
}
