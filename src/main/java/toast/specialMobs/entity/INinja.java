package toast.specialMobs.entity;

import net.minecraft.block.Block;

/**
 * A mob must implement this interface to use EntityAINinja.
 * This allows get and set methods for the hiding block so the AI knows when it should freeze.
 */
public interface INinja
{
    // Gets whether the ninja should not move.
    public boolean getFrozen();

    // Sets the ninja as an immovable object.
    public void setFrozen(boolean frozen);

    // Gets the block being hidden as, or null if not hiding.
    public Block getHidingBlock();

    // Gets the metadata of the block being hidden as, if any.
    public int getHidingData();

    // Sets the block being hidden as, set to null or air to cancel hiding.
    public void setHidingBlock(Block block, int data);
}