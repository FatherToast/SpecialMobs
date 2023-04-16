package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * One attribute-operation-value entry in an attribute list. Uses a 'lazy' implementation so the attribute registry is
 * not polled until this entry is actually used.
 * <p>
 * See also {@link ConfigDrivenAttributeModifierMap}
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings( "unused" )
public class AttributeEntry {
    /** The field containing this entry. We save a reference to help improve error/warning reports. */
    private final AbstractConfigField FIELD;
    
    /** The registry key for this entry's attribute. */
    public final ResourceLocation ATTRIBUTE_KEY;
    /** True if the value should be multiplied to the base attribute value (as opposed to added). */
    public final boolean MULTIPLY;
    /** The value given to this entry. */
    public final double VALUE;
    
    /** The attribute this entry is defined for. */
    private Attribute attribute;
    
    /** Creates an entry with the specified values using the addition operation. Incompatible with move speed. Used for creating default configs. */
    public static AttributeEntry add( Attribute attribute, double value ) {
        if( attribute.equals( Attributes.MOVEMENT_SPEED ) )
            throw new IllegalArgumentException( "Move speed should not be added!" );
        return new AttributeEntry( attribute, false, value );
    }
    
    /** Creates an entry with the specified values using the multiplication operation. Used for creating default configs. */
    public static AttributeEntry mult( Attribute attribute, double value ) { return new AttributeEntry( attribute, true, value ); }
    
    /** Creates an entry with the specified values. */
    private AttributeEntry( Attribute attrib, boolean multiply, double value ) {
        this( null, ForgeRegistries.ATTRIBUTES.getKey( attrib ), multiply, value );
        attribute = attrib;
    }
    
    /** Creates an entry with the specified values. */
    public AttributeEntry( @Nullable AbstractConfigField field, @Nullable ResourceLocation regKey, boolean multiply, double value ) {
        FIELD = field;
        ATTRIBUTE_KEY = regKey;
        MULTIPLY = multiply;
        VALUE = value;
    }
    
    /** @return Loads the attribute from registry. Returns true if successful. */
    private boolean validate() {
        if( attribute != null ) return true;
        
        if( !ForgeRegistries.ATTRIBUTES.containsKey( ATTRIBUTE_KEY ) ) {
            SpecialMobs.LOG.warn( "Invalid entry for {} \"{}\"! Invalid entry: {}",
                    FIELD.getClass(), FIELD.getKey(), ATTRIBUTE_KEY.toString() );
            return false;
        }
        attribute = ForgeRegistries.ATTRIBUTES.getValue( ATTRIBUTE_KEY );
        return true;
    }
    
    /**
     * @return The string representation of this entity list entry, as it would appear in a config file.
     * <p>
     * Format is "registry_key operation value", operation may be +, -, or *.
     */
    @Override
    public String toString() {
        // Start with the attribute registry key
        StringBuilder str = new StringBuilder( ATTRIBUTE_KEY.toString() ).append( ' ' );
        // Append operation and value
        if( MULTIPLY ) str.append( "* " ).append( VALUE );
        else if( VALUE < 0.0 ) str.append( "- " ).append( -VALUE );
        else str.append( "+ " ).append( VALUE );
        return str.toString();
    }
    
    /** Applies this attribute change to the entity attribute builder. */
    public void apply( AttributeSupplier.Builder builder ) {
        if( validate() ) apply( builder.builder.get( attribute ) );
    }
    
    /** Applies this attribute change to the entity. */
    public void apply( LivingEntity entity ) {
        if( validate() ) apply( entity.getAttribute( attribute ) );
    }
    
    /** Applies this attribute change to the attribute instance. Assumes that the instance is for this entry's target attribute. */
    private void apply( @Nullable AttributeInstance attributeInstance ) {
        if( attributeInstance == null )
            throw new IllegalStateException( "Attempted to modify non-registered attribute " + ATTRIBUTE_KEY );
        
        if( MULTIPLY ) attributeInstance.setBaseValue( attributeInstance.getBaseValue() * VALUE );
        else attributeInstance.setBaseValue( attributeInstance.getBaseValue() + VALUE );
    }
}