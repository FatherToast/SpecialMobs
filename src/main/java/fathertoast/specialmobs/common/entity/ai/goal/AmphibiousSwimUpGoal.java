package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.ai.IAmphibiousMob;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * The drowned "swim up" goal repurposed for use on other mobs.
 * <p>
 * {@link net.minecraft.world.entity.monster.Drowned.DrownedSwimUpGoal}
 */
@SuppressWarnings("JavadocReference")
public class AmphibiousSwimUpGoal<T extends PathfinderMob & IAmphibiousMob> extends Goal {
    
    private final T mob;
    private final double speedModifier;
    private final int seaLevel;
    
    private boolean disableAtDay = true;
    
    private boolean stuck;
    
    public AmphibiousSwimUpGoal( T entity, double speed ) {
        mob = entity;
        speedModifier = speed;
        seaLevel = entity.level().getSeaLevel() - 1;
    }
    
    /** Builder that allows this goal to run during the day. */
    public AmphibiousSwimUpGoal<T> alwaysEnabled() {
        disableAtDay = false;
        return this;
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() { return !(disableAtDay && mob.level().isDay()) && mob.isInWater() && mob.getY() < seaLevel - 1; }
    
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
            final Vec3 pos = DefaultRandomPos.getPosTowards( mob, 4, 8,
                    new Vec3( mob.getX(), seaLevel, mob.getZ() ), (float) Math.PI / 2F);
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