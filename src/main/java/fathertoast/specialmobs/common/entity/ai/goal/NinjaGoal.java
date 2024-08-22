package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.ai.INinja;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * This AI goal tells the ninja when players are looking in its direction so it can 'freeze' in place and act like a block.
 * <p>
 * It also contains static methods to help ninjas choose a block to disguise as.
 */
public class NinjaGoal<T extends Mob & INinja> extends Goal {
    
    private final T ninja;
    
    public NinjaGoal( T entity ) {
        ninja = entity;
        setFlags( EnumSet.of( Goal.Flag.MOVE, Flag.LOOK, Goal.Flag.JUMP ) );
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() {
        if( ninja.getHiddenDragon() == null || ninja.getHiddenDragon().getRenderShape() == RenderShape.INVISIBLE )
            return false;
        
        final List<Player> players = new ArrayList<>( ninja.level().players() );
        for( Player player : players ) {
            final float dX = (float) (ninja.getX() - player.getX());
            final float dZ = (float) (ninja.getZ() - player.getZ());
            final float angleFromPlayer = (float) Math.atan2( dX, -dZ ) * 180.0F / (float) Math.PI;
            
            if( Math.abs( angleFromPlayer - Mth.wrapDegrees( player.yHeadRot ) ) > 90.0F ) {
                return true; // A player is looking in the ninja's general direction!
            }
        }
        return false;
    }
    
    /** Called when this AI is activated. */
    @Override
    public void start() {
        ninja.getNavigation().stop();
        ninja.clearFire();
        ninja.moveTo( Math.floor( ninja.getX() ) + 0.5, Math.floor( ninja.getY() ), Math.floor( ninja.getZ() ) + 0.5 );
        ninja.setDeltaMovement( 0.0, 0.0, 0.0 );
        
        ninja.setCrouchingTiger( true );
    }
    
    /** @return Called each update while active and returns true if this AI can remain active. */
    @Override
    public boolean canContinueToUse() { return canUse(); }
    
    /** @return Whether this AI can be interrupted by a lower priority AI while running. */
    @Override
    public boolean isInterruptable() { return false; }
    
    /** Called when this AI is deactivated. */
    @Override
    public void stop() { ninja.setCrouchingTiger( false ); }
    
    
    //--------------- Static Disguise Helper ----------------
    
    /** Finds a nearby block for the entity to hide as and flags it to start hiding. */
    public static <T extends Mob & INinja> BlockState pickDisguise(T entity ) {
        final RandomSource random = entity.getRandom();
        
        // Random blocks to be picked regardless of position
        switch( random.nextInt( 200 ) ) {
            case 0: return Blocks.TNT.defaultBlockState();
            case 1: return Blocks.OAK_LOG.defaultBlockState();
            case 2: return Blocks.SPONGE.defaultBlockState();
            case 3: return Blocks.DEAD_BUSH.defaultBlockState();
            case 4: return Blocks.OAK_LEAVES.defaultBlockState();
            case 5: return randomRotation( Blocks.BEE_NEST.defaultBlockState(), random );
            case 6: return Blocks.CAKE.defaultBlockState();
            case 7: return Blocks.CRAFTING_TABLE.defaultBlockState();
            case 8: return randomRotation( Blocks.FURNACE.defaultBlockState(), random );
            case 9: return randomRotation( Blocks.ANVIL.defaultBlockState(), random );
            case 10: return Blocks.BREWING_STAND.defaultBlockState();
            case 11: return randomRotation( Blocks.LEVER.defaultBlockState()
                    .setValue( BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR ), random );
            case 12: return randomPottedFlower( random );
            case 13: return Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3);
            case 14: return Blocks.BONE_BLOCK.defaultBlockState();
            case 15: return Blocks.BOOKSHELF.defaultBlockState();
            case 16: return Blocks.DETECTOR_RAIL.defaultBlockState();
        }
        
        final BlockPos posUnderFeet = entity.blockPosition().below();
        final BlockState blockUnderFeet = entity.level().getBlockState( posUnderFeet );
        if( !blockUnderFeet.isAir() ) {
            // Options available based on the block we are standing on

            if( blockUnderFeet.is( Blocks.STONE ) || blockUnderFeet.is( Blocks.STONE_BRICKS ) ) {
                // Cave theme
                switch( random.nextInt( 30 ) ) {
                    case 0: return Blocks.BROWN_MUSHROOM.defaultBlockState();
                    case 1: return Blocks.RED_MUSHROOM.defaultBlockState();
                    case 2: return Blocks.CLAY.defaultBlockState();
                    case 3: return Blocks.GRAVEL.defaultBlockState();
                    case 4: return Blocks.COAL_ORE.defaultBlockState();
                    case 5: return Blocks.IRON_ORE.defaultBlockState();
                    case 6: return Blocks.LAPIS_ORE.defaultBlockState();
                    case 7: return Blocks.GOLD_ORE.defaultBlockState();
                    case 8: return Blocks.REDSTONE_ORE.defaultBlockState();
                    case 9: return Blocks.DIAMOND_ORE.defaultBlockState();
                    case 10: return Blocks.EMERALD_ORE.defaultBlockState();
                    case 11: return Blocks.COPPER_ORE.defaultBlockState();
                    case 12: return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                }
            }
            else if ( blockUnderFeet.is( Blocks.DEEPSLATE )) {
                // Deep cave theme
                switch ( random.nextInt(30) ) {
                    case 0: return Blocks.BROWN_MUSHROOM.defaultBlockState();
                    case 1: return Blocks.RED_MUSHROOM.defaultBlockState();
                    case 2: return Blocks.TUFF.defaultBlockState();
                    case 3: return Blocks.GRAVEL.defaultBlockState();
                    case 4: return Blocks.DEEPSLATE_COAL_ORE.defaultBlockState();
                    case 5: return Blocks.DEEPSLATE_IRON_ORE.defaultBlockState();
                    case 6: return Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState();
                    case 7: return Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState();
                    case 8: return Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState();
                    case 9: return Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState();
                    case 10: return Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState();
                    case 11: return Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState();
                    case 12: return Blocks.DEEPSLATE.defaultBlockState();
                    case 13: return Blocks.DEEPSLATE_BRICK_SLAB.defaultBlockState();
                    case 14: return Blocks.SCULK.defaultBlockState();
                    case 15: return Blocks.SCULK_CATALYST.defaultBlockState();
                }
            }
            else if( blockUnderFeet.is( Blocks.GRASS ) || blockUnderFeet.is( Blocks.DIRT ) || blockUnderFeet.is( Blocks.PODZOL ) ) {
                // Field theme
                switch( random.nextInt( 20 ) ) {
                    case 0: return blockUnderFeet;
                    case 1: return Blocks.OAK_LOG.defaultBlockState();
                    case 2: return Blocks.OAK_LEAVES.defaultBlockState();
                    case 3: return Blocks.PUMPKIN.defaultBlockState();
                    case 4: return Blocks.MELON.defaultBlockState();
                    case 5: return Blocks.TALL_GRASS.defaultBlockState();
                    case 6: return Blocks.FERN.defaultBlockState();
                    case 7: return randomRotation( Blocks.BEE_NEST.defaultBlockState(), random );
                    case 8: return randomFlower( random );
                    case 9: return randomPottedFlower( random );
                    case 10: return randomPottedPlant( random );
                }
            }
            else if( blockUnderFeet.is( Blocks.SAND ) || blockUnderFeet.is( Blocks.RED_SAND ) ||
                    blockUnderFeet.is( Blocks.SANDSTONE ) || blockUnderFeet.is( Blocks.RED_SANDSTONE ) ) {
                // Desert theme
                switch( random.nextInt( 5 ) ) {
                    case 0: return Blocks.CACTUS.defaultBlockState();
                    case 1: return Blocks.DEAD_BUSH.defaultBlockState();
                    case 2: return randomPottedDesertPlant( random );
                }
            }
            else if( blockUnderFeet.is( Blocks.NETHERRACK ) || blockUnderFeet.is( Blocks.NETHER_BRICKS ) ||
                    blockUnderFeet.is( Blocks.SOUL_SAND ) || blockUnderFeet.is( Blocks.SOUL_SOIL ) ||
                    blockUnderFeet.is( Blocks.CRIMSON_NYLIUM ) || blockUnderFeet.is( Blocks.WARPED_NYLIUM ) ) {
                // Nether theme
                switch( random.nextInt( 25 ) ) {
                    case 0: return blockUnderFeet;
                    case 1: return Blocks.GRAVEL.defaultBlockState();
                    case 2: return Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
                    case 3: return Blocks.NETHER_GOLD_ORE.defaultBlockState();
                    case 4: return Blocks.ANCIENT_DEBRIS.defaultBlockState();
                    case 5: return Blocks.BROWN_MUSHROOM.defaultBlockState();
                    case 6: return Blocks.RED_MUSHROOM.defaultBlockState();
                    case 7: return Blocks.CRIMSON_STEM.defaultBlockState();
                    case 8: return Blocks.WARPED_STEM.defaultBlockState();
                    case 9: return Blocks.CRIMSON_ROOTS.defaultBlockState();
                    case 10: return Blocks.WARPED_ROOTS.defaultBlockState();
                    case 11: return Blocks.CRIMSON_FUNGUS.defaultBlockState();
                    case 12: return Blocks.WARPED_FUNGUS.defaultBlockState();
                    case 13: return randomPottedNetherThing( random );
                }
            }
            else if( blockUnderFeet.is( Blocks.END_STONE ) || blockUnderFeet.is( Blocks.END_STONE_BRICKS ) ||
                    blockUnderFeet.is( Blocks.PURPUR_BLOCK ) ) {
                // End theme
                switch( random.nextInt( 10 ) ) {
                    case 0: return Blocks.CHORUS_PLANT.defaultBlockState();
                    case 1: return Blocks.PURPUR_PILLAR.defaultBlockState();
                    case 2: return randomPottedEndThing( random );
                }
            }
        }
        
        // Pick a random nearby render-able block
        final BlockPos.MutableBlockPos randPos = new BlockPos.MutableBlockPos();
        for( int i = 0; i < 16; i++ ) {
            randPos.set(
                    posUnderFeet.getX() + random.nextInt( 17 ) - 8,
                    posUnderFeet.getY() + random.nextInt( 5 ) - 2,
                    posUnderFeet.getZ() + random.nextInt( 17 ) - 8 );
            
            final BlockState randBlock = entity.level().getBlockState( randPos );
            if( randBlock.getRenderShape() == RenderShape.MODEL ) return randBlock;
        }
        
        // Hide as a log if none of the other options are chosen
        return Blocks.OAK_LOG.defaultBlockState();
    }
    
