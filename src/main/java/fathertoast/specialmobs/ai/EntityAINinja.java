package fathertoast.specialmobs.ai;

import fathertoast.specialmobs.entity.*;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public
class EntityAINinja< T extends EntityLiving & INinja > extends EntityAIBase
{
	// The owner of this ai.
	private final T theEntity;
	
	public
	EntityAINinja( T entity )
	{
		theEntity = entity;
		setMutexBits( MobHelper.AI_BIT_MOVEMENT | MobHelper.AI_BIT_FACING | MobHelper.AI_BIT_SWIMMING );
	}
	
	// Returns whether the AI should begin execution.
	@Override
	public
	boolean shouldExecute( )
	{
		if( theEntity.getDisguiseBlock( ) == null || theEntity.getDisguiseBlock( ).getRenderType( ) != EnumBlockRenderType.MODEL )
			return false;
		
		int length = theEntity.world.playerEntities.size( );
		try {
			EntityPlayer player;
			float        dX, dZ;
			float        angleFromPlayer;
			for( int i = 0; i < length; i++ ) {
				player = theEntity.world.playerEntities.get( i );
				dX = (float) (theEntity.posX - player.posX);
				dZ = (float) (theEntity.posZ - player.posZ);
				angleFromPlayer = (float) Math.atan2( dX, -dZ ) * 180.0F / (float) Math.PI;
				if( Math.abs( angleFromPlayer - MathHelper.wrapDegrees( player.rotationYawHead ) ) > 90.0F ) {
					return true;
				}
			}
		}
		catch( Exception ex ) {
			// Do nothing
		}
		return false;
	}
	
	// Returns whether an in-progress EntityAIBase should continue executing
	@Override
	public
	boolean shouldContinueExecuting( ) { return shouldExecute( ); }
	
	// Determine if this AI task is interruptible by a higher priority task.
	@Override
	public
	boolean isInterruptible( ) { return false; }
	
	// Called once when the AI begins execution.
	@Override
	public
	void startExecuting( )
	{
		theEntity.getNavigator( ).clearPath( );
		theEntity.motionY = 0.0;
		theEntity.setInDisguise( true );
	}
	
	// Resets the task.
	@Override
	public
	void resetTask( )
	{
		theEntity.setInDisguise( false );
	}
	
