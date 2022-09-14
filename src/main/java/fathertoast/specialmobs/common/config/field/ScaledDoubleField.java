package fathertoast.specialmobs.common.config.field;

import javax.annotation.Nullable;

/**
 * Represents a config field with a double value. The entered config value is converted by a specified scale factor when loaded.
 */
@SuppressWarnings( "unused" )
public class ScaledDoubleField extends DoubleField {
    /** Conversion scale factor for this field. */
    private final double SCALE;
    
    /** The underlying field value, scaled by the set conversion factor. */
    private double valueScaled;
    
    /** Creates a new field that accepts a common range of values. */
    public ScaledDoubleField( String key, double defaultValue, double scale, Range range, @Nullable String... description ) {
        super( key, defaultValue, range, description );
        SCALE = scale;
    }
    
    /** Creates a new field that accepts a specialized range of values. */
    public ScaledDoubleField( String key, double defaultValue, double scale, double min, double max, @Nullable String... description ) {
        super( key, defaultValue, min, max, description );
        SCALE = scale;
    }
    
    /**
     * Loads this field's value from the given raw toml value. If anything goes wrong, correct it at the lowest level possible.
     * <p>
     * For example, a missing value should be set to the default, while an out-of-range value should be adjusted to the
     * nearest in-range value
     */
    @Override
    public void load( @Nullable Object raw ) {
        super.load( raw );
        valueScaled = super.get() * SCALE;
    }
    
    /** @return Returns the config field's value. */
    @Override
    public double get() { return valueScaled; }
    
    /** @return Returns the unscaled form of the config field's value. */
    public double getUnscaled() { return super.get(); }
    
    /**
     * Represents a config field with a double value. The entered config value is converted from 'per second' to 'per tick' when loaded.
     */
    public static class Rate extends ScaledDoubleField {
        /** Conversion factor to convert from more meaningful units (blocks per second or m/s) to blocks per tick. */
        private static final double PER_SECOND_TO_PER_TICK = 0.05;
        
        /** Creates a new field that accepts a common range of values. */
        public Rate( String key, double defaultValue, Range range, String... description ) {
            super( key, defaultValue, PER_SECOND_TO_PER_TICK, range, description );
        }
        
        /** Creates a new field that accepts a specialized range of values. */
        public Rate( String key, double defaultValue, double min, double max, String... description ) {
            super( key, defaultValue, PER_SECOND_TO_PER_TICK, min, max, description );
        }
    }
}