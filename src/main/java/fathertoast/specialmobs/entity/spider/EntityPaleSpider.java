package fathertoast.specialmobs.entity.spider;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityPaleSpider extends Entity_SpecialSpider
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xded4c6 );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_ICE;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "pale" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "pale_eyes" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addSemicommonDrop( "semicommon", "Fermented eye", Items.FERMENTED_SPIDER_EYE );
	}
	
	public
	EntityPaleSpider( World world ) { super( world ); }
	
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
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ARMOR, 15.0 );
	}
	
	// Overridden to modify attack effects.
	@Override
	public
	void onTypeAttack( Entity target )
	{
		if( target instanceof EntityLivingBase ) {
			int duration = MobHelper.getDebuffDuration( world.getDifficulty( ) );
			
			((EntityLivingBase) target).addPotionEffect( new PotionEffect( MobEffects.WEAKNESS, duration, 0 ) );
			((EntityLivingBase) target).addPotionEffect( new PotionEffect( MobEffects.MINING_FATIGUE, duration, 1 ) );
		}
	}
}
