package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.ai.IAmphibiousMob;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

/**
 * The drowned "swim up" goal repurposed for use on other mobs.
 * <p>
 * {@link net.minecraft.entity.monster.DrownedEntity.SwimUpGoal}
 */
public class AmphibiousSwimUpGoal<T extends CreatureEntity & IAmphibiousMob> extends Goal {
    
    private final T mob;
    private final double speedModifier;
    private final int seaLevel;
    
    private boolean disableAtDay = true;
    
    private boolean stuck;
    
    public AmphibiousSwimUpGoal( T entity, double speed ) {
        mob = entity;
        speedModifier = speed;
        seaLevel = entity.level.getSeaLevel() - 1;
    }
    
    /** Builder that allows this goal to run during the day. */
    public AmphibiousSwimUpGoal<T> alwaysEnabled() {
        disableAtDay = false;
        return this;
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() { return !(disableAtDay && mob.level.isDay()) && mob.isInWater() && mob.getY() < seaLevel - 1; }
    
    /** @return Called each update while active and returns true if this AI can remain active. */
    @Override
    public boolean canContinueToUse() { return canUse() && !stuck; }
    
    /** Called when this AI is activated. */
    @Override
    public void start() {
        mob.setSwimmingUp( true );
        stuck = false;
    }
    
    /** Called when this AI is deactivated. */
    @Override
    public void stop() { mob.setSwimmingUp( false ); }
    
    /** Called each tick while this AI is active. */
    @Override
    public void tick() {
        if( mob.getY() < seaLevel && (mob.getNavigation().isDone() || closeToNextPos()) ) {
            final Vector3d pos = RandomPositionGenerator.getPosTowards( mob, 4, 8,
                    new Vector3d( mob.getX(), seaLevel, mob.getZ() ) );
            if( pos == null ) {
                stuck = true;
                return;
            }
            
            mob.getNavigation().moveTo( pos.x, pos.y, pos.z, speedModifier );
        }
        
    }
    
    /** @return True if the entity is within 2 blocks of its pathing target. */
    private boolean closeToNextPos() {
        final Path path = mob.getNavigation().getPath();
        if( path != null ) {
            final BlockPos pos = path.getTarget();
            return mob.distanceToSqr( pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5 ) < 4.0;
        }
        return false;
    }
}