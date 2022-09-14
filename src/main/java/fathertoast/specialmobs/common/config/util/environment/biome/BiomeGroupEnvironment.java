package fathertoast.specialmobs.common.config.util.environment.biome;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.DynamicRegistryGroupEnvironment;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class BiomeGroupEnvironment extends DynamicRegistryGroupEnvironment<Biome> {
    
    public BiomeGroupEnvironment( RegistryKey<Biome> biome, boolean invert ) { this( biome.location(), invert ); }
    
    public BiomeGroupEnvironment( ResourceLocation regKey, boolean invert ) { super( regKey, invert ); }
    
    public BiomeGroupEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_BIOME; }
    
    /** @return The registry used. */
    @Override
    public RegistryKey<Registry<Biome>> getRegistry() { return Registry.BIOME_REGISTRY; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public final boolean matches( ServerWorld world, @Nullable BlockPos pos ) {
        final Biome target = pos == null ? null : world.getBiome( pos );
        if( target != null ) {
            final List<Biome> entries = getRegistryEntries( world );
            for( Biome entry : entries ) {
                if( entry.equals( target ) ) return !INVERT;
            }
        }
        return INVERT;
    }
}