    /** @return The block state with a random rotation (horizontal facing). */
    private static BlockState randomRotation( BlockState block, RandomSource random ) {

        return block.setValue( BlockStateProperties.HORIZONTAL_FACING, Direction.Plane.HORIZONTAL.getRandomDirection( random ) );
    }
    
    /** @return A random flower. */
    @SuppressWarnings("ConstantConditions")
    private static BlockState randomFlower(RandomSource random ) {
        return ForgeRegistries.BLOCKS.tags().getTag(BlockTags.SMALL_FLOWERS).getRandomElement( random ).orElse( Blocks.POPPY ).defaultBlockState();
    }
    
    /** @return A random potted flower. */
    private static BlockState randomPottedFlower( RandomSource random ) {

        return switch (random.nextInt(12)) {
            case 0 -> Blocks.POTTED_DANDELION.defaultBlockState();
            case 1 -> Blocks.POTTED_POPPY.defaultBlockState();
            case 2 -> Blocks.POTTED_BLUE_ORCHID.defaultBlockState();
            case 3 -> Blocks.POTTED_ALLIUM.defaultBlockState();
            case 4 -> Blocks.POTTED_AZURE_BLUET.defaultBlockState();
            case 5 -> Blocks.POTTED_RED_TULIP.defaultBlockState();
            case 6 -> Blocks.POTTED_ORANGE_TULIP.defaultBlockState();
            case 7 -> Blocks.POTTED_WHITE_TULIP.defaultBlockState();
            case 8 -> Blocks.POTTED_PINK_TULIP.defaultBlockState();
            case 9 -> Blocks.POTTED_OXEYE_DAISY.defaultBlockState();
            case 10 -> Blocks.POTTED_CORNFLOWER.defaultBlockState();
            default -> Blocks.POTTED_LILY_OF_THE_VALLEY.defaultBlockState();
        };
    }
    
