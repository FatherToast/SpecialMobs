package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.ai.IExplodingMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * The "creeper swell" goal repurposed for use on other mobs.
 * <p>
 * {@link net.minecraft.entity.ai.goal.CreeperSwellGoal}
 */
public class SpecialSwellGoal<T extends MobEntity & IExplodingMob> extends Goal {
    
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
        if( target == null || mob.distanceToSqr( target ) > 49.0 || !mob.getSensing().canSee( target ) ) {
            mob.setSwellDir( -1 );
        }
        else {
            mob.setSwellDir( 1 );
        }
    }
}