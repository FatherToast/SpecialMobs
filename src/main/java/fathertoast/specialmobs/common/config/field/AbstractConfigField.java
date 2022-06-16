package fathertoast.specialmobs.common.config.field;

import com.electronwill.nightconfig.core.io.CharacterOutput;
import fathertoast.specialmobs.common.config.file.ToastTomlWriter;
import fathertoast.specialmobs.common.config.file.TomlHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single key-value mapping in a config.
 */
public abstract class AbstractConfigField {
    /** The base key prefix to use for all fields, based on the currently loading config category. */
    public static String loadingCategory;
    
    /** @see #getKey() */
    private final String KEY;
    /** @see #getComment() */
    private List<String> COMMENT;
    
    /**
     * Creates a new field with the supplied key and description.
     * If the description is null, it will cancel the entire comment, including the automatic field info text.
     */
    protected AbstractConfigField( String key, String... description ) {
        this( loadingCategory + key, description == null ? null : TomlHelper.newComment( description ) );
    }
    
    /**
     * Creates a new field with the supplied key and comment. This method is only used for very special circumstances.
     * If the comment is null, it will cancel the entire comment, including the automatic field info text.
     */
    AbstractConfigField( String key, List<String> comment ) {
        KEY = key;
        COMMENT = comment;
    }
    
    /** @return The unique config key that maps to this field in the config file. */
    public final String getKey() { return KEY; }
    
    /** @return A list of single-line comments to be placed directly above this field in the config file. */
    @Nullable
    public final List<String> getComment() { return COMMENT; }
    
    /** Adds procedural information to the comment and then makes it unmodifiable. */
    public final void finalizeComment() {
        if( COMMENT == null ) return;
        appendFieldInfo( COMMENT );
        ((ArrayList<String>) COMMENT).trimToSize();
        COMMENT = Collections.unmodifiableList( COMMENT );
    }
    
    /** Adds info about the field type, format, and bounds to the end of a field's description. */
    public abstract void appendFieldInfo( List<String> comment );
    
    /**
     * Loads this field's value from the given raw toml value. If anything goes wrong, correct it at the lowest level possible.
     * <p>
     * For example, a missing value should be set to the default, while an out-of-range value should be adjusted to the
     * nearest in-range value
     */
    public abstract void load( @Nullable Object raw );
    
    /** @return The raw toml value that should be assigned to this field in the config file. */
    public abstract Object getRaw();
    
    /** Writes this field's value to file. */
    public void writeValue( ToastTomlWriter writer, CharacterOutput output ) {
        Object raw = getRaw();
        if( raw instanceof IStringArray ) {
            writer.writeStringArray( ((IStringArray) raw).toStringList(), output );
        }
        else {
            writer.writeLine( TomlHelper.toLiteral( raw ), output );
        }
    }
}