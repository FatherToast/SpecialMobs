package fathertoast.specialmobs.common.config.util.environment.biome;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.DynamicRegistryGroupEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;
import java.util.List;

public class BiomeGroupEnvironment extends DynamicRegistryGroupEnvironment<Biome> {
    
    public BiomeGroupEnvironment( ResourceKey<Biome> biome, boolean invert ) { this( biome.location(), invert ); }
    
    public BiomeGroupEnvironment(ResourceLocation regKey, boolean invert ) { super( regKey, invert ); }
    
    public BiomeGroupEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_BIOME; }
    
    /** @return The registry used. */
    @Override
    public ResourceKey<Registry<Biome>> getRegistry() { return Registries.BIOME; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public final boolean matches( ServerLevel level, @Nullable BlockPos pos ) {
        final Biome target = pos == null ? null : level.getBiome( pos ).get();
        if( target != null ) {
            final List<Biome> entries = getRegistryEntries( level );
            for( Biome entry : entries ) {
                if( entry.equals( target ) ) return !INVERT;
            }
        }
        return INVERT;
    }
}