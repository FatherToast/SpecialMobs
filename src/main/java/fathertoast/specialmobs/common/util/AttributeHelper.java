package fathertoast.specialmobs.common.util;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;

/** An attribute builder used to easily tweak pre-existing attributes. */
public class AttributeHelper {
    /** @return An attribute builder used to easily tweak pre-existing attributes. */
    public static AttributeHelper of( AttributeModifierMap.MutableAttribute builder ) {
        return new AttributeHelper( builder );
    }
    
    private final AttributeModifierMap.MutableAttribute underlyingBuilder;
    
    private AttributeHelper( AttributeModifierMap.MutableAttribute builder ) {
        underlyingBuilder = builder;
    }
    
    /** @return The current state of this builder. */
    public AttributeModifierMap.MutableAttribute build() { return underlyingBuilder; }
    
    /**
     * Alters the entity's base attribute by adding an amount to it.
     * Do NOT use this for move speed, instead use {@link #multAttribute(Attribute, double)}
     *
     * @param attribute the attribute to modify
     * @param amount    the amount to add to the attribute
     */
    public AttributeHelper addAttribute( Attribute attribute, double amount ) {
        final ModifiableAttributeInstance attributeInstance = underlyingBuilder.builder.get( attribute );
        if( attributeInstance == null )
            throw new IllegalStateException( "Attempted to modify non-registered attribute " + attribute.getDescriptionId() );
        attributeInstance.setBaseValue( attributeInstance.getBaseValue() + amount );
        return this;
    }
    
    /**
     * Alters the entity's base attribute by multiplying it by an amount.
     * Mainly use this for move speed, for other attributes use {@link #addAttribute(Attribute, double)}
     *
     * @param attribute the attribute to modify
     * @param amount    the amount to multiply the attribute by
     */
    public AttributeHelper multAttribute( Attribute attribute, double amount ) {
        final ModifiableAttributeInstance attributeInstance = underlyingBuilder.builder.get( attribute );
        if( attributeInstance == null )
            throw new IllegalStateException( "Attempted to modify non-registered attribute " + attribute.getDescriptionId() );
        attributeInstance.setBaseValue( attributeInstance.getBaseValue() * amount );
        return this;
    }
}