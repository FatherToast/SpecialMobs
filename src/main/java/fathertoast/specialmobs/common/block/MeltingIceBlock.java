package fathertoast.specialmobs.common.block;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.register.SMBlocks;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

public class MeltingIceBlock extends IceBlock {
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Melting Ice",
                "", "", "", "", "", "" );//TODO
    }
    
    /** @return The state that should be placed. */
    public static BlockState getState(Level level, BlockPos pos ) {
        final BlockState currentBlock = level.getBlockState( pos );
        return SMBlocks.MELTING_ICE.get().defaultBlockState().setValue( HAS_WATER,
                currentBlock.is( Blocks.FROSTED_ICE ) ||
                        currentBlock.getBlock() == Blocks.WATER && currentBlock.getValue( LiquidBlock.LEVEL ) == 0 );
    }
    
    /** Call this after placing a melting ice block to trigger its melting logic. */
    public static void scheduleFirstTick( Level level, BlockPos pos, RandomSource random ) {
        level.scheduleTick( pos, SMBlocks.MELTING_ICE.get(), Mth.nextInt( random, 60, 120 ) );
    }
    
    /** Called after each melt logic tick to schedule the next tick. */
    private void scheduleTick( Level level, BlockPos pos, RandomSource random ) {
        final int darkness = 15 - getLight( level, pos );
        
        int solidNeighbors = 0;
        final BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        for( Direction direction : Direction.Plane.HORIZONTAL ) {
            if( level.getBlockState( neighborPos.setWithOffset( pos, direction ) ).isSolid() ) { //TODO: Check if this is correct
                solidNeighbors++;
            }
        }
        
        // The 'neutral' state is 0 block light and 0 solid neighbors - this gives the same tick rate as frosted ice (1-2s)
        // Max delay is same as the default 'first tick' delay (3-6s)
        final int delay = 5 + darkness + 10 * solidNeighbors;
        level.scheduleTick( pos, this, Mth.nextInt( random, delay, delay << 1 ) );
    }
    
    /** @return The light level touching this block (0-15). We use this method because the block is solid. */
    private static int getLight( Level level, BlockPos pos ) {
        int highestLight = 0;
        final BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        for( Direction direction : Direction.values() ) {
            final int neighborLight = level.getBrightness( LightLayer.BLOCK, neighborPos.setWithOffset( pos, direction ) );
            if( neighborLight > 14 ) return 15;
            if( neighborLight > highestLight ) highestLight = neighborLight;
        }
        return highestLight;
    }
    
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final BooleanProperty HAS_WATER = BooleanProperty.create( "has_water" );
    
    public MeltingIceBlock() {
        super( BlockBehaviour.Properties.copy( Blocks.ICE ).sound( SoundType.GLASS ).noOcclusion()
                .randomTicks().friction( 0.98F ).strength( 0.5F ) );
        registerDefaultState( stateDefinition.any().setValue( AGE, 0 ).setValue( HAS_WATER, true ) );
    }
    
    @Override
    protected void createBlockStateDefinition( StateDefinition.Builder<Block, BlockState> builder ) {
        builder.add( AGE ).add( HAS_WATER );
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return ItemStack.EMPTY;
    }

    @Override
    public void playerDestroy( Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool ) {
        player.awardStat( Stats.BLOCK_MINED.get( this ) );
        player.causeFoodExhaustion( 0.005F );
        dropResources( state, level, pos, blockEntity, player, tool );
        
        if( tool.getEnchantmentLevel( Enchantments.SILK_TOUCH ) == 0 ) {
            melt( state, level, pos );
        }
    }
    
    @Override
    public void randomTick( BlockState state, ServerLevel world, BlockPos pos, RandomSource random ) { tick( state, world, pos, random ); }
    
    @SuppressWarnings( "deprecation" )
    @Override
    public void tick( BlockState state, ServerLevel level, BlockPos pos, RandomSource random ) {
        if( canMelt( level, pos ) ) {
            if( slightlyMelt( state, level, pos, random ) ) {
                final BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
                trySlightlyMelt( level, neighborPos.setWithOffset( pos, Direction.DOWN ), random );
                for( Direction direction : Direction.Plane.HORIZONTAL ) {
                    trySlightlyMelt( level, neighborPos.setWithOffset( pos, direction ), random );
                }
            }
        }
        else scheduleTick( level, pos, random );
    }
    
    /** @return True if the conditions allow melting. */
    private boolean canMelt( Level level, BlockPos pos ) {
        // If a bright-ish (torch or higher) light source is nearby, always allow melting
        if( getLight( level, pos ) > 13 ) return true;
        
        // Otherwise, we want to prevent melting in two specific cases to get our desired melting pattern:
        //  * surrounded by other melting ice blocks horizontally (outside-in for ice floors/ceilings)
        //  * block below is non-air and block above is another melting ice block (top-down for ice walls)
        final BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        for( Direction direction : Direction.Plane.HORIZONTAL ) {
            if( !level.getBlockState( neighborPos.setWithOffset( pos, direction ) ).is( this ) ) {
                return !level.getBlockState( neighborPos.setWithOffset( pos, Direction.UP ) ).is( this ) ||
                        level.getBlockState( neighborPos.setWithOffset( pos, Direction.DOWN ) ).isAir();
            }
        }
        return false;
    }
    
    /** Attempts to slightly melt a target block. */
    private void trySlightlyMelt( Level level, BlockPos pos, RandomSource random ) {
        final BlockState state = level.getBlockState( pos );
        if( state.is( this ) && canMelt( level, pos ) ) slightlyMelt( state, level, pos, random );
    }
    
    /** @return Increments the block's melting and returns true if the block was completely melted. */
    private boolean slightlyMelt( BlockState state, Level level, BlockPos pos, RandomSource random ) {
        int age = state.getValue( AGE );
        if( age < 3 ) {
            level.setBlock( pos, state.setValue( AGE, age + 1 ), References.SetBlockFlags.UPDATE_CLIENT );
            scheduleTick( level, pos, random );
            return false;
        }
        else {
            melt( state, level, pos );
            return true;
        }
    }
    
    /** Melts the ice block. */
    @Override
    protected void melt( BlockState state, Level level, BlockPos pos ) {
        if( level.dimensionType().ultraWarm() || !state.getValue( HAS_WATER ) ) {
            level.removeBlock( pos, false );
        }
        else {
            level.setBlockAndUpdate( pos, Blocks.WATER.defaultBlockState() );
            level.neighborChanged( pos, Blocks.WATER, pos );
        }
    }
}