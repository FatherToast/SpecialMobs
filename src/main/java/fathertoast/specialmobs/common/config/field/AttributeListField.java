package fathertoast.specialmobs.common.config.field;

import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.config.util.AttributeEntry;
import fathertoast.specialmobs.common.config.util.AttributeList;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a config field with an entity list value.
 */
public class AttributeListField extends GenericField<AttributeList> {
    
    /** Provides a detailed description of how to use attribute lists. Recommended to put at the top of any file using attribute lists. */
    public static List<String> verboseDescription() {
        List<String> comment = new ArrayList<>();
        comment.add( "Attribute List fields: General format = [ \"namespace:attribute_name operation value\", ... ]" );
        comment.add( "  Attribute lists are arrays of base attribute changes. Attributes are defined by their key in the attribute registry," );
        comment.add( "  usually following the pattern 'namespace:attribute_name'. The operations that can be performed are +, -, and *. The + and -" );
        comment.add( "  operators change the attribute by a flat value by addition/subtraction. The * operator changes the attribute by multiplication." );
        comment.add( "  Each entry in the attribute list is applied in the exact order listed." );
        comment.add( "  As an example, the entry \"minecraft:generic.max_health + 10.0\" will increase a mob's max health by 10. By convention, never" );
        comment.add( "  use the + or - operators for movement speed (minecraft:generic.movement_speed)." );
        return comment;
    }
    
    /** Creates a new field. */
    public AttributeListField( String key, AttributeList defaultValue, String... description ) {
        super( key, defaultValue, description );
    }
    
    /** Applies all attribute changes in this list to the entity attribute builder. */
    public void apply( AttributeModifierMap.MutableAttribute builder ) { get().apply( builder ); }
    
    /** Applies all attribute changes in this list to the entity. */
    public void apply( LivingEntity entity ) { get().apply( entity ); }
    
    /** Adds info about the field type, format, and bounds to the end of a field's description. */
    public void appendFieldInfo( List<String> comment ) {
        comment.add( TomlHelper.fieldInfoFormat( "Attribute List", valueDefault,
                "[ \"namespace:attribute_name operation value\", ... ]" ) );
        comment.add( "   Range for Values: " + TomlHelper.fieldRange( DoubleField.Range.ANY.MIN, DoubleField.Range.ANY.MAX ) );
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
        List<AttributeEntry> entryList = new ArrayList<>();
        for( String line : list ) {
            AttributeEntry entry = parseEntry( line );
            if( entry != null ) {
                entryList.add( entry );
            }
        }
        value = new AttributeList( entryList );
    }
    
    /** Parses a single entry line and returns a valid result if possible, or null if the entry is completely invalid. */
    @Nullable
    private AttributeEntry parseEntry( final String line ) {
        // Parse the attribute-operation-value array
        final String[] args = line.split( " ", 4 );
        if( args.length > 3 ) {
            SpecialMobs.LOG.warn( "Entry has for {} \"{}\" is too long! Deleting excess. Invalid entry: {}",
                    getClass(), getKey(), line );
        }
        
        final Attribute attribute;
        final ResourceLocation regKey = new ResourceLocation( args[0].trim() );
        if( !ForgeRegistries.ATTRIBUTES.containsKey( regKey ) ) {
            SpecialMobs.LOG.warn( "Invalid entry for {} \"{}\"! Deleting entry. Invalid entry: {}",
                    getClass(), getKey(), line ); // TODO note: I don't know if attributes will be registered at this point
            return null;
        }
        attribute = ForgeRegistries.ATTRIBUTES.getValue( regKey );
        
        final int operator;
        if( args.length < 2 ) {
            SpecialMobs.LOG.warn( "Entry has no operator for {} \"{}\"! Replacing missing operator with +. Invalid entry: {}",
                    getClass(), getKey(), line );
            operator = 1;
        }
        else {
            switch( args[1] ) {
                case "*": operator = 0;
                    break;
                case "+": operator = 1;
                    break;
                case "-": operator = -1;
                    break;
                default: operator = 1;
                    SpecialMobs.LOG.warn( "Entry has invalid operator {} for {} \"{}\"! Replacing operator with +. " +
                            "Invalid entry: {}", args[1], getClass(), getKey(), line );
                    break;
            }
        }
        final int identityValue = operator == 0 ? 1 : 0;
        
        final double value;
        if( args.length < 3 ) {
            SpecialMobs.LOG.warn( "Entry has no value for {} \"{}\"! Replacing missing value with {}. Invalid entry: {}",
                    getClass(), getKey(), identityValue, line );
            value = identityValue;
        }
        else {
            value = parseValue( args[2], line, identityValue );
        }
        
        //noinspection ConstantConditions
        return operator == 0 ? AttributeEntry.mult( attribute, value ) : AttributeEntry.add( attribute, value * operator );
    }
    
    /** Parses a single value argument and returns a valid result. */
    private double parseValue( final String arg, final String line, final int identity ) {
        // Try to parse the value
        double value;
        try {
            value = Double.parseDouble( arg );
        }
        catch( NumberFormatException ex ) {
            // This is thrown if the string is not a parsable number
            SpecialMobs.LOG.warn( "Invalid value for {} \"{}\"! Falling back to {}. Invalid entry: {}",
                    getClass(), getKey(), identity, line );
            value = identity;
        }
        return value;
    }
}