package fathertoast.specialmobs.common.biome.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.core.register.SMBiomeMods;
import fathertoast.specialmobs.common.entity.creeper.DrowningCreeperEntity;
import fathertoast.specialmobs.common.entity.creeper.EnderCreeperEntity;
import fathertoast.specialmobs.common.entity.creeper.FireCreeperEntity;
import fathertoast.specialmobs.common.entity.slime.BlueberrySlimeEntity;
import fathertoast.specialmobs.common.entity.spider.FireSpiderEntity;
import fathertoast.specialmobs.common.entity.zombie.FireZombieEntity;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This modifier is responsible for all the natural spawning of Special Mobs monsters.
 * There should only ever be one json using this modifier, seen as it is pretty much just
 * a workaround now that BiomeLoadEvent is no more.
 */
public record NaturalAddSpawnModifier(String comment) implements BiomeModifier {

    public static final Codec<NaturalAddSpawnModifier> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("_comment").forGetter(NaturalAddSpawnModifier::comment))
            .apply(builder, NaturalAddSpawnModifier::new));


    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if( !Config.MAIN.GENERAL.enableNaturalSpawning.get() ) return;

        if ( phase == Phase.ADD ) {
            addCopiedSpawns( builder );
            addBiomeCategorySpawns( builder.getMobSpawnSettings(), biome, ForgeRegistries.BIOMES.getKey(biome.value()) );
        }
    }

    /** Adds enabled spawn-copier mobs to the spawn list. */
    private static void addCopiedSpawns( ModifiableBiomeInfo.BiomeInfo.Builder builder ) {
        addCopiedSpawns( builder, EntityType.SPIDER, EntityType.CAVE_SPIDER,
                Config.MAIN.NATURAL_SPAWNING.caveSpiderSpawnMultiplier.get() );

        addCopiedSpawns( builder, EntityType.ENDERMAN, EnderCreeperEntity.SPECIES.entityType.get(),
                Config.MAIN.NATURAL_SPAWNING.enderCreeperSpawnMultiplier.get() );
    }



    /** Adds an entity type to the spawn list by copying another type's spawn entries. Does nothing if the entity type is already added. */
    private static void addCopiedSpawns( ModifiableBiomeInfo.BiomeInfo.Builder builder, EntityType<?> typeToCopy, EntityType<?> typeToAdd, double multi ) {
        if( multi <= 0.0 ) return;

        final List<MobSpawnSettings.SpawnerData> spawnersToCopy = new ArrayList<>();
        final List<MobSpawnSettings.SpawnerData> spawners = builder.getMobSpawnSettings().getSpawner( MobCategory.MONSTER );

        for( MobSpawnSettings.SpawnerData spawner : spawners ) {
            if( spawner.type == typeToAdd && spawner.getWeight().asInt() > 0 ) return;
            if( spawner.type == typeToCopy && spawner.getWeight().asInt() > 0 ) spawnersToCopy.add( spawner );
        }

        // Currently, we simply copy pack size and spawn costs directly; configs can be added later for these, if needed
        if( !spawnersToCopy.isEmpty() ) {
            for( MobSpawnSettings.SpawnerData spawner : spawnersToCopy ) {
                addSpawn( builder.getMobSpawnSettings(), typeToAdd, (int) Math.max( 1, Mth.floor( spawner.getWeight().asInt()) * multi ), spawner.minCount, spawner.maxCount );
            }

            final MobSpawnSettings.MobSpawnCost costsToCopy = builder.getMobSpawnSettings().getCost( typeToCopy );
            if( costsToCopy != null ) {
                builder.getMobSpawnSettings().addMobCharge( typeToAdd, costsToCopy.charge(), costsToCopy.energyBudget() );
            }
        }
    }


    /** Adds enabled biome-category-based mobs to the spawn list. */
    private static void addBiomeCategorySpawns( MobSpawnSettings.Builder builder, Holder<Biome> holder, @Nullable ResourceLocation name ) {
        if ( holder.is( BiomeTags.IS_OCEAN ) ) {
            addSpawn( builder, DrowningCreeperEntity.SPECIES.entityType.get(),
                    Config.MAIN.NATURAL_SPAWNING.drowningCreeperOceanWeight.get() );
            addSpawn( builder, BlueberrySlimeEntity.SPECIES.entityType.get(),
                    Config.MAIN.NATURAL_SPAWNING.blueberrySlimeOceanWeight.get() );
        }
        else if ( holder.is( BiomeTags.IS_RIVER ) ) {
            addSpawn( builder, DrowningCreeperEntity.SPECIES.entityType.get(),
                    Config.MAIN.NATURAL_SPAWNING.drowningCreeperRiverWeight.get() );
            addSpawn( builder, BlueberrySlimeEntity.SPECIES.entityType.get(),
                    Config.MAIN.NATURAL_SPAWNING.blueberrySlimeRiverWeight.get() );
        }
        else if ( holder.is( BiomeTags.IS_NETHER ) ) {
            addNetherSpawns( builder, name );
        }
    }

    /** Adds enabled extra nether mobs to the spawn list. */
    private static void addNetherSpawns( MobSpawnSettings.Builder builder, @Nullable ResourceLocation name ) {
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
    private static boolean isBiome( @Nullable ResourceLocation name, ResourceKey<Biome> biome ) {
        return biome.location().equals( name );
    }

    private static void addSpawn( MobSpawnSettings.Builder builder, EntityType<?> entity, int weight ) {
        addSpawn( builder, entity, weight, 1, 1 );
    }

    private static void addSpawn( MobSpawnSettings.Builder builder, EntityType<?> entity, int weight, int minCount, int maxCount ) {
        if( weight > 0 ) {
            builder.addSpawn( entity.getCategory(), new MobSpawnSettings.SpawnerData( entity, weight, minCount, maxCount ) );
        }
    }

    private static void addSpawn( MobSpawnSettings.Builder builder, EntityType<?> entity, int weight, int minCount, int maxCount,
                                  double charge, double budget ) {
        if( weight > 0 ) {
            builder.addSpawn( entity.getCategory(), new MobSpawnSettings.SpawnerData( entity, weight, minCount, maxCount ) );
            builder.addMobCharge( entity, charge, budget );
        }
    }

    // RAAAAAAAUGH
    @Override
    public Codec<? extends BiomeModifier> codec() {
        return SMBiomeMods.NATURAL_ADD_SPAWN.get();
    }
}
