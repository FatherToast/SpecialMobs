package fathertoast.specialmobs.common.biome.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fathertoast.crust.api.config.common.field.IntField;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.core.register.SMBiomeMods;
import fathertoast.specialmobs.common.entity.creeper.*;
import fathertoast.specialmobs.common.entity.skeleton.PirateSkeletonEntity;
import fathertoast.specialmobs.common.entity.slime.BlueberrySlimeEntity;
import fathertoast.specialmobs.common.entity.spider.FireSpiderEntity;
import fathertoast.specialmobs.common.entity.witch.UndeadWitchEntity;
import fathertoast.specialmobs.common.entity.zombie.FireZombieEntity;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * This modifier is responsible for all the natural spawning of Special Mobs monsters.
 * There should only ever be one json using this modifier, seen as it is pretty much just
 * a workaround now that BiomeLoadEvent is no more.
 */
public record NaturalAddSpawnModifier( String comment ) implements BiomeModifier {
    
    public static final Codec<NaturalAddSpawnModifier> CODEC = RecordCodecBuilder.create( builder ->
            builder.group( Codec.STRING.fieldOf( "_comment" ).forGetter( NaturalAddSpawnModifier::comment ) )
                    .apply( builder, NaturalAddSpawnModifier::new ) );
    
    @Override
    public Codec<? extends BiomeModifier> codec() {
        return SMBiomeMods.NATURAL_ADD_SPAWN.get();
    }
    
    @Override
    public void modify( Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder ) {
        if( phase == Phase.ADD )
            addSpawns( builder.getMobSpawnSettings(), biome );
        else if( phase == Phase.MODIFY )
            modifySpawns( builder.getMobSpawnSettings() );
    }
    
    
    /** Adds enabled biome-category-based mob spawns to the biome. */
    private static void addSpawns( MobSpawnSettingsBuilder mobSpawns, Holder<Biome> biome ) {
        if( !Config.MAIN.ADDED_SPAWNS.enableAddedSpawns.get() ) return;
        
        // Water spawns
        if( biome.is( BiomeTags.IS_OCEAN ) ) {
            addSpawn( mobSpawns, DrowningCreeperEntity.SPECIES.entityType.get(),
                    Config.MAIN.ADDED_SPAWNS.drowningCreeperOceanWeight.get() );
            addSpawn( mobSpawns, BlueberrySlimeEntity.SPECIES.entityType.get(),
                    Config.MAIN.ADDED_SPAWNS.blueberrySlimeOceanWeight.get(), 4, 4 );
            addSpawn( mobSpawns, PirateSkeletonEntity.SPECIES.entityType.get(),
                    Config.MAIN.ADDED_SPAWNS.pirateSkeletonOceanWeight.get() );
        }
        else if( biome.is( BiomeTags.IS_RIVER ) ) {
            addSpawn( mobSpawns, DrowningCreeperEntity.SPECIES.entityType.get(),
                    Config.MAIN.ADDED_SPAWNS.drowningCreeperRiverWeight.get() );
            addSpawn( mobSpawns, BlueberrySlimeEntity.SPECIES.entityType.get(),
                    Config.MAIN.ADDED_SPAWNS.blueberrySlimeRiverWeight.get(), 4, 4 );
        }
        
        // Nether spawns
        // Soul sand valley and warped forest biomes have unique spawn setups
        else if( biome.is( Biomes.WARPED_FOREST ) ) {
            //            double charge = 1.0;
            //            double budget = 0.12;
            // Add warped variants here once they are created
        }
        else if( biome.is( Biomes.SOUL_SAND_VALLEY ) ) {
            double charge = 0.7;
            double budget = 0.15;
            
            addSpawn( mobSpawns, EntityType.WITHER_SKELETON,
                    Config.MAIN.ADDED_SPAWNS.witherSkeletonSoulSandValleyWeight.get(), 5, 5,
                    charge, budget );
            
            addSpawn( mobSpawns, DoomCreeperEntity.SPECIES.entityType.get(),
                    Config.MAIN.ADDED_SPAWNS.doomCreeperSoulSandValleyWeight.get(), 4, 4,
                    charge, budget );
            addSpawn( mobSpawns, SkeletonCreeperEntity.SPECIES.entityType.get(),
                    Config.MAIN.ADDED_SPAWNS.skeletonCreeperSoulSandValleyWeight.get(), 4, 4,
                    charge, budget );
            addSpawn( mobSpawns, UndeadWitchEntity.SPECIES.entityType.get(),
                    Config.MAIN.ADDED_SPAWNS.undeadWitchSoulSandValleyWeight.get(), 1, 1,
                    charge, budget );
        }
        // Remaining Nether biomes
        else if( biome.is( BiomeTags.IS_NETHER ) ) {
            addSpawn( mobSpawns, EntityType.WITHER_SKELETON,
                    Config.MAIN.ADDED_SPAWNS.witherSkeletonNetherWeight.get(), 5, 5 );
            
            if( biome.is( Biomes.BASALT_DELTAS ) ) {
                addSpawn( mobSpawns, EntityType.BLAZE,
                        Config.MAIN.ADDED_SPAWNS.blazeBasaltDeltasWeight.get(), 2, 3 );
            }
            else {
                addSpawn( mobSpawns, EntityType.BLAZE,
                        Config.MAIN.ADDED_SPAWNS.blazeNetherWeight.get(), 2, 3 );
            }
            
            //                    if( biome.is( Biomes.CRIMSON_FOREST ) ) {
            //                        // Add crimson variants here once they are created
            //                    }
            
            addSpawn( mobSpawns, FireCreeperEntity.SPECIES.entityType.get(),
                    Config.MAIN.ADDED_SPAWNS.fireCreeperNetherWeight.get(), 4, 4 );
            addSpawn( mobSpawns, FireZombieEntity.SPECIES.entityType.get(),
                    Config.MAIN.ADDED_SPAWNS.fireZombieNetherWeight.get(), 4, 4 );
            addSpawn( mobSpawns, FireSpiderEntity.SPECIES.entityType.get(),
                    Config.MAIN.ADDED_SPAWNS.fireSpiderNetherWeight.get(), 4, 4 );
        }
    }
    
