package fathertoast.specialmobs.common.event;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.entity.creeper.DrowningCreeperEntity;
import fathertoast.specialmobs.common.entity.creeper.EnderCreeperEntity;
import fathertoast.specialmobs.common.entity.creeper.FireCreeperEntity;
import fathertoast.specialmobs.common.entity.slime.BlueberrySlimeEntity;
import fathertoast.specialmobs.common.entity.spider.FireSpiderEntity;
import fathertoast.specialmobs.common.entity.zombie.FireZombieEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.CaveSpiderEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.world.MobSpawnInfoBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class NaturalSpawnManager {
    
    //--------------- Spawn Placement Registration ----------------
    
    /** Sets the natural spawn placement rules for entity types. */
    public static void registerSpawnPlacements() {
        if( !Config.MAIN.GENERAL.enableNaturalSpawning.get() ) return;
        
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
    
    @SuppressWarnings( "unused" )
    public static boolean checkSpawnRulesBasic( EntityType<? extends MobEntity> type, IServerWorld world,
                                                SpawnReason reason, BlockPos pos, Random random ) {
        return world.getDifficulty() != Difficulty.PEACEFUL && MobEntity.checkMobSpawnRules( type, world, reason, pos, random ) &&
                checkSpawnRulesConfigured( type, world, reason, pos, random );
    }
    
    public static boolean checkSpawnRulesWater( EntityType<? extends MobEntity> type, IServerWorld world,
                                                SpawnReason reason, BlockPos pos, Random random ) {
        return world.getDifficulty() != Difficulty.PEACEFUL && MonsterEntity.isDarkEnoughToSpawn( world, pos, random ) &&
                (reason == SpawnReason.SPAWNER || world.getFluidState( pos ).is( FluidTags.WATER )) &&
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
        if( !Config.MAIN.GENERAL.enableNaturalSpawning.get() ) return;
        
        final MobSpawnInfoBuilder spawnInfoBuilder = event.getSpawns();
        
        addCopiedSpawns( spawnInfoBuilder );
        addBiomeCategorySpawns( spawnInfoBuilder, event.getCategory(), event.getName() );
    }
    
    /** Adds enabled spawn-copier mobs to the spawn list. */
    private static void addCopiedSpawns( MobSpawnInfoBuilder builder ) {
        addCopiedSpawns( builder, EntityType.SPIDER, EntityType.CAVE_SPIDER,
                Config.MAIN.NATURAL_SPAWNING.caveSpiderSpawnMultiplier.get() );
        
        addCopiedSpawns( builder, EntityType.ENDERMAN, EnderCreeperEntity.SPECIES.entityType.get(),
                Config.MAIN.NATURAL_SPAWNING.enderCreeperSpawnMultiplier.get() );
    }
    
    /** Adds an entity type to the spawn list by copying another type's spawn entries. Does nothing if the entity type is already added. */
    private static void addCopiedSpawns( MobSpawnInfoBuilder builder, EntityType<?> typeToCopy, EntityType<?> typeToAdd, double multi ) {
        if( multi <= 0.0 ) return;
        
        final List<MobSpawnInfo.Spawners> spawnersToCopy = new ArrayList<>();
        final List<MobSpawnInfo.Spawners> spawners = builder.getSpawner( EntityClassification.MONSTER );
        for( MobSpawnInfo.Spawners spawner : spawners ) {
            if( spawner.type == typeToAdd && spawner.weight > 0 ) return;
            if( spawner.type == typeToCopy && spawner.weight > 0 ) spawnersToCopy.add( spawner );
        }
        
        // Currently, we simply copy pack size and spawn costs directly; configs can be added later for these if needed
        if( !spawnersToCopy.isEmpty() ) {
            for( MobSpawnInfo.Spawners spawner : spawnersToCopy ) {
                addSpawn( builder, typeToAdd, Math.max( 1, MathHelper.floor( spawner.weight * multi ) ),
                        spawner.minCount, spawner.maxCount );
            }
            
            final MobSpawnInfo.SpawnCosts costsToCopy = builder.getCost( typeToCopy );
            if( costsToCopy != null ) {
                builder.addMobCharge( typeToAdd, costsToCopy.getCharge(), costsToCopy.getEnergyBudget() );
            }
        }
    }
    
    /** Adds enabled biome-category-based mobs to the spawn list. */
    private static void addBiomeCategorySpawns( MobSpawnInfoBuilder builder, Biome.Category category, @Nullable ResourceLocation name ) {
        switch( category ) {
            case OCEAN:
                addSpawn( builder, DrowningCreeperEntity.SPECIES.entityType.get(),
                        Config.MAIN.NATURAL_SPAWNING.drowningCreeperOceanWeight.get() );
                addSpawn( builder, BlueberrySlimeEntity.SPECIES.entityType.get(),
                        Config.MAIN.NATURAL_SPAWNING.blueberrySlimeOceanWeight.get() );
                break;
            case RIVER:
                addSpawn( builder, DrowningCreeperEntity.SPECIES.entityType.get(),
                        Config.MAIN.NATURAL_SPAWNING.drowningCreeperRiverWeight.get() );
                addSpawn( builder, BlueberrySlimeEntity.SPECIES.entityType.get(),
                        Config.MAIN.NATURAL_SPAWNING.blueberrySlimeRiverWeight.get() );
                break;
            
            case NETHER:
                addNetherSpawns( builder, name );
                break;
        }
    }
    
    /** Adds enabled extra nether mobs to the spawn list. */
    private static void addNetherSpawns( MobSpawnInfoBuilder builder, @Nullable ResourceLocation name ) {
        // Soul sand valley and warped forest biomes have unique spawn setups
        if( isBiome( name, Biomes.WARPED_FOREST ) ) {
            // Add warped variants here once they are created
            return;
        }
        if( isBiome( name, Biomes.SOUL_SAND_VALLEY ) ) {
            addSpawn( builder, EntityType.WITHER_SKELETON,
                    Config.MAIN.NATURAL_SPAWNING.witherSkeletonSoulSandValleyWeight.get(), 5, 5,
                    0.7, 0.15 );
            return;
        }
        
        //                if( isBiome( name, Biomes.CRIMSON_FOREST ) ) {
        //                    // Add crimson variants here once they are created
        //                    // Do not return here - this biome has normal spawns!
        //                }
        
        addSpawn( builder, EntityType.WITHER_SKELETON,
                Config.MAIN.NATURAL_SPAWNING.witherSkeletonNetherWeight.get(), 5, 5 );
        
        if( isBiome( name, Biomes.BASALT_DELTAS ) ) {
            addSpawn( builder, EntityType.BLAZE,
                    Config.MAIN.NATURAL_SPAWNING.blazeBasaltDeltasWeight.get(), 2, 3 );
        }
        else {
            addSpawn( builder, EntityType.BLAZE,
                    Config.MAIN.NATURAL_SPAWNING.blazeNetherWeight.get(), 2, 3 );
        }
        
        addSpawn( builder, FireCreeperEntity.SPECIES.entityType.get(),
                Config.MAIN.NATURAL_SPAWNING.fireCreeperNetherWeight.get(), 4, 4 );
        addSpawn( builder, FireZombieEntity.SPECIES.entityType.get(),
                Config.MAIN.NATURAL_SPAWNING.fireZombieNetherWeight.get(), 4, 4 );
        addSpawn( builder, FireSpiderEntity.SPECIES.entityType.get(),
                Config.MAIN.NATURAL_SPAWNING.fireSpiderNetherWeight.get(), 4, 4 );
    }
    
    /** @return True if the name represents a particular biome. */
    private static boolean isBiome( @Nullable ResourceLocation name, RegistryKey<Biome> biome ) {
        return biome.location().equals( name );
    }
    
    private static void addSpawn( MobSpawnInfoBuilder builder, EntityType<?> entity, int weight ) {
        addSpawn( builder, entity, weight, 1, 1 );
    }
    
    private static void addSpawn( MobSpawnInfoBuilder builder, EntityType<?> entity, int weight, int minCount, int maxCount ) {
        if( weight > 0 ) {
            builder.addSpawn( entity.getCategory(), new MobSpawnInfo.Spawners( entity, weight, minCount, maxCount ) );
        }
    }
    
    private static void addSpawn( MobSpawnInfoBuilder builder, EntityType<?> entity, int weight, int minCount, int maxCount,
                                  double charge, double budget ) {
        if( weight > 0 ) {
            builder.addSpawn( entity.getCategory(), new MobSpawnInfo.Spawners( entity, weight, minCount, maxCount ) );
            builder.addMobCharge( entity, charge, budget );
        }
    }
}