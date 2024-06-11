package fathertoast.specialmobs.common.config.util.environment.biome;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.DynamicRegistryEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;

public class BiomeEnvironment extends DynamicRegistryEnvironment<Biome> {
    
    public BiomeEnvironment( ResourceKey<Biome> biome, boolean invert ) { super( biome.location(), invert ); }
    
    public BiomeEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_BIOME; }
    
    /** @return The registry used. */
    @Override
    public ResourceKey<Registry<Biome>> getRegistry() { return Registries.BIOME; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public boolean matches( ServerLevel level, @Nullable BlockPos pos ) {
        final Biome entry = getRegistryEntry( level );
        return (entry != null && pos != null && entry.equals( level.getBiome( pos ) )) != INVERT;
    }
}