    /** Modifies mob spawns in the biome based on existing spawns. */
    private static void modifySpawns( MobSpawnSettingsBuilder mobSpawns ) {
        // Multiplier-based spawns
        if( Config.MAIN.ADDED_SPAWNS.enableAddedSpawns.get() ) {
            addCopiedSpawns( mobSpawns, EntityType.SPIDER, EntityType.CAVE_SPIDER,
                    Config.MAIN.ADDED_SPAWNS.caveSpiderSpawnMultiplier.get() );
            addCopiedSpawns( mobSpawns, EntityType.ENDERMAN, EnderCreeperEntity.SPECIES.entityType.get(),
                    Config.MAIN.ADDED_SPAWNS.enderCreeperSpawnMultiplier.get() );
        }
        
        // Bestiary-based spawns
        if( Config.MAIN.GENERAL.useNaturalSpawner.get() ) {
            addBestiarySpawns( mobSpawns );
        }
    }
    
    /** Adds an entity type to the spawn list by copying another type's spawn entries. Does nothing if the entity type is already added. */
    private static void addCopiedSpawns( MobSpawnSettingsBuilder mobSpawns, EntityType<?> typeToCopy,
                                         EntityType<?> typeToAdd, double multi ) {
        if( multi <= 0.0 ) return;
        
        final List<MobSpawnSettings.SpawnerData> spawnersToCopy = new ArrayList<>();
        final List<MobSpawnSettings.SpawnerData> spawners = mobSpawns.getSpawner( MobCategory.MONSTER );
        
        for( MobSpawnSettings.SpawnerData spawner : spawners ) {
            if( spawner.type == typeToAdd && spawner.getWeight().asInt() > 0 ) return;
            if( spawner.type == typeToCopy && spawner.getWeight().asInt() > 0 ) spawnersToCopy.add( spawner );
        }
        
        // Currently, we simply copy pack size and spawn costs directly; configs can be added later for these, if needed
        if( !spawnersToCopy.isEmpty() ) {
            for( MobSpawnSettings.SpawnerData spawner : spawnersToCopy ) {
                addSpawn( mobSpawns, typeToAdd, (int) Math.max( 1, Mth.floor( spawner.getWeight().asInt() ) * multi ), spawner.minCount, spawner.maxCount );
            }
            
            final MobSpawnSettings.MobSpawnCost costsToCopy = mobSpawns.getCost( typeToCopy );
            if( costsToCopy != null ) {
                mobSpawns.addMobCharge( typeToAdd, costsToCopy.charge(), costsToCopy.energyBudget() );
            }
        }
    }
    
