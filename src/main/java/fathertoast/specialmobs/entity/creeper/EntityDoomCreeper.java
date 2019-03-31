package fathertoast.specialmobs.entity.creeper;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public
class EntityDoomCreeper extends Entity_SpecialCreeper
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x494949 );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FOREST;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "doom" ) ),
		null,
		new ResourceLocation( GET_TEXTURE_PATH( "doom_overlay" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addLootTable( "common", "Witch loot", LootTableList.ENTITIES_WITCH );
	}
	
	public
	EntityDoomCreeper( World world )
	{
		super( world );
		getSpecialData( ).addPotionImmunity( MobEffects.INSTANT_DAMAGE, MobEffects.WITHER );
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
	
	// The explosion caused by this creeper.
	@Override
	public
	void explodeByType( boolean powered, boolean griefing )
	{
		float power = (float) explosionRadius / 2.0F * (powered ? 2.0F : 1.0F);
		world.createExplosion( this, posX, posY, posZ, power, false );
	}
	
	// Return true here only if you are adding extra potion effects to potionCloudByType().
	@Override
	public
	boolean alwaysMakePotionCloud( ) { return true; }
	
	// The explosion caused by this creeper.
	@Override
	public
	void potionCloudByType( EntityAreaEffectCloud potionCloud, boolean powered )
	{
		potionCloud.setRadius( (float) explosionRadius * (powered ? 2.0F : 1.0F) );
		
		potionCloud.addEffect( new PotionEffect( MobEffects.INSTANT_DAMAGE, 100, 1 ) );
		potionCloud.addEffect( new PotionEffect( MobEffects.WITHER, 200 ) );
	}
}
