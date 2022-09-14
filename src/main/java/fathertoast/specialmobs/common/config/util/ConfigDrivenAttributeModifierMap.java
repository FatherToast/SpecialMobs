package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.field.AttributeListField;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConfigDrivenAttributeModifierMap extends AttributeModifierMap {
    
    private final AttributeListField FIELD;
    private final Map<Attribute, ModifiableAttributeInstance> BASE_ATTRIBUTES;
    
    private AttributeModifierMap underlyingMap;
    
    public ConfigDrivenAttributeModifierMap( AttributeListField field, MutableAttribute builder ) {
        super( builder.builder );
        FIELD = field;
        BASE_ATTRIBUTES = builder.builder;
        field.linkedAttributeMap = this;
    }
    
    /** Called when the config field is loaded to force a reload. */
    public void invalidate() { underlyingMap = null; }
    
    /** Called before any access to the underlying map to ensure it is loaded (nonnull). */
    private void validate() {
        if( underlyingMap != null ) return;
        
        // Create a deep clone of the base attribute map
        final MutableAttribute builder = builder();
        for( Map.Entry<Attribute, ModifiableAttributeInstance> entry : BASE_ATTRIBUTES.entrySet() ) {
            builder.add( entry.getKey(), entry.getValue().getBaseValue() );
        }
        FIELD.apply( builder );
        underlyingMap = builder.build();
    }
    
    @Override
    public double getValue( Attribute attribute ) {
        validate();
        return underlyingMap.getValue( attribute );
    }
    
    @Override
    public double getBaseValue( Attribute attribute ) {
        validate();
        return underlyingMap.getBaseValue( attribute );
    }
    
    @Override
    public double getModifierValue( Attribute attribute, UUID uuid ) {
        validate();
        return underlyingMap.getModifierValue( attribute, uuid );
    }
    
    @Override
    @Nullable
    public ModifiableAttributeInstance createInstance( Consumer<ModifiableAttributeInstance> onChanged, Attribute attribute ) {
        validate();
        return underlyingMap.createInstance( onChanged, attribute );
    }
    
    @Override
    public boolean hasAttribute( Attribute attribute ) { return BASE_ATTRIBUTES.containsKey( attribute ); }
    
    @Override
    public boolean hasModifier( Attribute attribute, UUID uuid ) {
        validate();
        return underlyingMap.hasModifier( attribute, uuid );
    }
}