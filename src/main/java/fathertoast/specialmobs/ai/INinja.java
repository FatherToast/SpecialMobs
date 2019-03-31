package fathertoast.specialmobs.ai;

import net.minecraft.block.state.IBlockState;

/**
 * A mob must implement this interface to use EntityAINinja.
 * This allows get and set methods for the disguise block so the ai knows when to activate.
 */
public
interface INinja
{
	// Gets whether the ninja should not move.
	boolean isInDisguise( );
	
	// Sets the ninja as an immovable object.
	void setInDisguise( boolean disguised );
	
	// Gets the block being hidden as, or null if not hiding.
	IBlockState getDisguiseBlock( );
	
	// Sets the block being hidden as, set to null to cancel hiding.
	void setDisguiseBlock( IBlockState block );
}
