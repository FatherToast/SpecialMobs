package fathertoast.specialmobs.common.biome.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMBiomeMods;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public record ReplaceModifier(ResourceLocation entityToReplace, ResourceLocation replacement) implements BiomeModifier {

    public static final Codec<ReplaceModifier> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("entityToReplace")
                    .forGetter(ReplaceModifier::entityToReplace),
            ResourceLocation.CODEC.fieldOf("replacement")
                    .forGetter(ReplaceModifier::replacement)
    ).apply(builder, ReplaceModifier::new));


    @SuppressWarnings("ConstantConditions")
    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if ( phase == Phase.MODIFY ) {
            for (MobCategory mobCategory : MobCategory.values()) {
                List<MobSpawnSettings.SpawnerData> spawnerDataList = builder.getMobSpawnSettings().getSpawner(mobCategory);

                for (MobSpawnSettings.SpawnerData spawner : spawnerDataList) {
                    EntityType<?> entityType = spawner.type;

                    if (ForgeRegistries.ENTITY_TYPES.containsValue(entityType)) {
                        if (ForgeRegistries.ENTITY_TYPES.getKey(entityType).equals(entityToReplace)) {
                            if (ForgeRegistries.ENTITY_TYPES.containsKey(replacement)) {

                                EntityType<?> replacementType = ForgeRegistries.ENTITY_TYPES.getValue(replacement);
                                builder.getMobSpawnSettings().addSpawn(entityType.getCategory(), new MobSpawnSettings
                                        .SpawnerData(replacementType, spawner.getWeight(), spawner.minCount, spawner.maxCount));
                                spawnerDataList.remove(spawner);
                            }
                            else {
                                SpecialMobs.LOG.error("A 'replace' biome modifier failed; the replacement entity type does not exist in the registry! To replace: {}, replacement: {}", entityToReplace, replacement);
                            }
                        }
                    }
                    else {
                        SpecialMobs.LOG.error("A 'replace' biome modifier failed; the to-replace entity type does not exist in the registry! To replace: {}, replacement: {}", entityToReplace, replacement);
                    }
                }
            }
        }
    }

    // RAAAAAAAUGH
    @Override
    public Codec<? extends BiomeModifier> codec() {
        return SMBiomeMods.REPLACE_MOD.get();
    }
}
