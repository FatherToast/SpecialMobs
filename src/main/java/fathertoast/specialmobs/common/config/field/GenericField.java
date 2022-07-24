package fathertoast.specialmobs.common.config.field;

import javax.annotation.Nullable;

/**
 * Represents a config field with an object value.
 * <p>
 * Note that it is important the field type has a non-default implementation of equals(), as this is how the config spec
 * determines when it does not need to rewrite to disk.
 *
 * @see Object#equals(Object)
 */
public abstract class GenericField<T> extends AbstractConfigField {
    /** The default field value. */
    protected final T valueDefault;
    
    /** The underlying field value. */
    protected T value;
    
    /** Creates a new field. */
    public GenericField( String key, T defaultValue, @Nullable String... description ) {
        super( key, description );
        valueDefault = defaultValue;
    }
    
    /** @return Returns the config field's value. */
    public T get() { return value; }
    
    /** @return The raw toml value that should be assigned to this field in the config file. */
    @Override
    public Object getRaw() { return value; }
}