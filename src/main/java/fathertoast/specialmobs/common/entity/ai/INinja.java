package fathertoast.specialmobs.common.entity.ai;

import net.minecraft.block.BlockState;

import javax.annotation.Nullable;

/**
 * Monsters must implement this interface to use the ninja goal AI.
 * This allows get and set methods for the disguise block and immovable state.
 */
public interface INinja {
    
    /** @return Whether this ninja is currently immovable. */
    boolean isCrouchingTiger();
    
    /** Sets this ninja's immovable state. When activated, the entity is 'snapped' to the nearest block position. */
    void setCrouchingTiger( boolean value );
    
    /** @return The block being hidden (rendered) as, or null if not hiding. */
    @Nullable
    BlockState getHiddenDragon();
    
    /** Sets the block being hidden (rendered) as, set to null to cancel hiding. */
    void setHiddenDragon( @Nullable BlockState block );
}