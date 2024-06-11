package fathertoast.specialmobs.common.config.util.environment.position;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.DynamicRegistryEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;

import javax.annotation.Nullable;

public class StructureEnvironment extends DynamicRegistryEnvironment<Structure> {
    
    public StructureEnvironment( ResourceKey<Structure> structure, boolean invert ) {
        super( structure.location(), invert );
    }
    
    public StructureEnvironment( AbstractConfigField field, String line ) { super( field, line ); }

    @Override
    public ResourceKey<Registry<Structure>> getRegistry() {
        return Registries.STRUCTURE;
    }

    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_STRUCTURE; }

    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public boolean matches( ServerLevel level, @Nullable BlockPos pos ) {
        final Structure entry = getRegistryEntry(level);
        return (entry != null && pos != null &&
                level.structureManager().getStructureAt( pos, entry ).isValid()) != INVERT;
    }
}