package fathertoast.specialmobs.common.config.util.environment;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registries are contained in {@link net.minecraftforge.registries.ForgeRegistries}
 */
public abstract class RegistryGroupEnvironment<T> extends AbstractEnvironment {
    /** The field containing this entry. We save a reference to help improve error/warning reports. */
    private final AbstractConfigField FIELD;
    
    /** If true, the condition is inverted. */
    protected final boolean INVERT;
    /** The namespace for this environment. */
    private final String NAMESPACE;
    
    private List<T> registryEntries;
    
    public RegistryGroupEnvironment( T regEntry, IForgeRegistry<T> registry, boolean invert ) {
        //noinspection ConstantConditions
        this( registry.getKey(regEntry), invert );
    }
    
    public RegistryGroupEnvironment( ResourceLocation regKey, boolean invert ) {
        FIELD = null;
        INVERT = invert;
        NAMESPACE = regKey.toString();
    }
    
    public RegistryGroupEnvironment( AbstractConfigField field, String line ) {
        FIELD = field;
        INVERT = line.startsWith( "!" );
        NAMESPACE = line.substring( INVERT ? 1 : 0, line.length() - 1 );
    }
    
    /** @return The string value of this environment, as it would appear in a config file. */
    @Override
    public final String value() { return (INVERT ? "!" : "") + NAMESPACE + "*"; }
    
    /** @return The registry used. */
    public abstract IForgeRegistry<T> getRegistry();
    
    /** @return The registry entries. */
    protected final List<T> getRegistryEntries() {
        if( registryEntries == null ) {
            registryEntries = new ArrayList<>();
            for( ResourceLocation regKey : getRegistry().getKeys() ) {
                if( regKey.toString().startsWith( NAMESPACE ) ) {
                    final T entry = getRegistry().getValue( regKey );
                    if( entry != null ) registryEntries.add( entry );
                }
            }
            if( registryEntries.isEmpty() ) {
                SpecialMobs.LOG.warn( "Namespace entry for {} \"{}\" did not match anything in registry \"{}\"! Questionable entry: {}",
                        FIELD == null ? "DEFAULT" : FIELD.getClass(), FIELD == null ? "DEFAULT" : FIELD.getKey(), getRegistry().getRegistryName(), NAMESPACE );
            }
            registryEntries = Collections.unmodifiableList( registryEntries );
        }
        return registryEntries;
    }
}