    /** @return A random potted non-flower plant. */
    private static BlockState randomPottedPlant( RandomSource random ) {
        return switch (random.nextInt(8)) {
            case 0 -> Blocks.POTTED_OAK_SAPLING.defaultBlockState();
            case 1 -> Blocks.POTTED_SPRUCE_SAPLING.defaultBlockState();
            case 2 -> Blocks.POTTED_BIRCH_SAPLING.defaultBlockState();
            case 3 -> Blocks.POTTED_JUNGLE_SAPLING.defaultBlockState();
            case 4 -> Blocks.POTTED_ACACIA_SAPLING.defaultBlockState();
            case 5 -> Blocks.POTTED_DARK_OAK_SAPLING.defaultBlockState();
            case 6 -> Blocks.POTTED_FERN.defaultBlockState();
            default -> Blocks.POTTED_BAMBOO.defaultBlockState();
        };
    }
    
    /** @return A random potted desert plant. */
    private static BlockState randomPottedDesertPlant( RandomSource random ) {
        //noinspection SwitchStatementWithTooFewBranches
        return switch (random.nextInt(2)) {
            case 0 -> Blocks.POTTED_DEAD_BUSH.defaultBlockState();
            default -> Blocks.POTTED_CACTUS.defaultBlockState();
        };
    }
    
