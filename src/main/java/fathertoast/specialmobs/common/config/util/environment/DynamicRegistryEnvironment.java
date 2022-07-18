package fathertoast.specialmobs.common.config.util.environment;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.util.ConfigUtil;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

/**
 * Dynamic registries are contained in {@link net.minecraft.util.registry.DynamicRegistries}
 */
public abstract class DynamicRegistryEnvironment<T> extends AbstractEnvironment {
    /** The field containing this entry. We save a reference to help improve error/warning reports. */
    private final AbstractConfigField FIELD;
    
    /** If true, the condition is inverted. */
    protected final boolean INVERT;
    /** The registry key for this environment. */
    private final ResourceLocation REGISTRY_KEY;
    
    private T registryEntry;
    /** The value of ConfigUtil#DYNAMIC_REGISTRY_VERSION at the time of last poll. */
    private byte version = -1;
    
    public DynamicRegistryEnvironment( ResourceLocation regKey, boolean invert ) {
        FIELD = null;
        INVERT = invert;
        REGISTRY_KEY = regKey;
    }
    
    public DynamicRegistryEnvironment( AbstractConfigField field, String line ) {
        FIELD = field;
        INVERT = line.startsWith( "!" );
        REGISTRY_KEY = new ResourceLocation( INVERT ? line.substring( 1 ) : line );
    }
    
    /** @return The string value of this environment, as it would appear in a config file. */
    @Override
    public final String value() { return (INVERT ? "!" : "") + REGISTRY_KEY.toString(); }
    
    /** @return The registry used. */
    public abstract RegistryKey<Registry<T>> getRegistry();
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public final boolean matches( World world, @Nullable BlockPos pos ) {
        if( world instanceof ServerWorld )
            return matches( (ServerWorld) world, pos ); // These don't work on the client :(
        return INVERT;
    }
    
    /** @return Returns true if this environment matches the provided environment. */
    public abstract boolean matches( ServerWorld world, @Nullable BlockPos pos );
    
    /** @return The target registry object. */
    @Nullable
    public final T getRegistryEntry( ServerWorld world ) {
        if( version != ConfigUtil.DYNAMIC_REGISTRY_VERSION ) {
            version = ConfigUtil.DYNAMIC_REGISTRY_VERSION;
            
            final Registry<T> registry = world.getServer().registryAccess().registryOrThrow( getRegistry() );
            registryEntry = registry.get( REGISTRY_KEY );
            if( registryEntry == null ) {
                SpecialMobs.LOG.info( "Missing entry for {} \"{}\"! Not present in registry \"{}\". Missing entry: {}",
                        FIELD.getClass(), FIELD.getKey(), getRegistry().getRegistryName(), REGISTRY_KEY );
            }
        }
        return registryEntry;
    }
}