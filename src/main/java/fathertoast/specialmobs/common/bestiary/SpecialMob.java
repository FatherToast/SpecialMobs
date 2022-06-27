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
     * REQUIRED. This is injected with a reference to the species during registration so you may access it later, as needed.
     * <p>
     * The annotated field must have a signature that follows the pattern:
     * <p>
     * public static MobFamily.Species<T> FIELD_NAME;
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.FIELD )
    @interface SpeciesReference { }
    
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
     * This is not 'overridable' because all species must have unique info in the bestiary.
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
     * OVERRIDABLE. This is called during registration to build the base attributes for the species.
     * 'Overridable' static methods inherit from their superclass if not defined in a subclass, but must be defined somewhere.
     * This is 'overridable' because not all species need to have different attributes from their parent vanilla mob.
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
     * REQUIRED. This is called during data generation to build the mod's default lang files.
     * This is not 'overridable' because all species must have unique names.
     * <p>
     * The annotated method must have a signature that follows the pattern:
     * <p>
     * public static String[] METHOD_NAME( String langKey )
     * <p>
     * The returned string array should be created by References#translations using the given lang key as the first
     * argument. Always be sure that any non-ASCII characters used are properly handled by the translations method.
     *
     * @see fathertoast.specialmobs.common.util.References#translations(String, String, String, String, String, String, String, String)
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    @interface LanguageProvider { }
    
    /**
     * REQUIRED. This is called during data generation to build the mob's default loot table. Special variants will
     * typically start this method by calling their vanilla replacement's implementation of this method.
     * This is not 'overridable' because all species must have unique default loot tables.
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