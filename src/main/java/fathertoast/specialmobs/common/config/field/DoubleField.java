package fathertoast.specialmobs.common.config.field;

import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.core.SpecialMobs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Represents a config field with a double value.
 */
@SuppressWarnings( "unused" )
public class DoubleField extends AbstractConfigField {
    /** The default field value. */
    private final double valueDefault;
    /** The minimum field value. */
    private final double valueMin;
    /** The maximum field value. */
    private final double valueMax;
    
    /** The underlying field value. */
    private double value;
    
    /** Creates a new field that accepts a common range of values. */
    public DoubleField( String key, double defaultValue, Range range, String... description ) {
        this( key, defaultValue, range.MIN, range.MAX, description );
    }
    
    /** Creates a new field that accepts a specialized range of values. */
    public DoubleField( String key, double defaultValue, double min, double max, String... description ) {
        super( key, description );
        valueDefault = defaultValue;
        valueMin = min;
        valueMax = max;
    }
    
    /** @return Returns the config field's value. */
    public double get() { return value; }
    
    /** @return Treats the config field's value as a percent chance (from 0 to 1) and returns the result of a single roll. */
    public boolean rollChance( Random random ) { return random.nextDouble() < value; }
    
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
        final double newValue;
        if( raw instanceof Number ) {
            // Parse the value
            final double rawValue = ((Number) raw).doubleValue();
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
        ANY( Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY ),
        /** Accepts any non-negative value (>= 0). */
        NON_NEGATIVE( 0.0, Double.POSITIVE_INFINITY ),
        /** Accepts any value between 0 and 1. */
        PERCENT( 0.0, 1.0 ),
        /** Accepts any value between -1 and 1. */
        SIGNED_PERCENT( -1.0, 1.0 ),
        /** Accepts any value between -1 and 2. */
        DROP_CHANCE( -1.0, 2.0 );
        
        public final double MIN;
        public final double MAX;
        
        Range( double min, double max ) {
            MIN = min;
            MAX = max;
        }
    }
    
    /**
     * Represents two number fields, a minimum and a maximum, combined into one.
     * This has convenience methods for returning a random value between the min (inclusive) and the max (exclusive).
     */
    public static class RandomRange {
        /** The minimum. Defines the lower limit of the range (inclusive). */
        private final DoubleField MINIMUM;
        /** The maximum. Defines the upper limit of the range (exclusive). */
        private final DoubleField MAXIMUM;
        
        /** Links two values together as minimum and maximum. */
        public RandomRange( DoubleField minimum, DoubleField maximum ) {
            MINIMUM = minimum;
            MAXIMUM = maximum;
            if( minimum.valueDefault > maximum.valueDefault ) {
                throw new IllegalArgumentException( String.format( "Random range has inverted default values! (%s > %s) See: (%s, %s)",
                        minimum.valueDefault, maximum.valueDefault, minimum.getKey(), maximum.getKey() ) );
            }
        }
        
        /** @return Returns the minimum value of this range. */
        public double getMin() { return MINIMUM.get(); }
        
        /** @return Returns the maximum value of this range. */
        public double getMax() { return MAXIMUM.get(); }
        
        /** @return Returns a random value between the minimum (inclusive) and the maximum (exclusive). */
        public double next( Random random ) {
            final double delta = getMax() - getMin();
            if( delta > 1.0e-4 ) {
                return getMin() + random.nextDouble() * delta;
            }
            if( delta < 0.0 ) {
                SpecialMobs.LOG.warn( "Value for range \"({},{})\" is invalid ({} > {})! Ignoring maximum value.",
                        MINIMUM.getKey(), MAXIMUM.getKey(), getMin(), getMax() );
            }
            return getMin();
        }
    }
}