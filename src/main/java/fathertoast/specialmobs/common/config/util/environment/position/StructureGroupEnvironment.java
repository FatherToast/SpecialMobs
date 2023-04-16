package fathertoast.specialmobs.common.config.util.environment.position;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.DynamicRegistryGroupEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;

import javax.annotation.Nullable;
import java.util.List;

public class StructureGroupEnvironment extends DynamicRegistryGroupEnvironment<Structure> {
    
    public StructureGroupEnvironment( ResourceKey<Structure> structure, boolean invert ) { super( structure.location(), invert ); }
    
    public StructureGroupEnvironment( ResourceLocation regKey, boolean invert ) { super( regKey, invert ); }
    
    public StructureGroupEnvironment( AbstractConfigField field, String line ) { super( field, line ); }

    @Override
    public ResourceKey<Registry<Structure>> getRegistry() {
        return Registry.STRUCTURE_REGISTRY;
    }

    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_STRUCTURE; }

    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public final boolean matches( ServerLevel level, @Nullable BlockPos pos ) {
        final StructureManager structureManager = pos != null ?
                level.structureManager() : null;
        if( structureManager != null ) {
            final List<Structure> entries = getRegistryEntries(level);
            for( Structure entry : entries ) {
                if( structureManager.getStructureAt( pos, entry ).isValid() ) return !INVERT;
            }
        }
        return INVERT;
    }
}