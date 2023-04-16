package fathertoast.specialmobs.common.config.field;

import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.config.util.LazyRegistryEntryList;
import fathertoast.specialmobs.common.config.util.RegistryEntryList;

import javax.annotation.Nullable;

/**
 * Represents a config field with a lazy registry entry list value. The provided default value can be a regular registry
 * entry list; if you use the varargs constructor they will be functionally identical anyway.
 * <p>
 * See also: {@link net.minecraftforge.registries.ForgeRegistries}
 */
@SuppressWarnings( "unused" )
public class LazyRegistryEntryListField<T> extends RegistryEntryListField<T> {
    
    /** Creates a new field. */
    public LazyRegistryEntryListField( String key, RegistryEntryList<T> defaultValue, @Nullable String... description ) {
        super( key, defaultValue, description );
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
        value = new LazyRegistryEntryList<>( this, valueDefault.getRegistry(), TomlHelper.parseStringList( raw ) );
    }
}