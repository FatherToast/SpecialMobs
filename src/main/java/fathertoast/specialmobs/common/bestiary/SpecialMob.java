package fathertoast.specialmobs.common.bestiary;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to note classes that define special mobs entities. Think of this as a kind of static interface that defines
 * a number of static methods that must be implemented in each special mob class.
 * <p>
 * Currently no runtime processing is done on special mob classes, however this could be modified in the future
 * to fully automate variant registration, if desired.
 */
@Target( ElementType.TYPE )
public @interface SpecialMob {
    
    /**
     * REQUIRED. This is grabbed during registration to be used as a mob 'factory'; the needed constructor will probably
     * already be needed by the entity's superclass, so it's just a matter of applying the annotation in that case.
     * <p>
     * The annotated constructor must have a signature that follows the pattern:
     * <p>
     * public CLASS_NAME( EntityType entityType, World world )
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.CONSTRUCTOR )
    @interface Constructor { }
    
    /**
     * REQUIRED. This is called during registration to collect static properties of the mob needed for the bestiary
     * and for building the species's entity type.
     * <p>
     * The annotated method must have a signature that follows the pattern:
     * <p>
     * public static BestiaryInfo METHOD_NAME( EntityType.Builder<LivingEntity> entityType )
     * <p>
     * The returned bestiary info will be used to describe the mob species.
     * The builder passed in is a copy of the vanilla 'base' entity type. Common uses of the entity type builder are
     * modifying entity size and fire/hazard immunities.
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    @interface BestiaryInfoSupplier { }
    
    /**
     * REQUIRED. This is called during registration to build the base attributes for the species.
     * <p>
     * The annotated method must have a signature that follows the pattern:
     * <p>
     * public static AttributeModifierMap.MutableAttribute METHOD_NAME()
     * <p>
     * The returned attribute modifier map builder will be 'built' immediately after the call and registered to be
     * applied to all entity class instances of the mob species.
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    @interface AttributeCreator { }
    
    /**
     * REQUIRED. This is called during data generation to build the mob's default loot table. Special variants will
     * typically start this method by calling their vanilla replacement's implementation of this method.
     * <p>
     * The annotated method must have a signature that follows the pattern:
     * <p>
     * public static void METHOD_NAME( LootTableBuilder loot )
     * <p>
     * The builder passed in is a new empty instance and will be 'built' immediately after the call.
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    @interface LootTableProvider { }
}