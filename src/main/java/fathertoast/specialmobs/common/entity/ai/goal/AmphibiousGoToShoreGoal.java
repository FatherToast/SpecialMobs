package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.ai.IAmphibiousMob;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

/**
 * The drowned "go to beach" goal repurposed for use on other mobs.
 * <p>
 * {@link net.minecraft.entity.monster.DrownedEntity.GoToBeachGoal}
 */
public class AmphibiousGoToShoreGoal<T extends CreatureEntity & IAmphibiousMob> extends MoveToBlockGoal {
    
    private final T amphibiousMob;
    
    private boolean disableAtDay = true;
    
    public AmphibiousGoToShoreGoal( T entity, double speed ) {
        super( entity, speed, 8, 2 );
        amphibiousMob = entity;
    }
    
    /** Builder that allows this goal to run during the day. */
    public AmphibiousGoToShoreGoal<T> alwaysEnabled() {
        disableAtDay = false;
        return this;
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() {
        return super.canUse() && !(disableAtDay && mob.level.isDay()) && mob.isInWater() && mob.getY() >= mob.level.getSeaLevel() - 3;
    }
    
    /** @return True if the position is valid to move to. */
    @Override
    protected boolean isValidTarget( IWorldReader world, BlockPos targetPos ) {
        return world.isEmptyBlock( targetPos.above() ) && world.isEmptyBlock( targetPos.above( 2 ) ) &&
                world.getBlockState( targetPos ).entityCanStandOn( world, targetPos, mob );
    }
    
    /** Called when this AI is activated. */
    @Override
    public void start() {
        amphibiousMob.setSwimmingUp( false );
        amphibiousMob.setNavigatorToGround();
        super.start();
    }
}