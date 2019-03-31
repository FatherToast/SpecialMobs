package fathertoast.specialmobs.entity.creeper;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public
class EntityDrowningCreeper extends Entity_SpecialCreeper
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x2d41f4 );
		info.weight = BestiaryInfo.BASE_WEIGHT_UNCOMMON;
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_WATER;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "drowning" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "drowning_eyes" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addPool(
			new LootPoolBuilder( "common" )
				.addEntry( new LootEntryItemBuilder( "Fish", Items.FISH ).setCount( 0, 2 ).addLootingBonus( 0, 1 ).smeltIfBurning( ).toLootEntry( ) )
				.toLootPool( )
		);
		loot.addUncommonDrop( "uncommon", "Undersea loot", Items.GOLD_NUGGET, Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS );
	}
	
	public
	EntityDrowningCreeper( World world )
	{
		super( world );
		getSpecialData( ).setImmuneToBurning( true );
		getSpecialData( ).setCanBreatheInWater( true );
		getSpecialData( ).setIgnoreWaterPush( true );
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
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 10.0 );
	}
	
	// The explosion caused by this creeper.
	@Override
	public
	void explodeByType( boolean powered, boolean griefing )
	{
		float power = (float) explosionRadius * (powered ? 2.0F : 1.0F) + 3.0F;
		world.createExplosion( this, posX, posY, posZ, griefing ? 1.0F : power, false );
		if( !griefing ) {
			return;
		}
		
		IBlockState cobblestone = Blocks.COBBLESTONE.getDefaultState( );
		IBlockState silverfish  = Blocks.MONSTER_EGG.getDefaultState( ).withProperty( BlockSilverfish.VARIANT, BlockSilverfish.EnumType.COBBLESTONE );
		IBlockState water       = Blocks.WATER.getDefaultState( );
		int         radius      = (int) Math.floor( power );
		int         rMinusOneSq = (radius - 1) * (radius - 1);
		BlockPos    center      = new BlockPos( this );
		
		BlockPos pos;
		int      distSq;
		for( int y = -radius; y <= radius; y++ ) {
			for( int x = -radius; x <= radius; x++ ) {
				for( int z = -radius; z <= radius; z++ ) {
					distSq = x * x + y * y + z * z;
					if( distSq <= radius * radius ) {
						pos = center.add( x, y, z );
						// Cobblestone casing
						if( distSq > rMinusOneSq ) {
							if( cobblestone.getBlock( ).canPlaceBlockAt( world, pos ) ) {
								if( rand.nextFloat( ) < 0.25F ) {
									world.setBlockState( pos, silverfish, 2 );
								}
								else {
									world.setBlockState( pos, cobblestone, 2 );
								}
							}
						}
						// Water fill
						else if( water.getBlock( ).canPlaceBlockAt( world, pos ) ) {
							world.setBlockState( pos, water, 2 );
						}
					}
				}
			}
		}
	}
}
