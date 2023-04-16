package fathertoast.specialmobs.common.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * A path navigator used for entities that can walk on fluids. Based on the strider's lava path navigator.
 */
public class FluidPathNavigator extends GroundPathNavigation {
    
    private final boolean waterWalkable;
    private final boolean lavaWalkable;
    
    public FluidPathNavigator( Mob entity, Level level, boolean water, boolean lava ) {
        super( entity, level );
        waterWalkable = water;
        lavaWalkable = lava;
    }
    
    /** @return A new pathfinder instance with a given max size (based on entity follow range). */
    @Override
    protected PathFinder createPathFinder(int maxPathSize ) {
        nodeEvaluator = new WalkNodeEvaluator();
        return new PathFinder( nodeEvaluator, maxPathSize );
    }
    
    /** @return Whether the path node type is walkable. */
    @Override
    protected boolean hasValidPathType( BlockPathTypes type ) {
        return waterWalkable && (type == BlockPathTypes.WATER || type == BlockPathTypes.WATER_BORDER) ||
                lavaWalkable && (type == BlockPathTypes.LAVA || type == BlockPathTypes.DAMAGE_FIRE || type == BlockPathTypes.DANGER_FIRE) ||
                super.hasValidPathType( type );
    }
    
    /** @return True if the given position is a block that is suitable for standing on. */
    @Override
    public boolean isStableDestination( BlockPos pos ) {
        final BlockState block = level.getBlockState( pos );
        return waterWalkable && block.is( Blocks.WATER ) || lavaWalkable && block.is( Blocks.LAVA ) ||
                super.isStableDestination( pos );
    }
}