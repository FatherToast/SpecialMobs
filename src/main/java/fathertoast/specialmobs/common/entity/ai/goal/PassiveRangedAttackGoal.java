package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * A ranged attack goal that can be used passively while performing other actions.
 */
public class PassiveRangedAttackGoal<T extends Mob & ISpecialMob<? super T> & RangedAttackMob> extends Goal {
    
    private final T mob;
    private final float recoilPower;
    
    private int attackTime;
    
    public PassiveRangedAttackGoal( T entity ) { this( entity, 0.0F ); }
    
    public PassiveRangedAttackGoal( T entity, float recoil ) {
        mob = entity;
        recoilPower = recoil;
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() { return mob.getTarget() != null && mob.getSpecialData().getRangedAttackMaxRange() > 0.0F; }
    
    /** Called each tick while this AI is active. */
    @Override
    public void tick() {
        final LivingEntity target = mob.getTarget();
        if( target == null ) return;
        
        final SpecialMobData<?> data = mob.getSpecialData();
        
        final double distSqr = target.distanceToSqr( mob );
        if( distSqr <= data.getRangedAttackMaxRange() * data.getRangedAttackMaxRange() && mob.hasLineOfSight( target ) ) {
            attackTime++;
            if( attackTime >= data.getRangedAttackCooldown() ) {
                attackTime = Mth.floor( (data.getRangedAttackCooldown() - data.getRangedAttackMaxCooldown()) *
                        Mth.sqrt( (float) distSqr ) / data.getRangedAttackMaxRange() );
                
                mob.performRangedAttack( target, 1.0F );
                
                if( recoilPower != 0.0F ) {
                    MobHelper.knockback( mob, recoilPower, 0.0F,
                            target.getX() - mob.getX(), target.getZ() - mob.getZ(), 0.2 );
                }
            }
        }
        else if( attackTime < 0 ) attackTime++;
        else if( attackTime > 0 ) attackTime--;
    }
}