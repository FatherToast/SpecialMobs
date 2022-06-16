package fathertoast.specialmobs.common.config.field;

import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.config.util.EntityEntry;
import fathertoast.specialmobs.common.config.util.EntityList;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a config field with an entity list value.
 */
public class EntityListField extends GenericField<EntityList> {
    /** The string to use in place of a registry key for a default entry. */
    public static final String REG_KEY_DEFAULT = "default";
    
    /** Provides a detailed description of how to use entity lists. Recommended to put at the top of any file using entity lists. */
    @Override
    public List<String> verboseDescription() {
        List<String> comment = new ArrayList<>();
        comment.add( "Entity List fields: General format = [ \"namespace:entity_type value1 value2 ...\", ... ]" );
        comment.add( "  Entity lists are arrays of entity types. Some entity lists specify a number of values linked to each entity type." );
        comment.add( "  Entity types are defined by their key in the entity registry, usually following the pattern 'namespace:entity_name'." );
        comment.add( "  '" + REG_KEY_DEFAULT + "' can be used instead of an entity type registry key to provide default values for all entities." );
        comment.add( "  An asterisk '*' can be used to match multiple entity types. For example, 'minecraft:*' will match all vanilla entities." );
        comment.add( "  List entries by default match any entity type derived from (i.e. based on) their entity type. For example, '~minecraft:zombie'." );
        comment.add( "    There is no steadfast rule about extending, even in vanilla, but the hope is that mod-added mobs will extend their base mob." );
        return comment;
    }
    
    /** Creates a new field. */
    public EntityListField( String key, EntityList defaultValue, String... description ) {
        super( key, defaultValue, description );
    }
    
    /** Adds info about the field type, format, and bounds to the end of a field's description. */
    public void appendFieldInfo( List<String> comment ) {
        // Number of values to include
        final int reqValues = valueDefault.getRequiredValues();
        final String fieldFormat;
        if( reqValues < 0 ) {
            // Variable number of values
            fieldFormat = "[ \"namespace:entity_type value1 value2 ...\", ... ]";
        }
        else {
            // Specific number of values
            StringBuilder format = new StringBuilder( "[ \"namespace:entity_type " );
            for( int i = 1; i <= reqValues; i++ ) {
                format.append( "value" );
                if( reqValues > 1 ) {
                    format.append( i );
                }
                format.append( " " );
            }
            format.deleteCharAt( format.length() - 1 ).append( "\", ... ]" );
            fieldFormat = format.toString();
        }
        comment.add( TomlHelper.fieldInfoFormat( "Entity List", valueDefault, fieldFormat ) );
        
        // Range for values, if applicable
        if( reqValues != 0 ) {
            comment.add( "   Range for Values: " + TomlHelper.fieldRange( valueDefault.getMinValue(), valueDefault.getMaxValue() ) );
        }
        
    }
    
    /**
     * Loads this field's value from the given raw toml value. If anything goes wrong, correct it at the lowest level possible.
     * <p>
     * For example, a missing value should be set to the default, while an out-of-range value should be adjusted to the
     * nearest in-range value
     */
    @Override
    public void load( @Nullable Object raw ) {
        if( raw == null ) {
            value = valueDefault;
            return;
        }
        List<String> list = TomlHelper.parseStringList( raw );
        List<EntityEntry> entryList = new ArrayList<>();
        for( String line : list ) {
            EntityEntry entry = parseEntry( line );
            if( entry != null ) {
                entryList.add( entry );
            }
        }
        value = new EntityList( entryList );
    }
    
