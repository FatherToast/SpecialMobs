package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.ai.IAngler;
import fathertoast.specialmobs.common.entity.projectile.SpecialFishingBobberEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;

/**
 * Causes the entity to use a fishing rod to pull its target in close.
 */
public class AnglerGoal<T extends Mob & ISpecialMob<? super T> & IAngler> extends Goal {
    
    private final T mob;
    
    private SpecialFishingBobberEntity bobber;
    private int castTime;
    
    public AnglerGoal( T entity ) {
        mob = entity;
    }
    
    private void setBobber( @Nullable SpecialFishingBobberEntity newBobber ) {
        if( bobber != null ) {
            bobber.discard();
        }
        bobber = newBobber;
        mob.setLineOut( newBobber != null );
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() { return mob.getTarget() != null && mob.getSpecialData().getRangedAttackMaxRange() > 4.0F; }
    
    /** Called when this AI is deactivated. */
    @Override
    public void stop() {
        setBobber( null );
        castTime = 0;
    }
    
    /** Called each tick while this AI is active. */
    @Override
    public void tick() {
        final LivingEntity target = mob.getTarget();
        if( target == null ) return;
        
        // Detect when the bobber gets destroyed
        if( bobber != null && !bobber.isAlive() ) {
            bobber = null;
            mob.setLineOut( false );
        }
        
        final SpecialMobData<?> data = mob.getSpecialData();
        
        final double distSqr = target.distanceToSqr( mob );
        if( distSqr > 16.0 && distSqr <= data.getRangedAttackMaxRange() * data.getRangedAttackMaxRange() && mob.hasLineOfSight( target ) ) {
            castTime++;
            if( castTime >= data.getRangedAttackCooldown() && mob.getRandom().nextInt( 5 ) == 0 ) {
                castTime = 0;
                
                final SpecialFishingBobberEntity newBobber = new SpecialFishingBobberEntity( mob, target );
                mob.playSound( SoundEvents.FISHING_BOBBER_THROW, 1.0F, 0.4F / (mob.getRandom().nextFloat() * 0.4F + 0.8F) );
                mob.level().addFreshEntity( newBobber );
                setBobber( newBobber );
            }
        }
        else if( castTime > 0 ) castTime--;
    }
}