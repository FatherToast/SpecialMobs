package toast.specialMobs.entity;

/**
 * This interface is implemented by all monsters in this mod.
 * It allows for commonly used data to be stored conveniently and for
 * attribute changes to be appropriately applied.
 */
public interface ISpecialMob
{
    /**
     * @return this mob's special data
     */
    public SpecialMobData getSpecialData();

    /**
     * Called to modify the mob's inherited attributes.
     */
    public void adjustEntityAttributes();
}