package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.Set;

/**
 * A list of entries used to match registry entries. Can safely be loaded before its target registry is loaded, but
 * is not able to error check as well.
 * <p>
 * See also: {@link net.minecraftforge.registries.ForgeRegistries}
 */
@SuppressWarnings( "unused" )
public class LazyRegistryEntryList<T extends IForgeRegistryEntry<T>> extends RegistryEntryList<T> {
    /** The field containing this list. We save a reference to help improve error/warning reports. */
    private final AbstractConfigField FIELD;
    /** True if the underlying set has been populated from the print list. */
    private boolean populated;
    
    /**
     * Create a new registry entry list from an array of entries. Used for creating default configs.
     * <p>
     * This method of creation can not take advantage of the * notation.
     */
    @SafeVarargs
    public LazyRegistryEntryList( IForgeRegistry<T> registry, T... entries ) {
        super( registry, entries );
        FIELD = null;
        populated = true;
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
                    SpecialMobs.LOG.warn( "Invalid entry for {} \"{}\"! Invalid entry: {}",
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
    public boolean contains( T entry ) {
        populateEntries();
        return super.contains( entry );
    }
}