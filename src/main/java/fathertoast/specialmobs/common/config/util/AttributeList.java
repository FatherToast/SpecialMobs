package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.field.IStringArray;
import fathertoast.specialmobs.common.config.file.TomlHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of attribute-operation-value entries used to describe a set of attribute changes.
 * <p>
 * See also {@link ConfigDrivenAttributeModifierMap}
 */
public class AttributeList implements IStringArray {
    /** The attribute-operation-value entries in this list. */
    private final AttributeEntry[] ENTRIES;
    
    /**
     * Create a new attribute list from a list of entries.
     */
    public AttributeList( List<AttributeEntry> entries ) { this( entries.toArray( new AttributeEntry[0] ) ); }
    
    /**
     * Create a new entity list from an array of entries. Used for creating default configs.
     */
    public AttributeList( AttributeEntry... entries ) { ENTRIES = entries; }
    
    /** @return A string representation of this object. */
    @Override
    public String toString() { return TomlHelper.toLiteral( toStringList().toArray() ); }
    
    /** @return Returns true if this object has the same value as another object. */
    @Override
    public boolean equals( Object other ) {
        if( !(other instanceof AttributeList) ) return false;
        // Compare by the string list view of the object
        return toStringList().equals( ((AttributeList) other).toStringList() );
    }
    
    /** @return A list of strings that will represent this object when written to a toml file. */
    @Override
    public List<String> toStringList() {
        // Create a list of the entries in string format
        final List<String> list = new ArrayList<>( ENTRIES.length );
        for( AttributeEntry entry : ENTRIES ) {
            list.add( entry.toString() );
        }
        return list;
    }
    
    /** Applies all attribute changes in this list to the entity attribute builder. */
    public void apply( AttributeModifierMap.MutableAttribute builder ) {
        for( AttributeEntry entry : ENTRIES ) entry.apply( builder );
    }
    
    /** Applies all attribute changes in this list to the entity. */
    public void apply( LivingEntity entity ) {
        for( AttributeEntry entry : ENTRIES ) entry.apply( entity );
    }
}