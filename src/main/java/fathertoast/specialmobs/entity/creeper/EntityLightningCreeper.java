package fathertoast.specialmobs.entity.creeper;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityLightningCreeper extends Entity_SpecialCreeper
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x499cae );
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "lightning" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Redstone dust", Items.REDSTONE );
	}
	
	public
	EntityLightningCreeper( World world )
	{
		super( world );
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
	
	// The explosion caused by this creeper.
	@Override
	public
	void explodeByType( boolean powered, boolean griefing )
	{
		float power = (float) explosionRadius * (powered ? 2.0F : 1.0F / 3.0F);
		world.createExplosion( this, posX, posY, posZ, power, griefing );
		
		// Spawn lightning
		world.addWeatherEffect( new EntityLightningBolt( world, posX, posY, posZ, false ) );
		if( power >= 2.0F ) {
			int radius = (int) Math.floor( power );
			for( int x = -radius; x <= radius; x++ ) {
				for( int z = -radius; z <= radius; z++ ) {
					if( (x != 0 || z != 0) && x * x + z * z <= radius * radius && rand.nextFloat( ) < 0.3F ) {
						world.addWeatherEffect( new EntityLightningBolt( world, posX + x, posY, posZ + z, false ) );
					}
				}
			}
		}
		
		// Start a thunderstorm
		if( powered ) {
			int duration = this.rand.nextInt( 12000 ) + 3600;
			if( !world.getWorldInfo( ).isThundering( ) || world.getWorldInfo( ).getThunderTime( ) < duration ) {
				world.getWorldInfo( ).setThunderTime( duration );
				world.getWorldInfo( ).setThundering( true );
			}
			duration += 1200;
			if( !world.getWorldInfo( ).isRaining( ) || world.getWorldInfo( ).getRainTime( ) < duration ) {
				world.getWorldInfo( ).setRainTime( duration );
				world.getWorldInfo( ).setRaining( true );
			}
		}
	}
}
