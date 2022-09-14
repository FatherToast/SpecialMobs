package fathertoast.specialmobs.common.entity.ai;

/**
 * Monsters must implement this interface to shoot fish hooks.
 * This allows get and set methods for the fish hook so that the server can communicate rendering info to the client.
 */
public interface IAngler {
    
    /** Sets this angler's line as out (or in). */
    void setLineOut( boolean value );
    
    /** @return Whether this angler's line is out. */
    boolean isLineOut();
}