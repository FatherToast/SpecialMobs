package fathertoast.specialmobs.entity.spider;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.config.*;
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
class EntityDesertSpider extends Entity_SpecialSpider
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xe6ddac );
		info.weightExceptions = new EnvironmentListConfig(
			new TargetEnvironment.TargetBiomeGroup( "desert", BestiaryInfo.BASE_WEIGHT_COMMON ),
			new TargetEnvironment.TargetBiomeGroup( "savanna", BestiaryInfo.BASE_WEIGHT_COMMON ),
			new TargetEnvironment.TargetBiomeGroup( "mesa", BestiaryInfo.BASE_WEIGHT_COMMON )
		);
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "desert" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "desert_eyes" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Leather", Items.LEATHER );
	}
	
	public
	EntityDesertSpider( World world )
	{
		super( world );
		setSize( 1.0F, 0.8F );
		getSpecialData( ).setBaseScale( 0.8F );
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
		experienceValue += 2;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 4.0 );
	}
	
	// Overridden to modify attack effects.
	@Override
	public
	void onTypeAttack( Entity target )
	{
		if( target instanceof EntityLivingBase ) {
			int duration = MobHelper.getDebuffDuration( world.getDifficulty( ) );
			
			if( !Config.get( ).GENERAL.DISABLE_NAUSEA ) {
				((EntityLivingBase) target).addPotionEffect( new PotionEffect( MobEffects.NAUSEA, duration, 0 ) );
			}
			((EntityLivingBase) target).addPotionEffect( new PotionEffect( MobEffects.BLINDNESS, duration, 0 ) );
			
			((EntityLivingBase) target).addPotionEffect( new PotionEffect( MobEffects.SLOWNESS, duration, 2 ) );
			((EntityLivingBase) target).addPotionEffect( new PotionEffect( MobEffects.RESISTANCE, duration, -2 ) );
		}
	}
}
