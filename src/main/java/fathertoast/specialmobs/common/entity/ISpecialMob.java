package fathertoast.specialmobs.common.entity;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;

import javax.annotation.Nullable;

public interface ISpecialMob<T extends LivingEntity & ISpecialMob<T>> {
    
    /** @return This mob's special data. */
    SpecialMobData<T> getSpecialData();
    
    /** @return This mob's species. */
    MobFamily.Species<? extends T> getSpecies();
    
    /** @return The experience that should be dropped by this entity. */
    int getExperience();
    
    /** Sets the experience that should be dropped by this entity. */
    void setExperience( int xp );
    
    /** Sets the entity's pathfinding malus for a particular node type; negative value is un-walkable. */
    void setPathfindingMalus( PathNodeType nodeType, float malus );
    
    /** Called on spawn to initialize properties based on the world, difficulty, and the group it spawns with. */
    void finalizeSpecialSpawn( IServerWorld world, DifficultyInstance difficulty, @Nullable SpawnReason spawnReason,
                               @Nullable ILivingEntityData groupData );
}