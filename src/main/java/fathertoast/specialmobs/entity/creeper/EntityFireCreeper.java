package fathertoast.specialmobs.entity.creeper;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityFireCreeper extends Entity_SpecialCreeper
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xe13916 );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FIRE;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "fire" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Fire charge", Items.FIRE_CHARGE );
		loot.addUncommonDrop( "uncommon", "Coal", Items.COAL );
	}
	
	public
	EntityFireCreeper( World world )
	{
		super( world );
		getSpecialData( ).setDamagedByWater( true );
		getSpecialData( ).setImmuneToFire( true );
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
	
	@Override
	protected
	void initTypeAI( )
	{
		setCanNotExplodeWhenWet( true );
	}
	
	// The explosion caused by this creeper.
	@Override
	public
	void explodeByType( boolean powered, boolean griefing )
	{
		float power = (float) explosionRadius * (powered ? 2.0F : 1.0F);
		world.newExplosion( this, posX, posY, posZ, power, true, griefing );
	}
}
