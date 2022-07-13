package fathertoast.specialmobs.common.config.field;

import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.config.util.RegistryEntryList;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a config field with a registry entry list value.
 * <p>
 * See also: {@link net.minecraftforge.registries.ForgeRegistries}
 */
@SuppressWarnings( "unused" )
public class RegistryEntryListField<T extends IForgeRegistryEntry<T>> extends GenericField<RegistryEntryList<T>> {
    
    /** Provides a detailed description of how to use registry entry lists. Recommended putting at the top of any file using registry entry lists. */
    public static List<String> verboseDescription() {
        List<String> comment = new ArrayList<>();
        comment.add( "Registry Entry List fields: General format = [ \"namespace:entry_name\", ... ]" );
        comment.add( "  Registry entry lists are arrays of registry keys. Many things in the game, such as blocks or potions, are defined" );
        comment.add( "    by their registry key within a registry. For example, all items are registered in the \"minecraft:item\" registry." );
        comment.add( "  An asterisk '*' can be used to match multiple registry keys. For example, 'minecraft:*' will match all vanilla entries" );
        comment.add( "    within the registry entry list's target registry." );
        return comment;
    }
    
    /** Creates a new field. */
    public RegistryEntryListField( String key, RegistryEntryList<T> defaultValue, String... description ) {
        super( key, defaultValue, description );
    }
    
    /** Adds info about the field type, format, and bounds to the end of a field's description. */
    public void appendFieldInfo( List<String> comment ) {
        comment.add( TomlHelper.fieldInfoFormat( "\"" + SpecialMobs.toString( valueDefault.getRegistry().getRegistryName() ) +
                "\" Registry List", valueDefault, "[ \"namespace:entry_name\", ... ]" ) );
    }
    
    /**
     * Loads this field's value from the given raw toml value. If anything goes wrong, correct it at the lowest level possible.
     * <p>
     * For example, a missing value should be set to the default, while an out-of-range value should be adjusted to the
     * nearest in-range value
     */
    @Override
    public void load( @Nullable Object raw ) {
        if( raw == null ) {
            value = valueDefault;
            return;
        }
        // All the actual loading is done through the objects
        value = new RegistryEntryList<>( this, valueDefault.getRegistry(), TomlHelper.parseStringList( raw ) );
    }
    
    /** @return The registry this list draws from. */
    public IForgeRegistry<T> getRegistry() { return value.getRegistry(); }
    
    /** @return The entries in this list. */
    public Set<T> getEntries() { return value.getEntries(); }
    
    /** @return Returns true if there are no entries in this list. */
    public boolean isEmpty() { return value.isEmpty(); }
    
    /** @return Returns true if the entry is contained in this list. */
    public boolean contains( T entry ) { return value.contains( entry ); }
}