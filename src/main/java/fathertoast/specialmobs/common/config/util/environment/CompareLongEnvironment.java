package fathertoast.specialmobs.common.config.util.environment;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class CompareLongEnvironment extends AbstractEnvironment {
    /** How the actual value is compared to this environment's value. */
    public final ComparisonOperator COMPARATOR;
    /** The value for this environment. */
    public final long VALUE;
    
    public CompareLongEnvironment( ComparisonOperator op, long value ) {
        COMPARATOR = op;
        VALUE = value;
    }
    
    public CompareLongEnvironment( AbstractConfigField field, String line ) {
        if( line.isEmpty() ) {
            COMPARATOR = ComparisonOperator.LESS_THAN;
            VALUE = 0L;
            SpecialMobs.LOG.warn( "Invalid entry for {} \"{}\"! Not defined. Defaulting to \"{}\". Invalid entry: {}",
                    field.getClass(), field.getKey(), value(), line );
        }
        else {
            final ComparisonOperator op = ComparisonOperator.parse( line );
            if( op == null ) {
                COMPARATOR = ComparisonOperator.LESS_THAN;
                SpecialMobs.LOG.warn( "Invalid entry for {} \"{}\"! Comparison not defined (must be in the set [ {} ]). Defaulting to \"{}\". Invalid entry: {}",
                        field.getClass(), field.getKey(), TomlHelper.literalList( (Object[]) ComparisonOperator.values() ), COMPARATOR, line );
            }
            else COMPARATOR = op;
            VALUE = parseValue( field, line, line.substring( COMPARATOR.toString().length() ).trim() );
        }
    }
    
    /** @return Parses the value and returns a valid result. */
    private long parseValue( AbstractConfigField field, String line, String arg ) {
        // Try to parse the value
        long value;
        try {
            value = Long.parseLong( arg );
        }
        catch( NumberFormatException ex ) {
            SpecialMobs.LOG.warn( "Invalid entry for {} \"{}\"! Value not defined (must be a long). Defaulting to '0'. Invalid entry: {}",
                    field.getClass(), field.getKey(), line );
            value = 0;
        }
        // Verify value is within range
        if( value < getMinValue() ) {
            SpecialMobs.LOG.warn( "Value for {} \"{}\" is below the minimum ({})! Clamping value. Invalid value: {}",
                    field.getClass(), field.getKey(), getMinValue(), value );
            value = getMinValue();
        }
        else if( value > getMaxValue() ) {
            SpecialMobs.LOG.warn( "Value for {} \"{}\" is above the maximum ({})! Clamping value. Invalid value: {}",
                    field.getClass(), field.getKey(), getMaxValue(), value );
            value = getMaxValue();
        }
        return value;
    }
    
    /** @return The minimum value that can be given to the value. */
    protected long getMinValue() { return Long.MIN_VALUE; }
    
    /** @return The maximum value that can be given to the value. */
    protected long getMaxValue() { return Long.MAX_VALUE; }
    
    /** @return The string value of this environment, as it would appear in a config file. */
    @Override
    public String value() { return COMPARATOR + " " + VALUE; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public boolean matches( World world, @Nullable BlockPos pos ) {
        final Long actual = getActual( world, pos );
        return actual != null && COMPARATOR.apply( actual, VALUE );
    }
    
    /** @return Returns the actual value to compare, or null if there isn't enough information. */
    @Nullable
    public abstract Long getActual( World world, @Nullable BlockPos pos );
}