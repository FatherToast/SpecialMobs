package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.MathHelper;

/**
 * A ranged attack goal that can be used passively while performing other actions.
 */
public class PassiveRangedAttackGoal<T extends MobEntity & ISpecialMob<? super T> & IRangedAttackMob> extends Goal {
    
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
        if( distSqr <= data.getRangedAttackMaxRange() * data.getRangedAttackMaxRange() && mob.canSee( target ) ) {
            attackTime++;
            if( attackTime >= data.getRangedAttackCooldown() ) {
                attackTime = MathHelper.floor( (data.getRangedAttackCooldown() - data.getRangedAttackMaxCooldown()) *
                        MathHelper.sqrt( distSqr ) / data.getRangedAttackMaxRange() );
                
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