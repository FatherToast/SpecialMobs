package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Set;

/**
 * A list of entries used to match registry entries. Can safely be loaded before its target registry is loaded, but
 * is not able to error check as well.
 * <p>
 * See also: {@link net.minecraftforge.registries.ForgeRegistries}
 */
public class LazyRegistryEntryList<T> extends RegistryEntryList<T> {
    /** The field containing this list. We save a reference to help improve error/warning reports. */
    private final AbstractConfigField FIELD;
    /** True if the underlying set has been populated from the print list. */
    private boolean populated;
    
    /**
     * Create a new registry entry list from an array of entries. Used for creating default configs.
     * <p>
     * This method of creation is less safe, but can take advantage of the regular vanilla entries, deferred entries,
     * resource locations, and raw strings.
     * <p>
     * @param vanilla If true, assume all entries are from vanilla (already loaded)
     */
    @SuppressWarnings("unchecked")
    public LazyRegistryEntryList( IForgeRegistry<T> registry, boolean vanilla, Object... entries ) {
        super( registry );
        FIELD = null;

        if (vanilla) {
            populated = true;
        }
        else {
            Class<T> type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                    .getActualTypeArguments()[0];

            for (Object entry : entries) {
                if (type.isInstance(entry) && registry.containsValue((T) entry)) {
                    final ResourceLocation regKey = registry.getKey((T) entry);
                    if (regKey == null) {
                        throw new IllegalArgumentException("Invalid default lazy registry list entry! " + entry);
                    }
                    PRINT_LIST.add(regKey.toString());
                }
                else if (entry instanceof RegistryObject) {
                    PRINT_LIST.add(((RegistryObject<?>) entry).getId().toString());
                }
                else if (entry instanceof ResourceLocation) {
                    PRINT_LIST.add(entry.toString());
                }
                else if (entry instanceof String) {
                    PRINT_LIST.add((String) entry);
                }
                else {
                    throw new IllegalArgumentException("Invalid default lazy registry list entry! " + entry);
                }
            }
        }
    }
    
    /**
     * Create a new registry entry list from a list of registry key strings.
     */
    public LazyRegistryEntryList( AbstractConfigField field, IForgeRegistry<T> registry, List<String> entries ) {
        super( registry );
        FIELD = field;
        for( String line : entries ) {
            if( line.endsWith( "*" ) ) {
                PRINT_LIST.add( line );
            }
            else {
                PRINT_LIST.add( new ResourceLocation( line ).toString() );
            }
        }
    }
    
    /** Fills out the registry entry set with the actual registry entries. */
    private void populateEntries() {
        if( populated ) return;
        populated = true;
        
        for( String line : PRINT_LIST ) {
            if( line.endsWith( "*" ) ) {
                // Handle special case; add all entries in namespace
                if( !mergeFromNamespace( line.substring( 0, line.length() - 1 ) ) ) {
                    SpecialMobs.LOG.warn( "Namespace entry for {} \"{}\" did not match anything! Questionable entry: {}",
                            FIELD.getClass(), FIELD.getKey(), line );
                }
            }
            else {
                // Add a single registry entry
                final ResourceLocation regKey = new ResourceLocation( line );
                if( !mergeFrom( regKey ) ) {
                    SpecialMobs.LOG.warn( "Invalid or duplicate entry for {} \"{}\"! Invalid entry: {}",
                            FIELD.getClass(), FIELD.getKey(), line );
                }
            }
        }
    }
    
    /** @return The entries in this list. */
    @Override
    public Set<T> getEntries() {
        populateEntries();
        return super.getEntries();
    }
    
    /** @return Returns true if there are no entries in this list. */
    @Override
    public boolean isEmpty() { return populated ? super.isEmpty() : PRINT_LIST.isEmpty(); }
    
    /** @return Returns true if the entry is contained in this list. */
    @Override
    public boolean contains( @Nullable T entry ) {
        populateEntries();
        return super.contains( entry );
    }
}