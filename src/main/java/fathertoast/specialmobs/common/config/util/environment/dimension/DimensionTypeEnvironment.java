package fathertoast.specialmobs.common.config.util.environment.dimension;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.DynamicRegistryEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;

import javax.annotation.Nullable;

public class DimensionTypeEnvironment extends DynamicRegistryEnvironment<DimensionType> {
    
    public DimensionTypeEnvironment( ResourceKey<DimensionType> dimType, boolean invert ) { super( dimType.location(), invert ); }
    
    public DimensionTypeEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_DIMENSION_TYPE; }
    
    /** @return The registry used. */
    @Override
    public ResourceKey<Registry<DimensionType>> getRegistry() { return Registries.DIMENSION_TYPE; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public boolean matches( ServerLevel level, @Nullable BlockPos pos ) {
        final DimensionType entry = getRegistryEntry( level );
        return (entry != null && entry.equals( level.dimensionType() )) != INVERT;
    }
}