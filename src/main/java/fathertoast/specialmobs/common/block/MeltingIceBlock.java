package fathertoast.specialmobs.common.block;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.register.SMBlocks;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Random;

public class MeltingIceBlock extends IceBlock {
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Melting Ice",
                "", "", "", "", "", "" );//TODO
    }
    
    /** @return The state that should be placed. */
    public static BlockState getState( World world, BlockPos pos ) {
        final BlockState currentBlock = world.getBlockState( pos );
        return SMBlocks.MELTING_ICE.get().defaultBlockState().setValue( HAS_WATER,
                currentBlock.is( Blocks.FROSTED_ICE ) ||
                        currentBlock.getBlock() == Blocks.WATER && currentBlock.getValue( FlowingFluidBlock.LEVEL ) == 0 );
    }
    
    /** Call this after placing a melting ice block to trigger its melting logic. */
    public static void scheduleFirstTick( World world, BlockPos pos, Random random ) {
        world.getBlockTicks().scheduleTick( pos, SMBlocks.MELTING_ICE.get(), MathHelper.nextInt( random, 60, 120 ) );
    }
    
    /** Called after each melt logic tick to schedule the next tick. */
    private void scheduleTick( World world, BlockPos pos, Random random ) {
        final int darkness = 15 - getLight( world, pos );
        
        int solidNeighbors = 0;
        final BlockPos.Mutable neighborPos = new BlockPos.Mutable();
        for( Direction direction : Direction.Plane.HORIZONTAL ) {
            if( world.getBlockState( neighborPos.setWithOffset( pos, direction ) ).getMaterial().isSolid() ) {
                solidNeighbors++;
            }
        }
        
        // The 'neutral' state is 0 block light and 0 solid neighbors - this gives the same tick rate as frosted ice (1-2s)
        // Max delay is same as the default 'first tick' delay (3-6s)
        final int delay = 5 + darkness + 10 * solidNeighbors;
        world.getBlockTicks().scheduleTick( pos, this, MathHelper.nextInt( random, delay, delay << 1 ) );
    }
    
    /** @return The light level touching this block (0-15). We use this method because the block is solid. */
    private static int getLight( World world, BlockPos pos ) {
        int highestLight = 0;
        final BlockPos.Mutable neighborPos = new BlockPos.Mutable();
        for( Direction direction : Direction.values() ) {
            final int neighborLight = world.getBrightness( LightType.BLOCK, neighborPos.setWithOffset( pos, direction ) );
            if( neighborLight > 14 ) return 15;
            if( neighborLight > highestLight ) highestLight = neighborLight;
        }
        return highestLight;
    }
    
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final BooleanProperty HAS_WATER = BooleanProperty.create( "has_water" );
    
    public MeltingIceBlock() {
        super( AbstractBlock.Properties.of( Material.ICE ).sound( SoundType.GLASS ).noOcclusion()
                .randomTicks().friction( 0.98F ).strength( 0.5F ) );
        registerDefaultState( stateDefinition.any().setValue( AGE, 0 ).setValue( HAS_WATER, true ) );
    }
    
    @Override
    protected void createBlockStateDefinition( StateContainer.Builder<Block, BlockState> builder ) {
        builder.add( AGE ).add( HAS_WATER );
    }
    
    @SuppressWarnings( "deprecation" )
    @Override
    public ItemStack getCloneItemStack( IBlockReader world, BlockPos pos, BlockState state ) { return ItemStack.EMPTY; }
    
    @Override
    public void playerDestroy( World world, PlayerEntity player, BlockPos pos, BlockState state,
                               @Nullable TileEntity tileEntity, ItemStack tool ) {
        player.awardStat( Stats.BLOCK_MINED.get( this ) );
        player.causeFoodExhaustion( 0.005F );
        dropResources( state, world, pos, tileEntity, player, tool );
        
        if( EnchantmentHelper.getItemEnchantmentLevel( Enchantments.SILK_TOUCH, tool ) == 0 ) {
            melt( state, world, pos );
        }
    }
    
    @Override
    public void randomTick( BlockState state, ServerWorld world, BlockPos pos, Random random ) { tick( state, world, pos, random ); }
    
    @SuppressWarnings( "deprecation" )
    @Override
    public void tick( BlockState state, ServerWorld world, BlockPos pos, Random random ) {
        if( canMelt( world, pos ) ) {
            if( slightlyMelt( state, world, pos, random ) ) {
                final BlockPos.Mutable neighborPos = new BlockPos.Mutable();
                trySlightlyMelt( world, neighborPos.setWithOffset( pos, Direction.DOWN ), random );
                for( Direction direction : Direction.Plane.HORIZONTAL ) {
                    trySlightlyMelt( world, neighborPos.setWithOffset( pos, direction ), random );
                }
            }
        }
        else scheduleTick( world, pos, random );
    }
    
    /** @return True if the conditions allow melting. */
    private boolean canMelt( World world, BlockPos pos ) {
        // If a bright-ish (torch or higher) light source is nearby, always allow melting
        if( getLight( world, pos ) > 13 ) return true;
        
        // Otherwise, we want to prevent melting in two specific cases to get our desired melting pattern:
        //  * surrounded by other melting ice blocks horizontally (outside-in for ice floors/ceilings)
        //  * block below is non-air and block above is another melting ice block (top-down for ice walls)
        final BlockPos.Mutable neighborPos = new BlockPos.Mutable();
        for( Direction direction : Direction.Plane.HORIZONTAL ) {
            if( !world.getBlockState( neighborPos.setWithOffset( pos, direction ) ).is( this ) ) {
                //noinspection deprecation
                return !world.getBlockState( neighborPos.setWithOffset( pos, Direction.UP ) ).is( this ) ||
                        world.getBlockState( neighborPos.setWithOffset( pos, Direction.DOWN ) ).isAir();
            }
        }
        return false;
    }
    
    /** Attempts to slightly melt a target block. */
    private void trySlightlyMelt( World world, BlockPos pos, Random random ) {
        final BlockState state = world.getBlockState( pos );
        if( state.is( this ) && canMelt( world, pos ) ) slightlyMelt( state, world, pos, random );
    }
    
    /** @return Increments the block's melting and returns true if the block was completely melted. */
    private boolean slightlyMelt( BlockState state, World world, BlockPos pos, Random random ) {
        int age = state.getValue( AGE );
        if( age < 3 ) {
            world.setBlock( pos, state.setValue( AGE, age + 1 ), References.SetBlockFlags.UPDATE_CLIENT );
            scheduleTick( world, pos, random );
            return false;
        }
        else {
            melt( state, world, pos );
            return true;
        }
    }
    
    /** Melts the ice block. */
    @Override
    protected void melt( BlockState state, World world, BlockPos pos ) {
        if( world.dimensionType().ultraWarm() || !state.getValue( HAS_WATER ) ) {
            world.removeBlock( pos, false );
        }
        else {
            world.setBlockAndUpdate( pos, Blocks.WATER.defaultBlockState() );
            world.neighborChanged( pos, Blocks.WATER, pos );
        }
    }
}