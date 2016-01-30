package toast.specialMobs.entity;

/**
 * A mob must implement this interface to shoot EntitySpecialFishHook.
 * This allows get and set methods for the fish hook so that the entity and hook can
 * keep track of each other.
 */
public interface IAngler
{
    /**
     * Sets this angler's fish hook.
     * 
     * @param hook the angler's new fish hook
     */
    public void setFishHook(EntitySpecialFishHook hook);

    /**
     * Gets this angler's fish hook.
     * 
     * @return the angler's current fish hook, null if the angler does not have one out
     */
    public EntitySpecialFishHook getFishHook();
}