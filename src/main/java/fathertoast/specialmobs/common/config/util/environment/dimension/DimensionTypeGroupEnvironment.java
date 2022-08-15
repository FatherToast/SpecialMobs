package fathertoast.specialmobs.common.config.util.environment.dimension;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.DynamicRegistryGroupEnvironment;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class DimensionTypeGroupEnvironment extends DynamicRegistryGroupEnvironment<DimensionType> {
    
    public DimensionTypeGroupEnvironment( RegistryKey<DimensionType> dimType, boolean invert ) { this( dimType.location(), invert ); }
    
    public DimensionTypeGroupEnvironment( ResourceLocation regKey, boolean invert ) { super( regKey, invert ); }
    
    public DimensionTypeGroupEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_DIMENSION_TYPE; }
    
    /** @return The registry used. */
    @Override
    public RegistryKey<Registry<DimensionType>> getRegistry() { return Registry.DIMENSION_TYPE_REGISTRY; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public final boolean matches( ServerWorld world, @Nullable BlockPos pos ) {
        final DimensionType target = world.dimensionType();
        final List<DimensionType> entries = getRegistryEntries( world );
        for( DimensionType entry : entries ) {
            if( entry.equals( target ) ) return !INVERT;
        }
        return INVERT;
    }
}