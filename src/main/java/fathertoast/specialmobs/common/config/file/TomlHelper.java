package fathertoast.specialmobs.common.config.file;

import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.core.utils.StringUtils;
import fathertoast.specialmobs.common.config.field.DoubleField;
import fathertoast.specialmobs.common.config.field.IntField;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings( "unused" )
public final class TomlHelper {
    private TomlHelper() { } // This is a static access only class that cannot be instantiated
    
    /** Attempts to convert a toml literal to a string list. May or may not be accurate. */
    public static List<String> parseStringList( Object value ) {
        final List<String> list = new ArrayList<>();
        if( value instanceof List ) {
            // Get all values from the list
            for( Object entry : (List<?>) value ) {
                if( entry != null ) {
                    list.add( entry.toString() );
                }
            }
        }
        else {
            // Read non-list as a single item list
            list.add( toLiteral( value ) );
        }
        return list;
    }
    
    /** Attempts to convert a string value to a raw non-string toml literal. May or may not be accurate. */
    public static Object parseRaw( @Nullable String value ) {
        // Note: It is very important here that the returned value is NOT a string
        
        if( value != null && !"".equals( value ) ) {
            // Try to parse as a numerical value
            try {
                return Double.parseDouble( value );
            }
            catch( NumberFormatException ex ) {
                // This is okay; string was not a number
            }
            // Try to parse as a boolean
            if( Boolean.TRUE.toString().equalsIgnoreCase( value ) ) {
                return Boolean.TRUE;
            }
            else if( Boolean.FALSE.toString().equalsIgnoreCase( value ) ) {
                return Boolean.FALSE;
            }
        }
        // Null or failed to parse string
        return NullObject.NULL_OBJECT;
    }
    
    /** Attempts to convert an object to a toml literal. May or may not be accurate. */
    public static String toLiteral( @Nullable Object value ) {
        if( value == null ) {
            return "";
        }
        else if( value instanceof Enum<?> ) {
            return "\"" + ((Enum<?>) value).name().toLowerCase() + "\"";
        }
        else if( value instanceof String ) {
            return "\"" + value + "\"";
        }
        // Infinite values not supported
        //else if( value instanceof Double && ((Double) value).isInfinite() ) {
        //    // Toml infinite literals do not match java
        //    return (Double) value > 0.0 ? "Inf" : "-Inf";
        //}
        else {
            return value.toString();
        }
    }
    
    /** Attempts to convert an object array to a toml literal. May or may not be accurate. */
    public static String toLiteral( @Nullable Object... values ) {
        if( values == null ) {
            return "";
        }
        if( values.length < 1 ) {
            return "[]";
        }
        else {
            return "[ " + literalList( values ) + " ]";
        }
    }
    
    /** Attempts to convert an object list to a list of toml literals. May or may not be accurate. */
    public static String literalList( Object... list ) { return literalList( Arrays.asList( list ) ); }
    
    /** Attempts to convert an object list to a list of toml literals. May or may not be accurate. */
    public static String literalList( @Nullable List<Object> list ) {
        if( list == null || list.isEmpty() ) return "";
        StringBuilder literals = new StringBuilder();
        for( Object obj : list ) {
            literals.append( toLiteral( obj ) ).append( ", " );
        }
        literals.delete( literals.length() - 2, literals.length() );
        return literals.toString();
    }
    
    /** @return The default field info for a field with a value format/structure. */
    public static String fieldInfoFormat( String typeName, Object defaultValue, String format ) {
        return String.format( "<%s> Format: %s, Default: %s", typeName, format, toLiteral( defaultValue ) );
    }
    
    /** @return The default field info for a field with a limited set of valid values. */
    public static String fieldInfoValidValues( String typeName, Object defaultValue, Object... validValues ) {
        return String.format( "<%s> Valid Values: { %s }, Default: %s",
                typeName, TomlHelper.literalList( validValues ), toLiteral( defaultValue ) );
    }
    
    /** @return The default field info for a series of int fields (no defaults listed). */
    public static String multiFieldInfo( IntField.Range range ) { return multiFieldInfo( range.MIN, range.MAX ); }
    