    /** Parses a single entry line and returns a valid result if possible, or null if the entry is completely invalid. */
    @Nullable
    private EntityEntry parseEntry( final String line ) {
        String modifiedLine = line;
        
        // Check if the entry should be "specific", i.e. check for entity class equality rather than instanceof
        final boolean extendable;
        if( line.startsWith( "~" ) ) {
            modifiedLine = line.substring( 1 );
            extendable = false;
        }
        else {
            extendable = true;
        }
        
        // Parse the entity-value array
        final String[] args = modifiedLine.split( " " );
        final EntityType<? extends Entity> entityType;
        if( REG_KEY_DEFAULT.equalsIgnoreCase( args[0].trim() ) ) {
            // Handle the special case of a default entry
            entityType = null;
        }
        else {
            // Normal entry
            final ResourceLocation regKey = new ResourceLocation( args[0].trim() );
            if( !ForgeRegistries.ENTITIES.containsKey( regKey ) ) {
                SpecialMobs.LOG.warn( "Invalid entry for {} \"{}\"! Deleting entry. Invalid entry: {}",
                        getClass(), getKey(), line );
                return null;
            }
            entityType = ForgeRegistries.ENTITIES.getValue( regKey );
        }
        final List<Double> valuesList = new ArrayList<>();
        final int reqValues = valueDefault.getRequiredValues();
        final int actualValues = args.length - 1;
        
        // Variable-value; just needs at least one value
        if( reqValues < 0 ) {
            if( actualValues < 1 ) {
                SpecialMobs.LOG.warn( "Entry has too few values for {} \"{}\"! Expected at least one value. " +
                                "Replacing missing value with 0. Invalid entry: {}",
                        getClass(), getKey(), line );
                valuesList.add( 0.0 );
            }
            else {
                // Parse all values
                for( int i = 1; i < args.length; i++ ) {
                    valuesList.add( parseValue( args[i], line ) );
                }
            }
        }
        // Specified value; must have the exact number of values
        else {
            if( reqValues > actualValues ) {
                SpecialMobs.LOG.warn( "Entry has too few values for {} \"{}\"! " +
                                "Expected {} values, but detected {}. Replacing missing values with 0. Invalid entry: {}",
                        getClass(), getKey(), reqValues, actualValues, line );
            }
            else if( reqValues < actualValues ) {
                SpecialMobs.LOG.warn( "Entry has too many values for {} \"{}\"! " +
                                "Expected {} values, but detected {}. Deleting additional values. Invalid entry: {}",
                        getClass(), getKey(), reqValues, actualValues, line );
            }
            
            // Parse all values
            for( int i = 1; i < reqValues + 1; i++ ) {
                if( i < args.length ) {
                    valuesList.add( parseValue( args[i], line ) );
                }
                else {
                    valuesList.add( 0.0 );
                }
            }
        }
        
        // Convert to array
        final double[] values = new double[valuesList.size()];
        for( int i = 0; i < values.length; i++ ) {
            values[i] = valuesList.get( i );
        }
        return new EntityEntry( entityType, extendable, values );
    }
    
    /** Parses a single value argument and returns a valid result. */
    private double parseValue( final String arg, final String line ) {
        // Try to parse the value
        double value;
        try {
            value = Double.parseDouble( arg );
        }
        catch( NumberFormatException ex ) {
            // This is thrown if the string is not a parsable number
            SpecialMobs.LOG.warn( "Invalid value for {} \"{}\"! Falling back to 0. Invalid entry: {}",
                    getClass(), getKey(), line );
            value = 0.0;
        }
        // Verify value is within range
        if( value < valueDefault.getMinValue() ) {
            SpecialMobs.LOG.warn( "Value for {} \"{}\" is below the minimum ({})! Clamping value. Invalid value: {}",
                    getClass(), getKey(), valueDefault.getMinValue(), value );
            value = valueDefault.getMinValue();
        }
        else if( value > valueDefault.getMaxValue() ) {
            SpecialMobs.LOG.warn( "Value for {} \"{}\" is above the maximum ({})! Clamping value. Invalid value: {}",
                    getClass(), getKey(), valueDefault.getMaxValue(), value );
            value = valueDefault.getMaxValue();
        }
        return value;
    }
    
    /**
     * Represents two entity list fields, a blacklist and a whitelist, combined into one.
     * The blacklist cannot contain values, but the whitelist can have any settings.
     */
    public static class Combined {
        /** The whitelist. To match, the entry must be present here. */
        private final EntityListField WHITELIST;
        /** The blacklist. Entries present here are ignored entirely. */
        private final EntityListField BLACKLIST;
        
        /** Links two lists together as blacklist and whitelist. */
        public Combined( EntityListField whitelist, EntityListField blacklist ) {
            WHITELIST = whitelist;
            BLACKLIST = blacklist;
            if( blacklist.valueDefault.getRequiredValues() != 0 ) {
                throw new IllegalArgumentException( "Blacklists cannot have values! See: " + blacklist.getKey() );
            }
        }
        
        /** @return True if the entity is contained in this list. */
        public boolean contains( Entity entity ) {
            return entity != null && !BLACKLIST.get().contains( entity ) && WHITELIST.get().contains( entity );
        }
        
        /**
         * @param entity The entity to retrieve values for.
         * @return The array of values of the best-match entry. Returns null if the entity is not contained in this entity list.
         */
        public double[] getValues( Entity entity ) {
            return entity != null && !BLACKLIST.get().contains( entity ) ? WHITELIST.get().getValues( entity ) : null;
        }
        
        /**
         * @param entity The entity to retrieve a value for.
         * @return The first value in the best-match entry's value array. Returns 0 if the entity is not contained in this
         * entity list or has no values specified. This should only be used for 'single value' lists.
         * @see EntityList#setSingleValue()
         * @see EntityList#setSinglePercent()
         */
        public double getValue( Entity entity ) {
            return entity != null && !BLACKLIST.get().contains( entity ) ? WHITELIST.get().getValue( entity ) : 0.0;
        }
        
        /**
         * @param entity The entity to roll a value for.
         * @return Randomly rolls the first percentage value in the best-match entry's value array. Returns false if the entity
         * is not contained in this entity list or has no values specified. This should only be used for 'single percent' lists.
         * @see EntityList#setSinglePercent()
         */
        public boolean rollChance( LivingEntity entity ) {
            return entity != null && !BLACKLIST.get().contains( entity ) && WHITELIST.get().rollChance( entity );
        }
    }
}