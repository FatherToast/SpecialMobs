package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.ai.IAmphibiousMob;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;

/**
 * The drowned "go to beach" goal repurposed for use on other mobs.
 * <p>
 * {@link net.minecraft.world.entity.monster.Drowned.DrownedGoToBeachGoal}
 */
@SuppressWarnings("JavadocReference")
public class AmphibiousGoToShoreGoal<T extends PathfinderMob & IAmphibiousMob> extends MoveToBlockGoal {
    
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
        return super.canUse() && !(disableAtDay && mob.level().isDay()) && mob.isInWater() && mob.getY() >= mob.level().getSeaLevel() - 3;
    }
    
    /** @return True if the position is valid to move to. */
    @Override
    protected boolean isValidTarget( LevelReader level, BlockPos targetPos ) {
        return level.isEmptyBlock( targetPos.above() ) && level.isEmptyBlock( targetPos.above( 2 ) ) &&
                level.getBlockState( targetPos ).entityCanStandOn( level, targetPos, mob );
    }
    
    /** Called when this AI is activated. */
    @Override
    public void start() {
        amphibiousMob.setSwimmingUp( false );
        amphibiousMob.setNavigatorToGround();
        super.start();
    }
}