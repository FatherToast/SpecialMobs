package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.field.DoubleField;
import fathertoast.specialmobs.common.config.file.TomlHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of condition-value entries used to link one number to specific environments.
 */
@SuppressWarnings( { "unused", "SameParameterValue" } )
public class EnvironmentList implements IStringArray {
    /** The condition-value entries in this list. */
    private final EnvironmentEntry[] ENTRIES;
    
    /** The minimum value accepted for entry values in this list. */
    private double minValue = Double.NEGATIVE_INFINITY;
    /** The maximum value accepted for entry values in this list. */
    private double maxValue = Double.POSITIVE_INFINITY;
    
    /**
     * Create a new environment list from a list of entries.
     * <p>
     * By default, environment list value(s) can be any numerical double.
     * This can be changed with helper methods that alter values' bounds and return 'this'.
     */
    public EnvironmentList( List<EnvironmentEntry> entries ) { this( entries.toArray( new EnvironmentEntry[0] ) ); }
    
    /**
     * Create a new environment list from an array of entries. Used for creating default configs.
     * <p>
     * By default, environment list value(s) can be any numerical double.
     * This can be changed with helper methods that alter values' bounds and return 'this'.
     */
    public EnvironmentList( EnvironmentEntry... entries ) { ENTRIES = entries; }
    
    /** @return A string representation of this object. */
    @Override
    public String toString() { return TomlHelper.toLiteral( toStringList().toArray() ); }
    
    /** @return Returns true if this object has the same value as another object. */
    @Override
    public boolean equals( @Nullable Object other ) {
        if( !(other instanceof EnvironmentList) ) return false;
        // Compare by the string list view of the object
        return toStringList().equals( ((EnvironmentList) other).toStringList() );
    }
    
    /** @return A list of strings that will represent this object when written to a toml file. */
    @Override
    public List<String> toStringList() {
        // Create a list of the entries in string format
        final List<String> list = new ArrayList<>( ENTRIES.length );
        for( EnvironmentEntry entry : ENTRIES ) {
            list.add( entry.toString() );
        }
        return list;
    }
    
    /** @return The value matching the given environment, or the default value if no matching environment is defined. */
    public double getOrElse(Level level, @Nullable BlockPos pos, DoubleField defaultValue ) {
        return getOrElse( level, pos, defaultValue.get() );
    }
    
    /** @return The value matching the given environment, or the default value if no matching environment is defined. */
    public double getOrElse( Level level, @Nullable BlockPos pos, double defaultValue ) {
        final Double value = get( level, pos );
        return value == null ? defaultValue : value;
    }
    
    /** @return The value matching the given environment, or null if no matching environment is defined. */
    @Nullable
    public Double get( Level level, @Nullable BlockPos pos ) {
        for( EnvironmentEntry entry : ENTRIES ) {
            if( entry.matches( level, pos ) ) return entry.VALUE;
        }
        return null;
    }
    
    /** Bounds entry values in this list to the specified range. */
    public EnvironmentList setRange( DoubleField.Range range ) { return setRange( range.MIN, range.MAX ); }
    
    /** Bounds entry values in this list to the specified limits, inclusive. */
    public EnvironmentList setRange( double min, double max ) {
        minValue = min;
        maxValue = max;
        return this;
    }
    
    /** @return The minimum value that can be given to entry values. */
    public double getMinValue() { return minValue; }
    
    /** @return The maximum value that can be given to entry values. */
    public double getMaxValue() { return maxValue; }
}