    /** @return A random potted Nether plant/fungus. */
    private static BlockState randomPottedNetherThing( RandomSource random ) {
        return switch (random.nextInt(7)) {
            case 0 -> Blocks.POTTED_WITHER_ROSE.defaultBlockState();
            case 1 -> Blocks.POTTED_RED_MUSHROOM.defaultBlockState();
            case 2 -> Blocks.POTTED_BROWN_MUSHROOM.defaultBlockState();
            case 3 -> Blocks.POTTED_CRIMSON_FUNGUS.defaultBlockState();
            case 4 -> Blocks.POTTED_WARPED_FUNGUS.defaultBlockState();
            case 5 -> Blocks.POTTED_CRIMSON_ROOTS.defaultBlockState();
            default -> Blocks.POTTED_WARPED_ROOTS.defaultBlockState();
        };
    }
    
    /** @return A random potted End-themed plant. */
    private static BlockState randomPottedEndThing( RandomSource random ) {
        return switch (random.nextInt(5)) {
            case 0 -> Blocks.POTTED_WITHER_ROSE.defaultBlockState();
            case 1 -> Blocks.POTTED_ALLIUM.defaultBlockState();
            case 2 -> Blocks.POTTED_AZURE_BLUET.defaultBlockState();
            case 3 -> Blocks.POTTED_OXEYE_DAISY.defaultBlockState();
            default -> Blocks.POTTED_LILY_OF_THE_VALLEY.defaultBlockState();
        };
    }
}