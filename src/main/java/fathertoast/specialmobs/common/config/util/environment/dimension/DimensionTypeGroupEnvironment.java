package fathertoast.specialmobs.common.config.util.environment.dimension;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.DynamicRegistryGroupEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.List;

public class DimensionTypeGroupEnvironment extends DynamicRegistryGroupEnvironment<DimensionType> {
    
    public DimensionTypeGroupEnvironment( ResourceKey<DimensionType> dimType, boolean invert ) { this( dimType.location(), invert ); }
    
    public DimensionTypeGroupEnvironment( ResourceLocation regKey, boolean invert ) { super( regKey, invert ); }
    
    public DimensionTypeGroupEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_DIMENSION_TYPE; }
    
    /** @return The registry used. */
    @Override
    public ResourceKey<Registry<DimensionType>> getRegistry() { return Registry.DIMENSION_TYPE_REGISTRY; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public final boolean matches( ServerLevel level, @Nullable BlockPos pos ) {
        final DimensionType target = level.dimensionType();
        final List<DimensionType> entries = getRegistryEntries( level );
        for( DimensionType entry : entries ) {
            if( entry.equals( target ) ) return !INVERT;
        }
        return INVERT;
    }
}