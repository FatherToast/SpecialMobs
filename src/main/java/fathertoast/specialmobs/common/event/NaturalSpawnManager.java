package fathertoast.specialmobs.common.event;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.CaveSpiderEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.world.MobSpawnInfoBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class NaturalSpawnManager {
    
    //--------------- Spawn Placement Registration ----------------
    
    /** Sets the natural spawn placement rules for entity types. */
    public static void registerSpawnPlacements() {
        // Bestiary-generated entities
        for( MobFamily.Species<?> species : MobFamily.getAllSpecies() ) {
            species.registerSpawnPlacement();
        }
        
        // Additional entries
        if( Config.MAIN.NATURAL_SPAWNING.caveSpiderSpawnMultiplier.get() > 0.0 ) {
            try {
                EntitySpawnPlacementRegistry.register( EntityType.CAVE_SPIDER, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
                        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, NaturalSpawnManager::checkSpawnRulesCaveSpider );
            }
            catch( IllegalStateException ex ) {
                // Overwriting the vanilla entry with our own throws this exception, but we can just ignore it :^)
            }
        }
    }
    
    public static void registerSpawnPlacement( MobFamily.Species<? extends MonsterEntity> species ) {
        registerSpawnPlacement( species, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND );
    }
    
    public static void registerSpawnPlacement( MobFamily.Species<? extends MonsterEntity> species,
                                               EntitySpawnPlacementRegistry.PlacementType type ) {
        registerSpawnPlacement( species, type, NaturalSpawnManager::checkSpawnRulesDefault );
    }
    
    public static <T extends MobEntity> void registerSpawnPlacement( MobFamily.Species<T> species,
                                                                     EntitySpawnPlacementRegistry.IPlacementPredicate<T> predicate ) {
        registerSpawnPlacement( species, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, predicate );
    }
    
    public static <T extends MobEntity> void registerSpawnPlacement( MobFamily.Species<T> species,
                                                                     EntitySpawnPlacementRegistry.PlacementType type,
                                                                     EntitySpawnPlacementRegistry.IPlacementPredicate<T> predicate ) {
        EntitySpawnPlacementRegistry.register( species.entityType.get(), type, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, predicate );
    }
    
    public static boolean checkSpawnRulesDefault( EntityType<? extends MonsterEntity> type, IServerWorld world,
                                                  SpawnReason reason, BlockPos pos, Random random ) {
        return MonsterEntity.checkMonsterSpawnRules( type, world, reason, pos, random ) &&
                checkSpawnRulesConfigured( type, world, reason, pos, random );
    }
    
    public static boolean checkSpawnRulesIgnoreLight( EntityType<? extends MonsterEntity> type, IServerWorld world,
                                                      SpawnReason reason, BlockPos pos, Random random ) {
        return MonsterEntity.checkAnyLightMonsterSpawnRules( type, world, reason, pos, random ) &&
                checkSpawnRulesConfigured( type, world, reason, pos, random );
    }
    
    public static boolean checkSpawnRulesConfigured( EntityType<? extends LivingEntity> type, IServerWorld world,
                                                     SpawnReason reason, BlockPos pos, Random random ) {
        if( reason == SpawnReason.NATURAL ) {
            final MobFamily.Species<?> species = MobFamily.Species.of( type );
            if( species != null && world instanceof World ) {
                return species.config.GENERAL.naturalSpawnChance.rollChance( random, (World) world, pos );
            }
        }
        return true;
    }
    
    public static boolean checkSpawnRulesCaveSpider( EntityType<CaveSpiderEntity> type, IServerWorld world,
                                                     SpawnReason reason, BlockPos pos, Random random ) {
        if( reason == SpawnReason.NATURAL && world instanceof World &&
                !Config.MAIN.NATURAL_SPAWNING.caveSpiderSpawnChance.rollChance( random, (World) world, pos ) ) {
            return false;
        }
        return MonsterEntity.checkMonsterSpawnRules( type, world, reason, pos, random );
    }
    
    
    //--------------- Added Natural Spawns ----------------
    
    public static void onBiomeLoad( BiomeLoadingEvent event ) {
        final MobSpawnInfoBuilder spawnInfoBuilder = event.getSpawns();
        
        if( Config.MAIN.NATURAL_SPAWNING.caveSpiderSpawnMultiplier.get() > 0.0 ) {
            addCaveSpiderSpawns( spawnInfoBuilder );
        }
    }
    
    /** Adds cave spiders to the spawn list by copying regular spider spawn entries. Does nothing if cave spiders are already added. */
    private static void addCaveSpiderSpawns( MobSpawnInfoBuilder builder ) {
        // First, we figure out what needs to be added and make sure cave spiders do not already naturally spawn
        final List<MobSpawnInfo.Spawners> spiderSpawners = new ArrayList<>();
        final List<MobSpawnInfo.Spawners> spawners = builder.getSpawner( EntityClassification.MONSTER );
        for( MobSpawnInfo.Spawners spawner : spawners ) {
            if( spawner.type == EntityType.CAVE_SPIDER && spawner.weight > 0 ) return;
            if( spawner.type == EntityType.SPIDER && spawner.weight > 0 ) spiderSpawners.add( spawner );
        }
        
        // Then, we actually add any needed spawns
        if( !spiderSpawners.isEmpty() ) {
            for( MobSpawnInfo.Spawners spawner : spiderSpawners ) {
                builder.addSpawn( EntityClassification.MONSTER, new MobSpawnInfo.Spawners( EntityType.CAVE_SPIDER,
                        Math.max( 1, (int) (spawner.weight * Config.MAIN.NATURAL_SPAWNING.caveSpiderSpawnMultiplier.get()) ),
                        spawner.minCount, spawner.maxCount ) );
            }
            
            final MobSpawnInfo.SpawnCosts spiderCost = builder.getCost( EntityType.SPIDER );
            if( spiderCost != null ) {
                // Just leave them the same as a regular spider, config can be added if anyone actually wants it
                builder.addMobCharge( EntityType.CAVE_SPIDER, spiderCost.getCharge(), spiderCost.getEnergyBudget() );
            }
        }
    }
    
    private static void addSpawn( MobSpawnInfoBuilder builder, EntityType<?> entity, int weight ) {
        builder.addSpawn( entity.getCategory(), new MobSpawnInfo.Spawners( entity, weight, 4, 4 ) );
    }
    
    private static void addSpawn( MobSpawnInfoBuilder builder, EntityType<?> entity, int weight, int maxCount ) {
        builder.addSpawn( entity.getCategory(), new MobSpawnInfo.Spawners( entity, weight, 1, maxCount ) );
    }
    
    private static void addSpawn( MobSpawnInfoBuilder builder, EntityType<?> entity, int weight, int minCount, int maxCount ) {
        builder.addSpawn( entity.getCategory(), new MobSpawnInfo.Spawners( entity, weight, minCount, maxCount ) );
    }
}