    /** Overhauls the biome's entire spawn list. Does nothing if no replaceable entity types are found. */
    private static void addBestiarySpawns( MobSpawnSettingsBuilder mobSpawns ) {
        final List<MobSpawnSettings.SpawnerData> spawners = mobSpawns.getSpawner( MobCategory.MONSTER );
        final List<MobSpawnSettings.SpawnerData> modifiedSpawners = new ArrayList<>( spawners.size() + MobFamily.getAllSpecies().size() );
        
        int factor = Config.MAIN.GENERAL.naturalSpawnFactor.getInt();
        boolean modifiedSpawns = false;
        for( MobSpawnSettings.SpawnerData spawner : spawners ) {
            MobFamily<?, ?> family = MobFamily.getReplacementFamily( spawner.type );
            if( family == null ) {
                // Non-replaceable entity type; multiply the weight to keep it at a similar spawn chance
                modifiedSpawners.add( new MobSpawnSettings.SpawnerData( spawner.type,
                        spawner.getWeight().asInt() * factor,
                        spawner.minCount, spawner.maxCount ) );
            }
            else {
                // Replaceable entity type
                MobSpawnSettings.MobSpawnCost costsToCopy = mobSpawns.getCost( spawner.type );
                boolean vanillaReplacement = Config.MAIN.GENERAL.masterVanillaReplacement.get() && family.config.GENERAL.vanillaReplacement.get();
                double specialVariantChance = family.config.GENERAL.specialVariantChance.base().getDouble();
                // Add all enabled special variants
                if( specialVariantChance > 0.0 ) {
                    int totalConfiguredWeight = 0;
                    for( IntField field : family.config.GENERAL.specialVariantWeights ) {
                        totalConfiguredWeight += field.getInt();
                    }
                    if( totalConfiguredWeight > 0 ) {
                        double rawVariantsWeight = spawner.getWeight().asInt() * factor * specialVariantChance;
                        MobFamily.Species<?>[] variants = family.variants;
                        for( int i = 0; i < variants.length; i++ ) {
                            int configuredWeight = family.config.GENERAL.specialVariantWeights.get( i ).getInt();
                            if( configuredWeight > 0 ) {
                                // This variant is enabled, do the thing
                                EntityType<?> speciesType = variants[i].entityType.get();
                                modifiedSpawners.add( new MobSpawnSettings.SpawnerData( speciesType,
                                        Mth.ceil( rawVariantsWeight * configuredWeight / totalConfiguredWeight ),
                                        spawner.minCount, spawner.maxCount ) );
                                if( costsToCopy != null ) {
                                    mobSpawns.addMobCharge( speciesType,
                                            costsToCopy.charge(), costsToCopy.energyBudget() );
                                }
                                modifiedSpawns = true;
                            }
                        }
                    }
                    else {
                        // All special variants are disabled :(
                        specialVariantChance = 0.0;
                    }
                }
                // Add the base mob
                if( specialVariantChance < 1.0 ) {
                    modifiedSpawners.add( new MobSpawnSettings.SpawnerData( vanillaReplacement ?
                            family.vanillaReplacement.entityType.get() : spawner.type,
                            Mth.ceil( spawner.getWeight().asInt() * factor * (1.0 - specialVariantChance) ),
                            spawner.minCount, spawner.maxCount ) );
                    if( vanillaReplacement ) {
                        if( costsToCopy != null ) {
                            mobSpawns.addMobCharge( family.vanillaReplacement.entityType.get(),
                                    costsToCopy.charge(), costsToCopy.energyBudget() );
                        }
                        modifiedSpawns = true;
                    }
                }
            }
        }
        // Apply actual spawn modifications; charge/budget has already been applied by this point, if needed
        if( modifiedSpawns ) {
            spawners.clear();
            spawners.addAll( modifiedSpawners );
        }
    }
    
    /** Adds a mob spawn to the biome with no pack size (i.e., individual spawn). */
    private static void addSpawn( MobSpawnSettingsBuilder mobSpawns, EntityType<?> entity, int weight ) {
        addSpawn( mobSpawns, entity, weight, 1, 1 );
    }
    
    /** Adds a mob spawn to the biome with a specified pack size. */
    private static void addSpawn( MobSpawnSettingsBuilder mobSpawns, EntityType<?> entity, int weight, int minCount, int maxCount ) {
        if( weight > 0 ) {
            mobSpawns.addSpawn( entity.getCategory(), new MobSpawnSettings.SpawnerData( entity, weight, minCount, maxCount ) );
        }
    }
    
    /**
     * Adds a mob spawn to the biome with a specified pack size and charge.
     * In vanilla, the charge/budget system is only used in the warped forest and soul sand valley biomes.
     */
    private static void addSpawn( MobSpawnSettingsBuilder mobSpawns, EntityType<?> entity, int weight, int minCount, int maxCount,
                                  double charge, double budget ) {
        if( weight > 0 ) {
            mobSpawns.addSpawn( entity.getCategory(), new MobSpawnSettings.SpawnerData( entity, weight, minCount, maxCount ) );
            mobSpawns.addMobCharge( entity, charge, budget );
        }
    }
}