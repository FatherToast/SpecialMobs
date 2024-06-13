package fathertoast.specialmobs.common.event;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;

public final class NaturalSpawnManager {

    /**
     * Holder for the SpawnPlacementRegisterEvent when it is fired. Temporarily stored as a field to
     * avoid passing the event around as an argument in a bazillion methods.
     */
    private static SpawnPlacementRegisterEvent registerEvent = null;

    //--------------- Spawn Placement Registration ----------------
    
    /** Sets the natural spawn placement rules for entity types. */
    public static void registerSpawnPlacements( SpawnPlacementRegisterEvent event ) {
        if( !Config.MAIN.GENERAL.enableNaturalSpawning.get() ) return;

        registerEvent = event;
        
        // Bestiary-generated entities
        for( MobFamily.Species<?> species : MobFamily.getAllSpecies() ) {
            species.registerSpawnPlacement( );
        }
        
        // Additional entries
        if( Config.MAIN.NATURAL_SPAWNING.caveSpiderSpawnMultiplier.get() > 0.0 ) {
            try {
                event.register( EntityType.CAVE_SPIDER, SpawnPlacements.Type.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalSpawnManager::checkSpawnRulesCaveSpider, SpawnPlacementRegisterEvent.Operation.AND );
            }
            catch( IllegalStateException ex ) {
                // Overwriting the vanilla entry with our own throws this exception, but we can just ignore it :^)
            }
        }
        registerEvent = null;
    }
    
    public static void registerSpawnPlacement( MobFamily.Species<? extends Monster> species ) {
        registerSpawnPlacement( species, SpawnPlacements.Type.ON_GROUND );
    }
    
    public static void registerSpawnPlacement( MobFamily.Species<? extends Monster> species,
                                               SpawnPlacements.Type type ) {
        registerSpawnPlacement( species, type, NaturalSpawnManager::checkSpawnRulesDefault );
    }
    
    public static <T extends Mob> void registerSpawnPlacement(MobFamily.Species<T> species,
                                                              SpawnPlacements.SpawnPredicate<T> predicate ) {
        registerSpawnPlacement( species, SpawnPlacements.Type.ON_GROUND, predicate );
    }
    
    public static <T extends Mob> void registerSpawnPlacement( MobFamily.Species<T> species,
                                                               SpawnPlacements.Type type,
                                                               SpawnPlacements.SpawnPredicate<T> predicate ) {
        registerEvent.register( species.entityType.get(), type, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, SpawnPlacementRegisterEvent.Operation.AND );
    }
    
    public static boolean checkSpawnRulesDefault(EntityType<? extends Monster> type, ServerLevelAccessor level,
                                                 MobSpawnType spawnType, BlockPos pos, RandomSource random ) {
        return Monster.checkMonsterSpawnRules( type, level, spawnType, pos, random ) &&
                checkSpawnRulesConfigured( type, level, spawnType, pos, random );
    }
    
    public static boolean checkSpawnRulesIgnoreLight( EntityType<? extends Monster> type, ServerLevelAccessor level,
                                                      MobSpawnType spawnType, BlockPos pos, RandomSource random ) {
        return Monster.checkAnyLightMonsterSpawnRules( type, level, spawnType, pos, random ) &&
                checkSpawnRulesConfigured( type, level, spawnType, pos, random );
    }
    
    @SuppressWarnings( "unused" )
    public static boolean checkSpawnRulesBasic( EntityType<? extends Mob> type, ServerLevelAccessor level,
                                                MobSpawnType spawnType, BlockPos pos, RandomSource random ) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules( type, level, spawnType, pos, random ) &&
                checkSpawnRulesConfigured( type, level, spawnType, pos, random );
    }
    
    public static boolean checkSpawnRulesWater( EntityType<? extends Mob> type, ServerLevelAccessor level,
                                                MobSpawnType spawnType, BlockPos pos, RandomSource random ) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn( level, pos, random ) &&
                (spawnType == MobSpawnType.SPAWNER || level.getFluidState( pos ).is( FluidTags.WATER )) &&
                checkSpawnRulesConfigured( type, level, spawnType, pos, random );
    }
    
    public static boolean checkSpawnRulesConfigured(EntityType<? extends LivingEntity> type, ServerLevelAccessor levelAccessor,
                                                    MobSpawnType spawnType, BlockPos pos, RandomSource random ) {
        if( spawnType == MobSpawnType.NATURAL ) {
            final MobFamily.Species<?> species = MobFamily.Species.of( type );
            if( species != null && levelAccessor instanceof Level level ) {
                return species.config.GENERAL.naturalSpawnChance.rollChance( random, level, pos );
            }
        }
        return true;
    }
    
    public static boolean checkSpawnRulesCaveSpider( EntityType<CaveSpider> type, ServerLevelAccessor levelAccessor,
                                                     MobSpawnType spawnType, BlockPos pos, RandomSource random ) {
        if( spawnType == MobSpawnType.NATURAL && levelAccessor instanceof Level level &&
                !Config.MAIN.NATURAL_SPAWNING.caveSpiderSpawnChance.rollChance( random, level, pos ) ) {
            return false;
        }
        return Monster.checkMonsterSpawnRules( type, levelAccessor, spawnType, pos, random );
    }
}