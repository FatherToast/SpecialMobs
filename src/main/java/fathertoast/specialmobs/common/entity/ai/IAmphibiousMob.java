package fathertoast.specialmobs.common.entity.ai;

/**
 * Monsters must implement this interface to use the amphibious AI.
 */
public interface IAmphibiousMob {
    /** @return True if this mob should use its swimming navigator for its current goal. */
    default boolean shouldSwim() { return isSwimmingUp(); }
    
    /** Sets whether this mob should swim upward. */
    void setSwimmingUp( boolean value );
    
    /** @return True if this mob should swim upward. */
    boolean isSwimmingUp();
    
    /** Sets this mob's current navigator to swimming mode. */
    void setNavigatorToSwim();
    
    /** Sets this mob's current navigator to ground mode. */
    void setNavigatorToGround();
}