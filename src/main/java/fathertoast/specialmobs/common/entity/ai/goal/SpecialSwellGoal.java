package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.ai.IExplodingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * The "creeper swell" goal repurposed for use on other mobs.
 * <p>
 * {@link net.minecraft.world.entity.ai.goal.SwellGoal}
 */
public class SpecialSwellGoal<T extends Mob & IExplodingMob> extends Goal {
    
    private final T mob;
    
    private LivingEntity target;
    
    public SpecialSwellGoal( T entity ) {
        mob = entity;
        setFlags( EnumSet.of( Flag.MOVE ) );
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() {
        final LivingEntity target = mob.getTarget();
        return mob.getSwellDir() > 0 || target != null && mob.distanceToSqr( target ) < 9.0F + mob.getExtraRange();
    }
    
    /** Called when this AI is activated. */
    @Override
    public void start() {
        mob.getNavigation().stop();
        target = mob.getTarget();
    }
    
    /** Called when this AI is deactivated. */
    @Override
    public void stop() {
        mob.setSwellDir( -1 );
        target = null;
    }
    
    /** Called each tick while this AI is active. */
    @Override
    public void tick() {
        if( target == null || mob.distanceToSqr( target ) > 49.0 || !mob.getSensing().hasLineOfSight( target ) ) {
            mob.setSwellDir( -1 );
        }
        else {
            mob.setSwellDir( 1 );
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}