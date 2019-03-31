package fathertoast.specialmobs.entity.spider;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityPoisonSpider extends Entity_SpecialSpider
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x0c424e );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FOREST;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( "textures/entity/spider/cave_spider.png" ),
		new ResourceLocation( "textures/entity/spider_eyes.png" )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addUncommonDrop( "uncommon", "Spider eye", Items.SPIDER_EYE );
	}
	
	public
	EntityPoisonSpider( World world ) { super( world ); }
	
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
			int duration = MobHelper.getDebuffDuration( world.getDifficulty( ) );
			
			((EntityLivingBase) target).addPotionEffect( new PotionEffect( MobEffects.POISON, duration ) );
		}
	}
}
