package fathertoast.specialmobs.common.config.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * One attribute-operation-value entry in an attribute list.
 */
@SuppressWarnings( "unused" )
public class AttributeEntry {
    /** The attribute this entry is defined for. */
    public final Attribute ATTRIBUTE;
    /** True if the value should be multiplied to the base attribute value (as opposed to added). */
    public final boolean MULTIPLY;
    /** The value given to this entry. */
    public final double VALUE;
    
    /** The class this entry is defined for. This is not assigned until a world has been loaded. */
    Class<? extends Entity> entityClass;
    
    /** Creates an entry with the specified values using the addition operation. Incompatible with move speed. Used for creating default configs. */
    public static AttributeEntry add( Attribute attribute, double value ) {
        if( attribute.equals( Attributes.MOVEMENT_SPEED ) )
            throw new IllegalArgumentException( "Move speed should not be added!" );
        return new AttributeEntry( attribute, false, value );
    }
    
    /** Creates an entry with the specified values using the multiplication operation. Used for creating default configs. */
    public static AttributeEntry mult( Attribute attribute, double value ) { return new AttributeEntry( attribute, true, value ); }
    
    /** Creates an entry with the specified values. */
    private AttributeEntry( Attribute attribute, boolean multiply, double value ) {
        ATTRIBUTE = attribute;
        MULTIPLY = multiply;
        VALUE = value;
    }
    
    /**
     * @return The string representation of this entity list entry, as it would appear in a config file.
     * <p>
     * Format is "registry_key operation value", operation may be +, -, or *.
     */
    @Override
    public String toString() {
        // Start with the attribute registry key
        ResourceLocation resource = ForgeRegistries.ATTRIBUTES.getKey( ATTRIBUTE );
        StringBuilder str = new StringBuilder( resource == null ? "null" : resource.toString() ).append( ' ' );
        // Append operation and value
        if( MULTIPLY ) str.append( "* " ).append( VALUE );
        else if( VALUE < 0.0 ) str.append( "- " ).append( -VALUE );
        else str.append( "+ " ).append( VALUE );
        return str.toString();
    }
    
    /** Applies this attribute change to the entity attribute builder. */
    public void apply( AttributeModifierMap.MutableAttribute builder ) { apply( builder.builder.get( ATTRIBUTE ) ); }
    
    /** Applies this attribute change to the entity. */
    public void apply( LivingEntity entity ) { apply( entity.getAttribute( ATTRIBUTE ) ); }
    
    /** Applies this attribute change to the attribute instance. Assumes that the instance is for this entry's target attribute. */
    private void apply( ModifiableAttributeInstance attributeInstance ) {
        if( attributeInstance == null )
            throw new IllegalStateException( "Attempted to modify non-registered attribute " + ATTRIBUTE.getDescriptionId() );
        
        if( MULTIPLY ) attributeInstance.setBaseValue( attributeInstance.getBaseValue() * VALUE );
        else attributeInstance.setBaseValue( attributeInstance.getBaseValue() + VALUE );
    }
}