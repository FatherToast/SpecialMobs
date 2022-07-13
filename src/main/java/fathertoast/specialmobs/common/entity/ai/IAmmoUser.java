package fathertoast.specialmobs.common.entity.ai;

/**
 * Monsters must implement this interface to use ammo-based AI goals.
 * The default implementation is "unlimited ammo".
 */
public interface IAmmoUser {
    /** @return True if this entity has ammo to use. */
    default boolean hasAmmo() { return true; }
    
    /** Consumes ammo for a single use. */
    default void consumeAmmo() { }
}