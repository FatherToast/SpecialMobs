package fathertoast.specialmobs.common.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.pathfinding.PathNodeType;

public interface ISpecialMob<T extends LivingEntity & ISpecialMob<T>> {
    /** @return This mob's special data. */
    SpecialMobData<T> getSpecialData();
    
    /** @return The experience that should be dropped by the entity. */
    int getExperience();
    
    /** Sets the experience that should be dropped by the entity. */
    void setExperience( int xp );
    
    /** Sets the entity's pathfinding malus for a particular node type; negative value is un-walkable. */
    void setPathfindingMalus( PathNodeType nodeType, float malus );
}