package fathertoast.specialmobs.common.config.util.environment.position;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.RegistryGroupEnvironment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.List;

public class StructureGroupEnvironment extends RegistryGroupEnvironment<Structure<?>> {
    
    public StructureGroupEnvironment( Structure<?> biome ) { super( biome ); }
    
    public StructureGroupEnvironment( Structure<?> biome, boolean invert ) { super( biome, invert ); }
    
    public StructureGroupEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_STRUCTURE; }
    
    /** @return The registry used. */
    @Override
    public IForgeRegistry<Structure<?>> getRegistry() { return ForgeRegistries.STRUCTURE_FEATURES; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public final boolean matches( World world, @Nullable BlockPos pos ) {
        final StructureManager structureManager = pos != null && world instanceof ServerWorld ?
                ((ServerWorld) world).structureFeatureManager() : null;
        if( structureManager != null ) {
            final List<Structure<?>> entries = getRegistryEntries();
            for( Structure<?> entry : entries ) {
                if( structureManager.getStructureAt( pos, false, entry ).isValid() ) return !INVERT;
            }
        }
        return INVERT;
    }
}