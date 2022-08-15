package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.file.TomlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of entity-value entries used to link one or more numbers to specific entity types.
 */
@SuppressWarnings( { "unused", "SameParameterValue" } )
public class EntityList implements IStringArray {
    /** The entity-value entries in this list. */
    private final EntityEntry[] ENTRIES;
    
    /** The number of values each entry must have. If this is negative, then entries may have any non-zero number of values. */
    private int entryValues = -1;
    /** The minimum value accepted for entry values in this list. */
    private double minValue = Double.NEGATIVE_INFINITY;
    /** The maximum value accepted for entry values in this list. */
    private double maxValue = Double.POSITIVE_INFINITY;
    
    /**
     * Create a new entity list from a list of entries.
     * <p>
     * By default, entity lists will allow any non-zero number of values, and the value(s) can be any numerical double.
     * These parameters can be changed with helper methods that alter the number of values or values' bounds and return 'this'.
     */
    public EntityList( List<EntityEntry> entries ) { this( entries.toArray( new EntityEntry[0] ) ); }
    
    /**
     * Create a new entity list from an array of entries. Used for creating default configs.
     * <p>
     * By default, entity lists will allow any non-zero number of values, and the value(s) can be any numerical double.
     * These parameters can be changed with helper methods that alter the number of values or values' bounds and return 'this'.
     */
    public EntityList( EntityEntry... entries ) { ENTRIES = entries; }
    
    /** @return A string representation of this object. */
    @Override
    public String toString() { return TomlHelper.toLiteral( toStringList().toArray() ); }
    
    /** @return Returns true if this object has the same value as another object. */
    @Override
    public boolean equals( @Nullable Object other ) {
        if( !(other instanceof EntityList) ) return false;
        // Compare by the string list view of the object
        return toStringList().equals( ((EntityList) other).toStringList() );
    }
    
    /** @return A list of strings that will represent this object when written to a toml file. */
    @Override
    public List<String> toStringList() {
        // Create a list of the entries in string format
        final List<String> list = new ArrayList<>( ENTRIES.length );
        for( EntityEntry entry : ENTRIES ) {
            list.add( entry.toString() );
        }
        return list;
    }
    
    /** @return True if the entity is contained in this list. */
    public boolean contains( @Nullable Entity entity ) {
        if( entity == null ) return false;
        final EntityEntry targetEntry = new EntityEntry( entity );
        for( EntityEntry currentEntry : ENTRIES ) {
            currentEntry.checkClass( entity.level );
            if( currentEntry.contains( targetEntry ) )
                return true;
        }
        return false;
    }
    
    /**
     * @param entity The entity to retrieve values for.
     * @return The array of values of the best-match entry. Returns null if the entity is not contained in this entity list.
     */
    @Nullable
    public double[] getValues( @Nullable Entity entity ) {
        if( entity == null ) return null;
        final EntityEntry targetEntry = new EntityEntry( entity );
        EntityEntry bestMatch = null;
        for( EntityEntry currentEntry : ENTRIES ) {
            currentEntry.checkClass( entity.level );
            // Immediately return if we match the most stringent entry possible
            if( !currentEntry.EXTEND && currentEntry.entityClass == targetEntry.entityClass ) {
                return currentEntry.VALUES;
            }
            // Otherwise, update the best match if we match for the first time, or we match a more specific entry
            else if( currentEntry.contains( targetEntry ) && (bestMatch == null || bestMatch.contains( currentEntry )) ) {
                bestMatch = currentEntry;
            }
        }
        return bestMatch == null ? null : bestMatch.VALUES;
    }
    
    /**
     * @param entity The entity to retrieve a value for.
     * @return The first value in the best-match entry's value array. Returns 0 if the entity is not contained in this
     * entity list or has no values specified. This should only be used for 'single value' lists.
     * @see #setSingleValue()
     * @see #setSinglePercent()
     */
    public double getValue( @Nullable Entity entity ) {
        final double[] values = getValues( entity );
        return values == null || values.length < 1 ? 0.0 : values[0];
    }
    
    /**
     * @param entity The entity to roll a value for.
     * @return Randomly rolls the first percentage value in the best-match entry's value array. Returns false if the entity
     * is not contained in this entity list or has no values specified. This should only be used for 'single percent' lists.
     * @see #setSinglePercent()
     */
    public boolean rollChance( @Nullable LivingEntity entity ) {
        return ENTRIES.length > 0 && entity != null && entity.getRandom().nextDouble() < getValue( entity );
    }
    
    /** Marks this entity list as a simple percentage listing; exactly one percent (0 to 1) per entry. */
    public EntityList setSinglePercent() { return setSingleValue().setRange0to1(); }
    
    /** Marks this entity list as identification only; no values will be linked to any entries. */
    public EntityList setNoValues() { return setMultiValue( 0 ); }
    
    /** Marks this entity list as single-value; each entry will have exactly one value. */
    public EntityList setSingleValue() { return setMultiValue( 1 ); }
    
    /** Marks this entity list as multi-value; each entry will have the specified number of values. */
    public EntityList setMultiValue( int numberOfValues ) {
        entryValues = numberOfValues;
        return this;
    }
    
    /** Bounds entry values in this list between 0 and 1, inclusive. */
    public EntityList setRange0to1() { return setRange( 0.0, 1.0 ); }
    
    /** Bounds entry values in this list to any positive value (>= +0). */
    public EntityList setRangePos() { return setRange( 0.0, Double.POSITIVE_INFINITY ); }
    
    /** Bounds entry values in this list to the specified limits, inclusive. Note that 0 must be within the range. */
    private EntityList setRange( double min, double max ) {
        minValue = min;
        maxValue = max;
        return this;
    }
    
    /**
     * @return The number of values that must be included in each entry.
     * A negative value implies any non-zero number of values is allowed.
     */
    public int getRequiredValues() { return entryValues; }
    
    /** @return The minimum value that can be given to entry values. */
    public double getMinValue() { return minValue; }
    
    /** @return The maximum value that can be given to entry values. */
    public double getMaxValue() { return maxValue; }
}