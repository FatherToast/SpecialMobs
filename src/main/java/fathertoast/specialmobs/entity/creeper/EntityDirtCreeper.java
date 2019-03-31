package fathertoast.specialmobs.entity.creeper;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public
class EntityDirtCreeper extends Entity_SpecialCreeper
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x78553b );
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "dirt" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Dirt", Blocks.DIRT );
		loot.addSemicommonDrop( "semicommon", "Bread", Items.BREAD );
		loot.addRareDrop( "rare", "Root veggie", Items.CARROT, Items.POTATO );
	}
	
	public
	EntityDirtCreeper( World world )
	{
		super( world );
		getSpecialData( ).setImmuneToBurning( true );
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
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ARMOR, 6.0 );
	}
	
	// The explosion caused by this creeper.
	@Override
	public
	void explodeByType( boolean powered, boolean griefing )
	{
		float power = (float) explosionRadius * (powered ? 2.0F : 1.0F);
		world.createExplosion( this, posX, posY, posZ, power, false );
		if( !griefing ) {
			return;
		}
		
		IBlockState dirt   = Blocks.DIRT.getDefaultState( );
		int         radius = (int) Math.floor( power );
		BlockPos    center = new BlockPos( this );
		
		BlockPos pos;
		for( int y = -radius; y <= radius; y++ ) {
			for( int x = -radius; x <= radius; x++ ) {
				for( int z = -radius; z <= radius; z++ ) {
					if( x * x + y * y + z * z <= radius * radius ) {
						pos = center.add( x, y, z );
						if( dirt.getBlock( ).canPlaceBlockAt( world, pos ) ) {
							world.setBlockState( pos, dirt, 2 );
						}
					}
				}
			}
		}
	}
}