    /** @return The default field info for a series of int fields (no defaults listed). */
    public static String multiFieldInfo( int min, int max ) {
        return String.format( "<%s> Range: %s", "Integer", fieldRange( min, max ) );
    }
    
    /** @return The default field info for a series of double fields (no defaults listed). */
    public static String multiFieldInfo( DoubleField.Range range ) { return multiFieldInfo( range.MIN, range.MAX ); }
    
    /** @return The default field info for a series of double fields (no defaults listed). */
    public static String multiFieldInfo( double min, double max ) {
        return String.format( "<%s> Range: %s", "Number", fieldRange( min, max ) );
    }
    
    /** @return The default field info for a number with a range. */
    public static String fieldInfoRange( int defaultValue, int min, int max ) {
        return fieldInfoRange( "Integer", fieldRange( min, max ), defaultValue );
    }
    
    /** @return The default field info for a number with a range. */
    public static String fieldInfoRange( double defaultValue, double min, double max ) {
        return fieldInfoRange( "Number", fieldRange( min, max ), defaultValue );
    }
    
    /** @return The default field info for a number with a range. */
    private static String fieldInfoRange( String typeName, String range, Number defaultValue ) {
        return String.format( "<%s> Range: %s, Default: %s", typeName, range, toLiteral( defaultValue ) );
    }
    
    /** @return A range representation of toml literals. */
    public static String fieldRange( int min, int max ) {
        if( min == Integer.MIN_VALUE ) {
            if( max == Integer.MAX_VALUE ) {
                return fieldRangeNoLimit();
            }
            else {
                return fieldRangeUpperLimit( max );
            }
        }
        else if( max == Integer.MAX_VALUE ) {
            return fieldRangeLowerLimit( min );
        }
        else {
            return fieldRangeInterval( min, max );
        }
    }
    
    /** @return A range representation of toml literals. */
    public static String fieldRange( double min, double max ) {
        if( min <= -Double.MAX_VALUE ) {
            if( max >= Double.MAX_VALUE ) {
                return fieldRangeNoLimit();
            }
            else {
                return fieldRangeUpperLimit( max );
            }
        }
        else if( max >= Double.MAX_VALUE ) {
            return fieldRangeLowerLimit( min );
        }
        else {
            return fieldRangeInterval( min, max );
        }
    }
    
    /** @return A range representation of toml literals with no lower or upper limit. */
    private static String fieldRangeNoLimit() { return "Any Value"; }
    
    /** @return A range representation of toml literals with only an upper limit. */
    private static String fieldRangeUpperLimit( Number max ) { return ConfigUtil.LESS_OR_EQUAL + " " + toLiteral( max ); }
    
    /** @return A range representation of toml literals with only a lower limit. */
    private static String fieldRangeLowerLimit( Number min ) { return ConfigUtil.GREATER_OR_EQUAL + " " + toLiteral( min ); }
    
    /** @return A range representation of toml literals with both a lower and upper limit. */
    private static String fieldRangeInterval( Number min, Number max ) { return toLiteral( min ) + " ~ " + toLiteral( max ); }
    
    /** Splits a toml key into a path. */
    public static List<String> splitKey( String key ) { return StringUtils.split( key, '.' ); }
    
    /** Combines a toml path into a key. */
    public static String mergePath( @Nullable List<String> path ) {
        if( path == null || path.isEmpty() ) return "";
        StringBuilder key = new StringBuilder();
        for( String subKey : path ) {
            key.append( subKey ).append( '.' );
        }
        key.deleteCharAt( key.length() - 1 );
        return key.toString();
    }
    
    /** Convenience method for creating a list of single-line comments (no \n or \r). */
    public static ArrayList<String> newComment( String... lines ) { return new ArrayList<>( Arrays.asList( lines ) ); }
    
    /** Combines an array of objects as a comma-separated string. */
    public static String combineList( Object... list ) { return combineList( Arrays.asList( list ) ); }
    
    /** Combines a list of objects as a comma-separated string. */
    public static String combineList( @Nullable List<Object> list ) {
        if( list == null || list.isEmpty() ) return "";
        StringBuilder key = new StringBuilder();
        for( Object obj : list ) {
            key.append( obj ).append( ", " );
        }
        key.delete( key.length() - 2, key.length() );
        return key.toString();
    }
}