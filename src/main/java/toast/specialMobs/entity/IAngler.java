package toast.specialMobs.entity;

/**
 * A mob must implement this interface to shoot EntitySpecialFishHook.
 * This allows get and set methods for the fishhook so that the entity and hook can
 * keep track of each other.
 */
public interface IAngler {
    /**
     * Sets this angler's fishhook.
     *
     * @param hook the angler's new fishhook
     */
    void setFishHook( EntitySpecialFishHook hook );
    
    /**
     * Gets this angler's fishhook.
     *
     * @return the angler's current fishhook, null if the angler does not have one out
     */
    EntitySpecialFishHook getFishHook();
}