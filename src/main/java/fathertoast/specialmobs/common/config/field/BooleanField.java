package fathertoast.specialmobs.common.config.field;

import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.core.SpecialMobs;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a config field with a boolean value.
 */
@SuppressWarnings( "unused" )
public class BooleanField extends AbstractConfigField {
    /** The default field value. */
    private final boolean valueDefault;
    
    /** The underlying field value. */
    private boolean value;
    
    /** Creates a new field. */
    public BooleanField( String key, boolean defaultValue, String... description ) {
        super( key, description );
        valueDefault = defaultValue;
    }
    
    /** @return Returns the config field's value. */
    public boolean get() { return value; }
    
    /** Adds info about the field type, format, and bounds to the end of a field's description. */
    public void appendFieldInfo( List<String> comment ) {
        comment.add( TomlHelper.fieldInfoValidValues( "Boolean", valueDefault, true, false ) );
    }
    
    /**
     * Loads this field's value from the given raw toml value. If anything goes wrong, correct it at the lowest level possible.
     * <p>
     * For example, a missing value should be set to the default, while an out-of-range value should be adjusted to the
     * nearest in-range value
     */
    @Override
    public void load( @Nullable Object raw ) {
        // Use a final local variable to make sure the value gets set exactly one time
        final boolean newValue;
        if( raw instanceof Boolean ) {
            // Parse the value
            newValue = (Boolean) raw;
        }
        else if( raw instanceof Number ) {
            // Convert the value
            final double rawValue = ((Number) raw).doubleValue();
            SpecialMobs.LOG.warn( "Value for {} \"{}\" is numerical! Converting value. Invalid value: {}",
                    getClass(), getKey(), raw );
            newValue = rawValue != 0.0; // 0 is false, anything else is true
        }
        else if( raw instanceof String ) {
            // Try unboxing the string to another primitive type
            SpecialMobs.LOG.info( "Unboxing string value for {} \"{}\" to a different primitive.",
                    getClass(), getKey() );
            load( TomlHelper.parseRaw( (String) raw ) );
            return;
        }
        else {
            // Value cannot be parsed to this field
            if( raw != null ) {
                SpecialMobs.LOG.warn( "Invalid value for {} \"{}\"! Falling back to default. Invalid value: {}",
                        getClass(), getKey(), raw );
            }
            newValue = valueDefault;
        }
        value = newValue;
    }
    
    /** @return The raw toml value that should be assigned to this field in the config file. */
    @Override
    public Object getRaw() { return value; }
}