package fathertoast.specialmobs.common.config.field;

import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.core.SpecialMobs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Represents a config field with an integer value.
 */
@SuppressWarnings( "unused" )
public class IntField extends AbstractConfigField {
    /** The default field value. */
    private final int valueDefault;
    /** The minimum field value. */
    private final int valueMin;
    /** The maximum field value. */
    private final int valueMax;
    
    /** The underlying field value. */
    private int value;
    
    /** Creates a new field that accepts a common range of values. */
    public IntField( String key, int defaultValue, Range range, @Nullable String... description ) {
        this( key, defaultValue, range.MIN, range.MAX, description );
    }
    
    /** Creates a new field that accepts a specialized range of values. */
    public IntField( String key, int defaultValue, int min, int max, @Nullable String... description ) {
        super( key, description );
        valueDefault = defaultValue;
        valueMin = min;
        valueMax = max;
    }
    
    /** @return Returns the config field's value. */
    public int get() { return value; }
    
    /** Adds info about the field type, format, and bounds to the end of a field's description. */
    public void appendFieldInfo( List<String> comment ) {
        comment.add( TomlHelper.fieldInfoRange( valueDefault, valueMin, valueMax ) );
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
        final int newValue;
        if( raw instanceof Number ) {
            // Parse the value
            final int rawValue = ((Number) raw).intValue();
            if( rawValue < valueMin ) {
                SpecialMobs.LOG.warn( "Value for {} \"{}\" is below the minimum ({})! Clamping value. Invalid value: {}",
                        getClass(), getKey(), valueMin, raw );
                newValue = valueMin;
            }
            else if( rawValue > valueMax ) {
                SpecialMobs.LOG.warn( "Value for {} \"{}\" is above the maximum ({})! Clamping value. Invalid value: {}",
                        getClass(), getKey(), valueMax, raw );
                newValue = valueMax;
            }
            else {
                if( (double) rawValue != ((Number) raw).doubleValue() ) {
                    SpecialMobs.LOG.warn( "Value for {} \"{}\" is not an integer! Truncating value. Invalid value: {}",
                            getClass(), getKey(), raw );
                }
                newValue = rawValue;
            }
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
    
    /** A set of commonly used ranges for this field type. */
    public enum Range {
        /** Accepts any value. */
        ANY( Integer.MIN_VALUE, Integer.MAX_VALUE ),
        /** Accepts any positive value (> 0). */
        POSITIVE( 1, Integer.MAX_VALUE ),
        /** Accepts any non-negative value (>= 0). */
        NON_NEGATIVE( 0, Integer.MAX_VALUE ),
        /** Accepts any non-negative value and -1 (>= -1). */
        TOKEN_NEGATIVE( -1, Integer.MAX_VALUE );
        
        public final int MIN;
        public final int MAX;
        
        Range( int min, int max ) {
            MIN = min;
            MAX = max;
        }
    }
    
    /**
     * Represents two number fields, a minimum and a maximum, combined into one.
     * This has convenience methods for returning a random value between the min and the max (inclusive).
     */
    public static class RandomRange {
        /** The minimum. Defines the lower limit of the range (inclusive). */
        private final IntField MINIMUM;
        /** The maximum. Defines the upper limit of the range (inclusive). */
        private final IntField MAXIMUM;
        
        /** Links two values together as minimum and maximum. */
        public RandomRange( IntField minimum, IntField maximum ) {
            MINIMUM = minimum;
            MAXIMUM = maximum;
            if( minimum.valueDefault > maximum.valueDefault ) {
                throw new IllegalArgumentException( String.format( "Random range has inverted default values! (%s > %s) See: (%s, %s)",
                        minimum.valueDefault, maximum.valueDefault, minimum.getKey(), maximum.getKey() ) );
            }
        }
        
        /** @return Returns the minimum value of this range. */
        public int getMin() { return MINIMUM.get(); }
        
        /** @return Returns the maximum value of this range. */
        public int getMax() { return MAXIMUM.get(); }
        
        /** @return Returns a random value between the minimum and the maximum (inclusive). */
        public int next( Random random ) {
            final int delta = getMax() - getMin();
            if( delta > 0 ) {
                return getMin() + random.nextInt( delta + 1 );
            }
            if( delta < 0 ) {
                SpecialMobs.LOG.warn( "Value for range \"({},{})\" is invalid ({} > {})! Ignoring maximum value.",
                        MINIMUM.getKey(), MAXIMUM.getKey(), getMin(), getMax() );
            }
            return getMin();
        }
    }
}