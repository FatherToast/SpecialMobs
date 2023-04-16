package fathertoast.specialmobs.common.entity;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import javax.annotation.Nullable;

public interface ISpecialMob<T extends Mob & ISpecialMob<T>> {
    
    /** @return This mob's special data. */
    SpecialMobData<T> getSpecialData();
    
    /** @return This mob's species. */
    MobFamily.Species<? extends T> getSpecies();
    
    /** @return The experience that should be dropped by this entity. */
    int getExperience();
    
    /** Sets the experience that should be dropped by this entity. */
    void setExperience( int xp );
    
    /** Sets the entity's pathfinding malus for a particular node type; negative value is un-walkable. */
    void setSpecialPathfindingMalus( BlockPathTypes pathType, float malus );
    
    /** Called on spawn to initialize properties based on the world, difficulty, and the group it spawns with. */
    void finalizeSpecialSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnType,
                              @Nullable SpawnGroupData groupData );
}