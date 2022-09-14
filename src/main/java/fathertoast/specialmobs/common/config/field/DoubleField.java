package fathertoast.specialmobs.common.config.field;

import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

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
    public DoubleField( String key, double defaultValue, Range range, @Nullable String... description ) {
        this( key, defaultValue, range.MIN, range.MAX, description );
    }
    
    /** Creates a new field that accepts a specialized range of values. */
    public DoubleField( String key, double defaultValue, double min, double max, @Nullable String... description ) {
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
    
    /**
     * Represents a double field and an environment exception list, combined into one.
     * This has convenience methods for returning the value that should be used based on the environment.
     */
    public static class EnvironmentSensitive {
        /** The base value. */
        private final DoubleField BASE;
        /** The environment exceptions list. */
        private final EnvironmentListField EXCEPTIONS;
        
        /** Links two fields together as base and exceptions. */
        public EnvironmentSensitive( DoubleField base, EnvironmentListField exceptions ) {
            BASE = base;
            EXCEPTIONS = exceptions;
        }
        
        /** @return Returns the config field's value. */
        public double get( World world, @Nullable BlockPos pos ) { return EXCEPTIONS.getOrElse( world, pos, BASE ); }
        
        /** @return Treats the config field's value as a percent chance (from 0 to 1) and returns the result of a single roll. */
        public boolean rollChance( Random random, World world, @Nullable BlockPos pos ) { return random.nextDouble() < get( world, pos ); }
    }
    
    /**
     * Represents an environment sensitive list of weighted values. Unlike the normal weighted list, this is just a simple
     * wrapper class, and its weights are doubles.
     * It sacrifices automation for flexibility, largely to help with the craziness of environment list fields.
     */
    public static class EnvironmentSensitiveWeightedList<T> {
        
        private final List<Entry<T>> UNDERLYING_LIST;
        
        /** Links an array of values to two arrays of fields as base weights and exceptions. */
        public EnvironmentSensitiveWeightedList( T[] values, DoubleField[] baseWeights, EnvironmentListField[] weightExceptions ) {
            if( values.length != baseWeights.length || values.length != weightExceptions.length )
                throw new IllegalArgumentException( "All arrays must be equal length!" );
            
            final ArrayList<Entry<T>> list = new ArrayList<>();
            for( int i = 0; i < values.length; i++ ) {
                list.add( new Entry<>( values[i], new EnvironmentSensitive( baseWeights[i], weightExceptions[i] ) ) );
                
                // Do a bit of error checking; allows us to ignore the possibility of negative weights
                if( baseWeights[i].valueMin < 0.0 || weightExceptions[i].valueDefault.getMinValue() < 0.0 ) {
                    throw new IllegalArgumentException( "Weight is not allowed to be negative! See " +
                            baseWeights[i].getKey() + " and/or " + weightExceptions[i].getKey() );
                }
            }
            list.trimToSize();
            UNDERLYING_LIST = Collections.unmodifiableList( list );
        }
        
        /** @return Returns a random item from this weighted list. Null if none of the items have a positive weight. */
        @Nullable
        public T next( Random random, World world, @Nullable BlockPos pos ) { return next( random, world, pos, null ); }
        
        /** @return Returns a random item from this weighted list. Null if none of the items have a positive weight. */
        @Nullable
        public T next( Random random, World world, @Nullable BlockPos pos, @Nullable Predicate<T> selector ) {
            // Due to the 'nebulous' nature of environment-based weights, we must recalculate weights for EVERY call
            final double[] weights = new double[UNDERLYING_LIST.size()];
            double targetWeight = 0.0;
            for( int i = 0; i < weights.length; i++ ) {
                final Entry<T> entry = UNDERLYING_LIST.get( i );
                if( selector == null || selector.test( entry.VALUE ) ) {
                    targetWeight += weights[i] = entry.WEIGHT.get( world, pos );
                }
            }
            if( targetWeight <= 0.0 ) return null;
            
            // Now we unravel the target weight to a random point
            targetWeight *= random.nextDouble();
            for( int i = 0; i < weights.length; i++ ) {
                targetWeight -= weights[i];
                if( targetWeight < 0.0 ) return UNDERLYING_LIST.get( i ).VALUE;
            }
            
            SpecialMobs.LOG.error( "Environment-sensitive weight list was unable to return a value when it should have! " +
                    "This is probably due to error in floating point calculations, perhaps try changing the scale of weights." );
            return null;
        }
        
        private static class Entry<T> {
            final T VALUE;
            final DoubleField.EnvironmentSensitive WEIGHT;
            
            Entry( T value, DoubleField.EnvironmentSensitive weight ) {
                VALUE = value;
                WEIGHT = weight;
            }
        }
    }
}