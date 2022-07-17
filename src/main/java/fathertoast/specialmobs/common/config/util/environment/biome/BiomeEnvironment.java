package fathertoast.specialmobs.common.config.util.environment.biome;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.DynamicRegistryEnvironment;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public class BiomeEnvironment extends DynamicRegistryEnvironment<Biome> {
    
    public BiomeEnvironment( RegistryKey<Biome> biome ) { super( biome.getRegistryName() ); }
    
    public BiomeEnvironment( RegistryKey<Biome> biome, boolean invert ) { super( biome.getRegistryName(), invert ); }
    
    public BiomeEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_BIOME; }
    
    /** @return The registry used. */
    @Override
    public RegistryKey<Registry<Biome>> getRegistry() { return Registry.BIOME_REGISTRY; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public boolean matches( ServerWorld world, @Nullable BlockPos pos ) {
        final Biome entry = getRegistryEntry( world );
        return (entry != null && pos != null && entry.equals( world.getBiome( pos ) )) != INVERT;
    }
}