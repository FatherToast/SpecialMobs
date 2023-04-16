package fathertoast.specialmobs.common.entity.ai;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialHurtByTargetGoal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Provides helper methods for adjusting pre-defined AI goals.
 */
@SuppressWarnings( "unused" )
public final class AIHelper {
    
    /** Inserts an AI goal at the specified priority. Existing goals have their priority incremented accordingly. */
    public static void insertGoal(GoalSelector ai, int priority, Goal goal ) {
        for( WrappedGoal task : new ArrayList<>( ai.availableGoals ) ) {
            if( task.getPriority() >= priority ) task.priority++;
        }
        ai.addGoal( priority, goal );
    }
    
    /** Inserts an AI goal at the specified priority. Existing goals have their priority decremented accordingly. */
    public static void insertGoalReverse( GoalSelector ai, int priority, Goal goal ) {
        for( WrappedGoal task : new ArrayList<>( ai.availableGoals ) ) {
            if( task.getPriority() <= priority ) task.priority--;
        }
        ai.addGoal( priority, goal );
    }
    
    /** Removes all goals with the specified priority. */
    public static void removeGoals( GoalSelector ai, int priority ) {
        for( WrappedGoal task : new ArrayList<>( ai.availableGoals ) ) {
            if( task.getPriority() == priority ) ai.removeGoal( task.getGoal() );
        }
    }
    
    /** Removes all goals of the specified type. */
    public static void removeGoals( GoalSelector ai, Class<? extends Goal> goalType ) {
        for( WrappedGoal task : new ArrayList<>( ai.availableGoals ) ) {
            if( task.getGoal().getClass().equals( goalType ) ) ai.removeGoal( task.getGoal() );
        }
    }
    
    /** @return A goal with the specified priority; null if none are found. */
    @Nullable
    public static Goal getGoal( GoalSelector ai, int priority ) {
        for( WrappedGoal task : new ArrayList<>( ai.availableGoals ) ) {
            if( task.getPriority() == priority ) return task.getGoal();
        }
        SpecialMobs.LOG.warn( "Attempted to get '{}'-priority goal, but none exists!", priority );
        return null;
    }
    
    /** @return A goal of the specified type; null if none are found. */
    @Nullable
    public static Goal getGoal( GoalSelector ai, Class<? extends Goal> goalType ) {
        for( WrappedGoal task : new ArrayList<>( ai.availableGoals ) ) {
            if( task.getGoal().getClass().equals( goalType ) ) return task.getGoal();
        }
        SpecialMobs.LOG.warn( "Attempted to get '{}' goal, but none exists!", goalType.getSimpleName() );
        return null;
    }
    
    /** Replaces the entity's water avoiding random walking goal with an equivalent non-water-avoiding goal. */
    public static void replaceWaterAvoidingRandomWalking( PathfinderMob entity, double speedModifier ) {
        for( WrappedGoal task : new ArrayList<>( entity.goalSelector.availableGoals ) ) {
            if( task.getGoal() instanceof WaterAvoidingRandomStrollGoal ) {
                final int priority = task.getPriority();
                entity.goalSelector.removeGoal( task.getGoal() );
                entity.goalSelector.addGoal( priority, new RandomStrollGoal( entity, speedModifier ) );
                return;
            }
        }
        SpecialMobs.LOG.warn( "Attempted to replace random walking goal for {}, but none exists!", entity.getClass().getSimpleName() );
    }

    /** Replaces the entity's hurt by target goal with an equivalent replacement more compatible with special mobs. */
    public static void replaceHurtByTarget( PathfinderMob entity, SpecialHurtByTargetGoal replacement ) {
        for( WrappedGoal task : new ArrayList<>( entity.targetSelector.availableGoals ) ) {
            if( task.getGoal() instanceof HurtByTargetGoal) {
                final int priority = task.getPriority();
                entity.targetSelector.removeGoal( task.getGoal() );
                entity.targetSelector.addGoal( priority, replacement );
                return;
            }
        }
        SpecialMobs.LOG.warn( "Attempted to replace hurt by target goal for {}, but none exists!", entity.getClass().getSimpleName() );
    }
}