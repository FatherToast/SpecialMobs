package fathertoast.specialmobs.common.biome.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fathertoast.specialmobs.common.core.register.SMBiomeMods;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.ForgeRegistries;

public record ConfigAddSpawn(ResourceLocation entityToAdd, HolderSet<Biome> biomes, int weight, int min, int max, boolean configOverride) implements BiomeModifier {

    public static final Codec<ConfigAddSpawn> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("entityToAdd")
                    .forGetter(ConfigAddSpawn::entityToAdd),
            Biome.LIST_CODEC.fieldOf("biomes")
                    .forGetter(ConfigAddSpawn::biomes),
            Codec.INT.fieldOf("weight")
                    .forGetter(ConfigAddSpawn::weight),
            Codec.INT.fieldOf("min")
                    .forGetter(ConfigAddSpawn::min),
            Codec.INT.fieldOf("max")
                    .forGetter(ConfigAddSpawn::max),
            Codec.BOOL.fieldOf("configOverride")
                    .forGetter(ConfigAddSpawn::configOverride)
    ).apply(builder, ConfigAddSpawn::new));


    @SuppressWarnings("ConstantConditions")
    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if ( phase == Phase.ADD && biomes.contains(biome) ) {
            if (ForgeRegistries.ENTITY_TYPES.containsKey(entityToAdd)) {
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityToAdd);

                if (configOverride) {

                }

                builder.getMobSpawnSettings().addSpawn(entityType.getCategory(), new MobSpawnSettings.SpawnerData(entityType, weight, min, max));
            }
        }
    }

    // RAAAAAAAUGH
    @Override
    public Codec<? extends BiomeModifier> codec() {
        return SMBiomeMods.CONFIG_ADD_SPAWN.get();
    }
}
