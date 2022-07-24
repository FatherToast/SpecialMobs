package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EntityListField;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * One entity-value entry in an entity list. Uses a 'lazy' implementation so the entity type registry is
 * not polled until this entry is actually used.
 */
@SuppressWarnings( "unused" )
public class EntityEntry {
    /** The field containing this entry. We save a reference to help improve error/warning reports. */
    private final AbstractConfigField FIELD;
    
    /** The registry key for this entry's entity type. */
    public final ResourceLocation ENTITY_KEY;
    /** True if this should check for instanceof the entity class (as opposed to equals). */
    public final boolean EXTEND;
    /** The values given to this entry. Null for comparison objects. */
    public final double[] VALUES;
    
    /** The entity type this entry is defined for. If this is null, then this entry will match any entity. */
    private EntityType<? extends Entity> entityType;
    /** The class this entry is defined for. This is not assigned until a world has been loaded. */
    Class<? extends Entity> entityClass;
    
    /** Creates an entry used to compare entity classes internally with the entries in an entity list. */
    EntityEntry( Entity entity ) {
        FIELD = null;
        ENTITY_KEY = entity.getType().getRegistryName();
        EXTEND = false;
        VALUES = null;
        entityType = entity.getType();
        entityClass = entity.getClass();
    }
    
    /** Creates an entry with the specified values that acts as a default matching all entity types. Used for creating default configs. */
    public EntityEntry( double... values ) { this( null, true, values ); }
    
    /** Creates an extendable entry with the specified values. Used for creating default configs. */
    public EntityEntry( @Nullable EntityType<? extends Entity> type, double... values ) { this( type, true, values ); }
    
    /** Creates an entry with the specified values. Used for creating default configs. */
    public EntityEntry( @Nullable EntityType<? extends Entity> type, boolean extend, double... values ) {
        this( null, type == null ? null : type.getRegistryName(), extend, values );
        entityType = type;
    }
    
    /** Creates an entry with the specified values. */
    public EntityEntry( @Nullable AbstractConfigField field, @Nullable ResourceLocation regKey, boolean extend, double... values ) {
        FIELD = field;
        ENTITY_KEY = regKey;
        EXTEND = extend;
        VALUES = values;
    }
    
    /** @return Loads the entity type from registry. Returns true if successful. */
    private boolean validate() {
        if( entityType != null || ENTITY_KEY == null ) return true; // Null entity key means this is a default entry
        
        if( !ForgeRegistries.ENTITIES.containsKey( ENTITY_KEY ) ) {
            SpecialMobs.LOG.warn( "Invalid entry for {} \"{}\"! Invalid entry: {}",
                    FIELD.getClass(), FIELD.getKey(), ENTITY_KEY.toString() );
            return false;
        }
        entityType = ForgeRegistries.ENTITIES.getValue( ENTITY_KEY );
        return true;
    }
    
    /** Called on this entry before using it to check if the entity class has been determined, and loads the class if it has not been. */
    void checkClass( World world ) {
        if( validate() && entityType != null && entityClass == null ) {
            try {
                final Entity entity = entityType.create( world );
                if( entity != null ) {
                    entityClass = entity.getClass();
                    entity.remove();
                }
            }
            catch( Exception ex ) {
                SpecialMobs.LOG.warn( "Failed to load class of entity type {}!", entityType );
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
        if( !validate() ) return false;
        
        // Handle default entries
        if( entityType == null ) return true;
        if( entry.entityType == null ) return false;
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
        StringBuilder str = new StringBuilder( ENTITY_KEY == null ? EntityListField.REG_KEY_DEFAULT : ENTITY_KEY.toString() );
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