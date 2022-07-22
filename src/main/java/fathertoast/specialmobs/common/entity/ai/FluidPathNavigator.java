package fathertoast.specialmobs.common.entity.ai;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A path navigator used for entities that can walk on fluids. Based on the strider's lava path navigator.
 */
public class FluidPathNavigator extends GroundPathNavigator {
    
    private final boolean waterWalkable;
    private final boolean lavaWalkable;
    
    public FluidPathNavigator( MobEntity entity, World world, boolean water, boolean lava ) {
        super( entity, world );
        waterWalkable = water;
        lavaWalkable = lava;
    }
    
    /** @return A new pathfinder instance with a given max size (based on entity follow range). */
    @Override
    protected PathFinder createPathFinder( int maxPathSize ) {
        nodeEvaluator = new WalkNodeProcessor();
        return new PathFinder( nodeEvaluator, maxPathSize );
    }
    
    /** @return Whether the path node type is walkable. */
    @Override
    protected boolean hasValidPathType( PathNodeType node ) {
        return waterWalkable && (node == PathNodeType.WATER || node == PathNodeType.WATER_BORDER) ||
                lavaWalkable && (node == PathNodeType.LAVA || node == PathNodeType.DAMAGE_FIRE || node == PathNodeType.DANGER_FIRE) ||
                super.hasValidPathType( node );
    }
    
    /** @return True if the given position is a block that is suitable for standing on. */
    @Override
    public boolean isStableDestination( BlockPos pos ) {
        final BlockState block = level.getBlockState( pos );
        return waterWalkable && block.is( Blocks.WATER ) || lavaWalkable && block.is( Blocks.LAVA ) ||
                super.isStableDestination( pos );
    }
}