	// Finds a nearby block for the entity to hide as and flags it to start hiding.
	public static
	< T extends EntityLiving & INinja >
	void startHiding( T entity )
	{
		// Options available regardless of position
		switch( entity.getRNG( ).nextInt( 100 ) ) {
			case 0:
				entity.setDisguiseBlock( Blocks.CHEST.correctFacing( entity.world, new BlockPos( entity ), Blocks.CHEST.getDefaultState( ) ) );
				return;
			case 1:
				entity.setDisguiseBlock( Blocks.LOG.getDefaultState( ) );
				return;
			case 2:
				entity.setDisguiseBlock( Blocks.SPONGE.getDefaultState( ) );
				return;
			case 3:
				entity.setDisguiseBlock( Blocks.DEADBUSH.getDefaultState( ) );
				return;
			case 4:
				entity.setDisguiseBlock( Blocks.LEAVES.getDefaultState( ) );
				return;
		}
		
		BlockPos    posUnderFeet   = new BlockPos( entity ).add( 0, -1, 0 );
		IBlockState blockUnderFeet = entity.world.getBlockState( posUnderFeet );
		if( !blockUnderFeet.getBlock( ).isAir( blockUnderFeet, entity.world, posUnderFeet ) ) {
			// Options available based on the block we are standing on
			
			if( blockUnderFeet.getBlock( ) == Blocks.STONE ) {
				// Cave theme
				switch( entity.getRNG( ).nextInt( 32 ) ) {
					case 0:
						entity.setDisguiseBlock( blockUnderFeet );
						return;
					case 1:
						entity.setDisguiseBlock( Blocks.GRAVEL.getDefaultState( ) );
						return;
					case 2:
						entity.setDisguiseBlock( Blocks.COAL_ORE.getDefaultState( ) );
						return;
					case 3:
						entity.setDisguiseBlock( Blocks.IRON_ORE.getDefaultState( ) );
						return;
					case 4:
						entity.setDisguiseBlock( Blocks.LAPIS_ORE.getDefaultState( ) );
						return;
					case 5:
						entity.setDisguiseBlock( Blocks.GOLD_ORE.getDefaultState( ) );
						return;
					case 6:
						entity.setDisguiseBlock( Blocks.REDSTONE_ORE.getDefaultState( ) );
						return;
					case 7:
						entity.setDisguiseBlock( Blocks.DIAMOND_ORE.getDefaultState( ) );
						return;
					case 8:
						entity.setDisguiseBlock( Blocks.EMERALD_ORE.getDefaultState( ) );
						return;
				}
			}
			else if( blockUnderFeet.getBlock( ) == Blocks.GRASS || blockUnderFeet.getBlock( ) == Blocks.DIRT ) {
				// Field theme
				switch( entity.getRNG( ).nextInt( 12 ) ) {
					case 0:
						entity.setDisguiseBlock( blockUnderFeet );
						return;
					case 1:
						entity.setDisguiseBlock( Blocks.LOG.getDefaultState( ) );
						return;
					case 2:
						entity.setDisguiseBlock( Blocks.PUMPKIN.getDefaultState( ).withProperty( BlockPumpkin.FACING, EnumFacing.Plane.HORIZONTAL.random( entity.getRNG( ) ) ) );
						return;
					case 3:
						entity.setDisguiseBlock( Blocks.MELON_BLOCK.getDefaultState( ) );
						return;
					case 4:
						entity.setDisguiseBlock( Blocks.TALLGRASS.getDefaultState( ).withProperty(
							BlockTallGrass.TYPE, entity.getRNG( ).nextInt( 3 ) != 0 ? BlockTallGrass.EnumType.GRASS : BlockTallGrass.EnumType.FERN
						) );
						return;
					case 5:
						entity.setDisguiseBlock( Blocks.LEAVES.getDefaultState( ) );
						return;
				}
			}
			else if( blockUnderFeet.getBlock( ) == Blocks.SAND || blockUnderFeet.getBlock( ) == Blocks.SANDSTONE ) {
				// Desert theme
				switch( entity.getRNG( ).nextInt( 6 ) ) {
					case 0:
						entity.setDisguiseBlock( blockUnderFeet );
						return;
					case 1:
						entity.setDisguiseBlock( Blocks.CACTUS.getDefaultState( ) );
						return;
					case 2:
						entity.setDisguiseBlock( Blocks.DEADBUSH.getDefaultState( ) );
						return;
				}
			}
			else if( blockUnderFeet.getBlock( ) == Blocks.NETHERRACK || blockUnderFeet.getBlock( ) == Blocks.NETHER_BRICK || blockUnderFeet.getBlock( ) == Blocks.SOUL_SAND ) {
				// Nether theme
				switch( entity.getRNG( ).nextInt( 14 ) ) {
					case 0:
						entity.setDisguiseBlock( blockUnderFeet );
						return;
					case 1:
						entity.setDisguiseBlock( Blocks.GRAVEL.getDefaultState( ) );
						return;
					case 2:
						entity.setDisguiseBlock( Blocks.SOUL_SAND.getDefaultState( ) );
						return;
					case 3:
						entity.setDisguiseBlock( Blocks.GLOWSTONE.getDefaultState( ) );
						return;
					case 4:
						entity.setDisguiseBlock( Blocks.QUARTZ_ORE.getDefaultState( ) );
						return;
					case 5:
						entity.setDisguiseBlock( Blocks.BROWN_MUSHROOM.getDefaultState( ) );
						return;
					case 6:
						entity.setDisguiseBlock( Blocks.RED_MUSHROOM.getDefaultState( ) );
						return;
				}
			}
		}
		
		// Pick a random nearby renderable block
		BlockPos    randPos;
		IBlockState randBlock;
		for( int i = 16; i-- > 0; ) {
			randPos = posUnderFeet.add(
				entity.getRNG( ).nextInt( 17 ) - 8,
				entity.getRNG( ).nextInt( 4 ) - 2,
				entity.getRNG( ).nextInt( 17 ) - 8
			);
			randBlock = entity.world.getBlockState( randPos );
			
			if( randBlock.getRenderType( ) == EnumBlockRenderType.MODEL ) {
				entity.setDisguiseBlock( randBlock );
				return;
			}
		}
		
		// Hide as a log if none of the other options are chosen
		entity.setDisguiseBlock( Blocks.LOG.getDefaultState( ) );
	}
}
