package fathertoast.specialmobs.entity.creeper;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public
class EntityDarkCreeper extends Entity_SpecialCreeper
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xf9ff3a );
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "dark" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "dark_eyes" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addClusterDrop( "common", "Torches", Blocks.TORCH );
		
		loot.addRareDrop(
			"rare", "Night vision potion",
			PotionUtils.addPotionToItemStack( new ItemStack( Items.POTIONITEM ), PotionTypes.NIGHT_VISION )
		);
	}
	
	public
	EntityDarkCreeper( World world ) { super( world ); }
	
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
		float     power     = (float) explosionRadius / 2.0F * (powered ? 2.0F : 1.0F);
		Explosion explosion = new Explosion( world, this, posX, posY, posZ, power, false, griefing );
		if( net.minecraftforge.event.ForgeEventFactory.onExplosionStart( world, explosion ) )
			return;
		
		explosion.doExplosionA( );
		
		// Add unaffected light sources to the explosion's affected area
		final BlockPos center = new BlockPos( this );
		final int      radius = explosionRadius * 4 * (powered ? 2 : 1);
		BlockPos       pos;
		IBlockState    block;
		for( int y = -radius; y <= radius; y++ ) {
			for( int x = -radius; x <= radius; x++ ) {
				for( int z = -radius; z <= radius; z++ ) {
					if( x * x + y * y + z * z <= radius * radius ) {
						pos = center.add( x, y, z );
						block = world.getBlockState( pos );
						// Ignore the block if it is not a light or is already exploded
						if( block.getLightValue( world, pos ) > 1 && !explosion.getAffectedBlockPositions( ).contains( pos ) ) {
							float blockDamage = (float) radius * (0.7F + rand.nextFloat( ) * 0.6F);
							if( block.getMaterial( ) != Material.AIR ) {
								blockDamage -= (getExplosionResistance( explosion, world, pos, block ) + 0.3F) * 0.3F;
							}
							if( blockDamage > 0.0F && canExplosionDestroyBlock( explosion, world, pos, block, blockDamage ) ) {
								explosion.getAffectedBlockPositions( ).add( pos );
							}
						}
					}
				}
			}
		}
		
		explosion.doExplosionB( true );
		
		// Move the time forawrd to next night
		if( powered ) {
			long      time    = world.getWorldTime( );
			final int dayTime = (int) (time % 24000L);
			
			time -= dayTime;
			if( dayTime <= 13000 ) {
				time += 13000L;
			}
			else {
				time += 37000L;
			}
			world.setWorldTime( time );
		}
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
		super.potionCloudByType( potionCloud, powered );
		
		potionCloud.addEffect( new PotionEffect( MobEffects.BLINDNESS, 100 ) );
	}
}
