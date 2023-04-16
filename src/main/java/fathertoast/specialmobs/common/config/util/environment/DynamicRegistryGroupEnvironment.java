package fathertoast.specialmobs.common.config.util.environment;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.util.ConfigUtil;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dynamic/builtin registries are contained in {@link net.minecraft.data.BuiltinRegistries}
 */
public abstract class DynamicRegistryGroupEnvironment<T> extends AbstractEnvironment {
    /** The field containing this entry. We save a reference to help improve error/warning reports. */
    private final AbstractConfigField FIELD;
    
    /** If true, the condition is inverted. */
    protected final boolean INVERT;
    /** The namespace for this environment. */
    private final String NAMESPACE;
    
    private List<T> registryEntries;
    /** The value of ConfigUtil#DYNAMIC_REGISTRY_VERSION at the time of last poll. */
    private byte version = -1;
    
    public DynamicRegistryGroupEnvironment( ResourceLocation regKey, boolean invert ) {
        FIELD = null;
        INVERT = invert;
        NAMESPACE = regKey.toString();
    }
    
    public DynamicRegistryGroupEnvironment( AbstractConfigField field, String line ) {
        FIELD = field;
        INVERT = line.startsWith( "!" );
        NAMESPACE = line.substring( INVERT ? 1 : 0, line.length() - 1 );
    }
    
    /** @return The string value of this environment, as it would appear in a config file. */
    @Override
    public final String value() { return (INVERT ? "!" : "") + NAMESPACE + "*"; }
    
    /** @return The registry used. */
    public abstract ResourceKey<Registry<T>> getRegistry();
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public final boolean matches( Level level, @Nullable BlockPos pos ) {
        if( level instanceof ServerLevel serverLevel )
            return matches( serverLevel, pos ); // These don't work on the client :(
        return INVERT;
    }
    
    /** @return Returns true if this environment matches the provided environment. */
    public abstract boolean matches( ServerLevel level, @Nullable BlockPos pos );
    
    /** @return The target registry object. */
    protected final List<T> getRegistryEntries( ServerLevel level ) {
        if( version != ConfigUtil.DYNAMIC_REGISTRY_VERSION ) {
            version = ConfigUtil.DYNAMIC_REGISTRY_VERSION;
            
            registryEntries = new ArrayList<>();
            final Registry<T> registry = level.getServer().registryAccess().registryOrThrow( getRegistry() );
            for( ResourceLocation regKey : registry.keySet() ) {
                if( regKey.toString().startsWith( NAMESPACE ) ) {
                    final T entry = registry.get( regKey );
                    if( entry != null ) registryEntries.add( entry );
                }
            }
            if( registryEntries.isEmpty() ) {
                SpecialMobs.LOG.info( "Namespace entry for {} \"{}\" did not match anything in registry \"{}\"! Questionable entry: {}",
                        FIELD == null ? "DEFAULT" : FIELD.getClass(), FIELD == null ? "DEFAULT" : FIELD.getKey(), getRegistry().location(), NAMESPACE );
            }
            registryEntries = Collections.unmodifiableList( registryEntries );
        }
        return registryEntries;
    }
}