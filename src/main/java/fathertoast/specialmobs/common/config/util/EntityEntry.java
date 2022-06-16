package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.field.EntityListField;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * One entity-value entry in an entity list.
 */
@SuppressWarnings( "unused" )
public class EntityEntry {
    /** The entity type this entry is defined for. If this is null, then this entry will match any entity. */
    public final EntityType<? extends Entity> TYPE;
    /** True if this should check for instanceof the entity class (as opposed to equals). */
    public final boolean EXTEND;
    /** The values given to this entry. Null for comparison objects. */
    public final double[] VALUES;
    
    /** The class this entry is defined for. This is not assigned until a world has been loaded. */
    Class<? extends Entity> entityClass;
    
    /** Creates an entry used to compare entity classes internally with the entries in an entity list. */
    EntityEntry( Entity entity ) {
        TYPE = entity.getType();
        EXTEND = false;
        VALUES = null;
        entityClass = entity.getClass();
    }
    
    /** Creates an entry with the specified values that acts as a default matching all entity types. Used for creating default configs. */
    public EntityEntry( double... values ) {
        this( null, true, values );
    }
    
    /** Creates an extendable entry with the specified values. Used for creating default configs. */
    public EntityEntry( EntityType<? extends Entity> entityType, double... values ) {
        this( entityType, true, values );
    }
    
    /** Creates an entry with the specified values. Used for creating default configs. */
    public EntityEntry( EntityType<? extends Entity> entityType, boolean extend, double... values ) {
        TYPE = entityType;
        EXTEND = extend;
        VALUES = values;
    }
    
    /** Called on this entry before using it to check if the entity class has been determined, and loads the class if it has not been. */
    void checkClass( World world ) {
        if( TYPE != null && entityClass == null ) {
            try {
                final Entity entity = TYPE.create( world );
                if( entity != null ) {
                    entityClass = entity.getClass();
                    entity.remove();
                }
            }
            catch( Exception ex ) {
                SpecialMobs.LOG.warn( "Failed to load class of entity type {}!", TYPE );
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * @return Returns true if the given entity description is contained within this one (is more specific).
     * <p>
     * This operates under the assumption that there will not be multiple default entries or multiple non-extendable
     * entries for the same class in a list.
     */
    public boolean contains( EntityEntry entry ) {
        // Handle default entries
        if( TYPE == null ) return true;
        if( entry.TYPE == null ) return false;
        // Same entity, but non-extendable is more specific
        if( entityClass == entry.entityClass ) return !entry.EXTEND;
        // Extendable entry, check if the other is for a subclass
        if( EXTEND ) return entityClass.isAssignableFrom( entry.entityClass );
        // Non-extendable entries cannot contain other entries
        return false;
    }
    
    /**
     * @return The string representation of this entity list entry, as it would appear in a config file.
     * <p>
     * Format is "~registry_key value0 value1 ...", the ~ prefix is optional.
     */
    @Override
    public String toString() {
        // Start with the entity type registry key
        ResourceLocation resource = TYPE == null ? null : ForgeRegistries.ENTITIES.getKey( TYPE );
        StringBuilder str = new StringBuilder( resource == null ? EntityListField.REG_KEY_DEFAULT : resource.toString() );
        // Insert "specific" prefix if not extendable
        if( !EXTEND ) {
            str.insert( 0, '~' );
        }
        // Append values array
        if( VALUES != null && VALUES.length > 0 ) {
            for( double value : VALUES ) {
                str.append( ' ' ).append( value );
            }
        }
        return str.toString();
    }
}