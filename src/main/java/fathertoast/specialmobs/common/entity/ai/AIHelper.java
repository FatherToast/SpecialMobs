package fathertoast.specialmobs.common.entity.ai;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;

import java.util.ArrayList;

/**
 * Provides helper methods for adjusting pre-defined AI goals.
 */
public final class AIHelper {
    
    /** Inserts an AI goal at the specified priority. Existing goals have their priority incremented accordingly. */
    public static void insertGoal( GoalSelector ai, int priority, Goal goal ) {
        for( PrioritizedGoal task : new ArrayList<>( ai.availableGoals ) ) {
            if( task.getPriority() >= priority ) task.priority++;
        }
        ai.addGoal( priority, goal );
    }
    
    /** Inserts an AI goal at the specified priority. Existing goals have their priority decremented accordingly. */
    public static void insertGoalReverse( GoalSelector ai, int priority, Goal goal ) {
        for( PrioritizedGoal task : new ArrayList<>( ai.availableGoals ) ) {
            if( task.getPriority() <= priority ) task.priority--;
        }
        ai.addGoal( priority, goal );
    }
    
    /** Removes all goals with the specified priority. */
    public static void removeGoals( GoalSelector ai, int priority ) {
        for( PrioritizedGoal task : new ArrayList<>( ai.availableGoals ) ) {
            if( task.getPriority() == priority ) ai.removeGoal( task.getGoal() );
        }
    }
    
    /** Removes all goals of the specified type. */
    public static void removeGoals( GoalSelector ai, Class<? extends Goal> goalType ) {
        for( PrioritizedGoal task : new ArrayList<>( ai.availableGoals ) ) {
            if( task.getGoal().getClass().equals( goalType ) ) ai.removeGoal( task.getGoal() );
        }
    }
}