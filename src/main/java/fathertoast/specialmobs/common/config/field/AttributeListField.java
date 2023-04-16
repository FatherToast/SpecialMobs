package fathertoast.specialmobs.common.config.field;

import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.config.util.AttributeEntry;
import fathertoast.specialmobs.common.config.util.AttributeList;
import fathertoast.specialmobs.common.config.util.ConfigDrivenAttributeModifierMap;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a config field with an entity list value.
 * <p>
 * See also {@link ConfigDrivenAttributeModifierMap}
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AttributeListField extends GenericField<AttributeList> {
    
    /** Provides a detailed description of how to use attribute lists. Recommended to put at the top of any file using attribute lists. */
    public static List<String> verboseDescription() {
        List<String> comment = new ArrayList<>();
        comment.add( "Attribute List fields: General format = [ \"namespace:attribute_name operation value\", ... ]" );
        comment.add( "  Attribute lists are arrays of base attribute changes. Attributes are defined by their key in the attribute registry," );
        comment.add( "    usually following the pattern 'namespace:attribute_name'." );
        comment.add( "  The operations that can be performed are +, -, and *. The + and - operators change the attribute by adding the value to it" );
        comment.add( "    (or subtracting the value from it). The * operator changes the attribute by multiplying it by the value." );
        comment.add( "  Each entry in the attribute list is applied in the exact order listed." );
        comment.add( "  As an example, the entry \"minecraft:generic.max_health + 10.0\" will increase a mob's max health by 10. By convention, never" );
        comment.add( "    use the + or - operators for movement speed (minecraft:generic.movement_speed)." );
        return comment;
    }
    
    /** The linked attribute modifier map, if any. You must have a separate field per map you want to link. */
    public ConfigDrivenAttributeModifierMap linkedAttributeMap;
    
    /** Creates a new field. */
    public AttributeListField( String key, AttributeList defaultValue, @Nullable String... description ) {
        super( key, defaultValue, description );
    }
    
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
            entryList.add( parseEntry( line ) );
        }
        value = new AttributeList( entryList );
        
        if( linkedAttributeMap != null ) linkedAttributeMap.invalidate();
    }
    
    /** Parses a single entry line and returns a valid result. */
    private AttributeEntry parseEntry( final String line ) {
        // Parse the attribute-operation-value array
        final String[] args = line.split( " ", 4 );
        if( args.length > 3 ) {
            SpecialMobs.LOG.warn( "Entry has for {} \"{}\" is too long! Deleting excess. Invalid entry: {}",
                    getClass(), getKey(), line );
        }
        
        final ResourceLocation regKey = new ResourceLocation( args[0].trim() );
        
        // 1 and -1 are used for add and subtract as a shortcut to convert to addition
        final byte OP_MULTIPLY = 0;
        final byte OP_ADD = 1;
        final byte OP_SUBTRACT = -1;
        
        final byte operator;
        if( args.length < 2 ) {
            SpecialMobs.LOG.warn( "Entry has no operator for {} \"{}\"! Replacing missing operator with +. Invalid entry: {}",
                    getClass(), getKey(), line );
            operator = OP_ADD;
        }
        else {
            switch (args[1]) {
                case "*" -> operator = OP_MULTIPLY;
                case "+" -> operator = OP_ADD;
                case "-" -> operator = OP_SUBTRACT;
                default -> {
                    operator = OP_ADD;
                    SpecialMobs.LOG.warn("Entry has invalid operator {} for {} \"{}\"! Replacing operator with +. " +
                            "Invalid entry: {}", args[1], getClass(), getKey(), line);
                }
            }
        }
        final int identityValue = operator == OP_MULTIPLY ? 1 : 0;
        
        final double value;
        if( args.length < 3 ) {
            SpecialMobs.LOG.warn( "Entry has no value for {} \"{}\"! Replacing missing value with {}. Invalid entry: {}",
                    getClass(), getKey(), identityValue, line );
            value = identityValue;
        }
        else {
            value = parseValue( args[2], line, identityValue );
        }
        
        return operator == OP_MULTIPLY ? new AttributeEntry( this, regKey, true, value ) :
                new AttributeEntry( this, regKey, false, value * operator );
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
    
    
    // Convenience methods
    
    /** Applies all attribute changes in this list to the entity attribute builder. */
    public void apply( AttributeSupplier.Builder builder ) { get().apply( builder ); }
    
    /** Applies all attribute changes in this list to the entity. */
    public void apply( LivingEntity entity ) { get().apply( entity ); }
}