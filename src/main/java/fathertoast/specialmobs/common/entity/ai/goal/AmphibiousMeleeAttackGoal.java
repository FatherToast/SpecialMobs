package fathertoast.specialmobs.common.entity.ai.goal;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

import javax.annotation.Nullable;

/**
 * The drowned "attack" goal repurposed for use on other mobs.
 * Prevents mobs from attacking targets outside of water during the day.
 * <p>
 * {@link net.minecraft.entity.monster.DrownedEntity.AttackGoal}
 */
public class AmphibiousMeleeAttackGoal extends MeleeAttackGoal {
    
    /** @return True if the target is valid. */
    public static boolean isValidTarget( @Nullable LivingEntity target ) {
        return target != null && (!target.level.isDay() || target.isInWater());
    }
    
    public AmphibiousMeleeAttackGoal( CreatureEntity entity, double speed, boolean longMemory ) { super( entity, speed, longMemory ); }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() { return super.canUse() && isValidTarget( mob.getTarget() ); }
    
    /** @return Called each update while active and returns true if this AI can remain active. */
    @Override
    public boolean canContinueToUse() { return super.canContinueToUse() && isValidTarget( mob.getTarget() ); }
    
    // Uncomment if ever needed
    //    /** A zombie attack version of the normal amphibious melee goal. */
    //    public static class Zombie extends ZombieAttackGoal {
    //
    //        public Zombie( ZombieEntity entity, double speed, boolean longMemory ) { super( entity, speed, longMemory ); }
    //
    //        /** @return Returns true if this AI can be activated. */
    //        @Override
    //        public boolean canUse() { return super.canUse() && isValidTarget( mob.getTarget() ); }
    //
    //        /** @return Called each update while active and returns true if this AI can remain active. */
    //        @Override
    //        public boolean canContinueToUse() { return super.canContinueToUse() && isValidTarget( mob.getTarget() ); }
    //    }
}