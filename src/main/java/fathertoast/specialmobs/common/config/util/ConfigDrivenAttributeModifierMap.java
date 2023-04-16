package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.field.AttributeListField;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConfigDrivenAttributeModifierMap extends AttributeSupplier {
    
    private final AttributeListField FIELD;
    private final Map<Attribute, AttributeInstance> BASE_ATTRIBUTES;
    
    private AttributeSupplier underlyingSupplier;
    
    public ConfigDrivenAttributeModifierMap( AttributeListField field, AttributeSupplier.Builder builder ) {
        super( builder.builder );
        FIELD = field;
        BASE_ATTRIBUTES = builder.builder;
        field.linkedAttributeMap = this;
    }
    
    /** Called when the config field is loaded to force a reload. */
    public void invalidate() { underlyingSupplier = null; }
    
    /** Called before any access to the underlying map to ensure it is loaded (nonnull). */
    private void validate() {
        if( underlyingSupplier != null ) return;
        
        // Create a deep clone of the base attribute map
        final Builder builder = builder();
        for( Map.Entry<Attribute, AttributeInstance> entry : BASE_ATTRIBUTES.entrySet() ) {
            builder.add( entry.getKey(), entry.getValue().getBaseValue() );
        }
        FIELD.apply( builder );
        underlyingSupplier = builder.build();
    }
    
    @Override
    public double getValue( Attribute attribute ) {
        validate();
        return underlyingSupplier.getValue( attribute );
    }
    
    @Override
    public double getBaseValue( Attribute attribute ) {
        validate();
        return underlyingSupplier.getBaseValue( attribute );
    }
    
    @Override
    public double getModifierValue( Attribute attribute, UUID uuid ) {
        validate();
        return underlyingSupplier.getModifierValue( attribute, uuid );
    }
    
    @Override
    @Nullable
    public AttributeInstance createInstance( Consumer<AttributeInstance> onChanged, Attribute attribute ) {
        validate();
        return underlyingSupplier.createInstance( onChanged, attribute );
    }
    
    @Override
    public boolean hasAttribute( Attribute attribute ) { return BASE_ATTRIBUTES.containsKey( attribute ); }
    
    @Override
    public boolean hasModifier( Attribute attribute, UUID uuid ) {
        validate();
        return underlyingSupplier.hasModifier( attribute, uuid );
    }
}