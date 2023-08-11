package fathertoast.specialmobs.common.core.register;

import com.mojang.serialization.Codec;
import fathertoast.specialmobs.common.biome.modifier.ConfigAddSpawn;
import fathertoast.specialmobs.common.biome.modifier.ReplaceModifier;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SMBiomeMods {

    /** The deferred register for this mod's biome modifiers. */
    public static final DeferredRegister<Codec<? extends BiomeModifier>> REGISTRY = DeferredRegister.create( ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, SpecialMobs.MOD_ID );


    public static final RegistryObject<Codec<ReplaceModifier>> REPLACE_SPAWN = REGISTRY.register("replace_spawn", () -> ReplaceModifier.CODEC);
    public static final RegistryObject<Codec<ConfigAddSpawn>> CONFIG_ADD_SPAWN = REGISTRY.register("config_add_spawn", () -> ConfigAddSpawn.CODEC);
}

