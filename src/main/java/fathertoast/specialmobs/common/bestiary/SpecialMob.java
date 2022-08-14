package fathertoast.specialmobs.common.bestiary;

import fathertoast.specialmobs.common.entity.ISpecialMob;

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
     * {@code public static MobFamily.Species<T> FIELD_NAME;}
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.FIELD )
    @interface SpeciesReference { }
    
    /**
     * REQUIRED. This is used to enforce overriding {@link ISpecialMob#getSpecies()} in every species entity class.
     * <p>
     * The annotated method must have a signature that follows the pattern:
     * <p>
     * {@code public MobFamily.Species<? extends T> getSpecies();}
     * <p>
     * The returned species should be the field annotated with {@link SpecialMob.SpeciesReference}.
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    @interface SpeciesSupplier { }
    
    /**
     * REQUIRED. This is called during registration to collect static properties of the mob needed for the bestiary
     * and for building the species's entity type.
     * This is not 'overridable' because all species must have unique info in the bestiary, however some info in the
     * builder is automatically inherited.
     * <p>
     * The annotated method must have a signature that follows the pattern:
     * <p>
     * {@code public static void METHOD_NAME( BestiaryInfo.Builder bestiaryInfo )}
     * <p>
     * The provided bestiary info builder is initialized with as much data from the parent entity as possible.
     * The only builder method that absolutely must be called is #color( int ). This will likely later expand to desc.
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    @interface BestiaryInfoSupplier { }
    
    /**
     * OPTIONAL-OVERRIDABLE. This is called during registration to generate the species config.
     * 'Overridable' static methods inherit from their superclass if not defined in a subclass.
     * This is 'overridable' because not all species (or even families) require unique config categories.
     * <p>
     * The annotated method must have a signature that follows the pattern:
     * <p>
     * {@code public static SpeciesConfig METHOD_NAME( MobFamily.Species<?> species )}
     * <p>
     * The returned config will be loaded immediately after the call and a reference will be stored in the species.
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    @interface ConfigSupplier { }
    
    /**
     * OVERRIDABLE. This is called during registration to build the base attributes for the species.
     * 'Overridable' static methods inherit from their superclass if not defined in a subclass, but must be defined somewhere.
     * This is 'overridable' because a few species have a different parent vanilla mob from the rest of their family.
     * <p>
     * The annotated method must have a signature that follows the pattern:
     * <p>
     * {@code public static AttributeModifierMap.MutableAttribute METHOD_NAME()}
     * <p>
     * The returned attribute modifier map builder will be 'built' immediately after the call and registered to be
     * applied to all entity class instances of the mob species.
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    @interface AttributeSupplier { }
    
    /**
     * OVERRIDABLE. This is called during registration to register the species's spawn placement.
     * 'Overridable' static methods inherit from their superclass if not defined in a subclass, but must be defined somewhere.
     * This is 'overridable' because some species may have a different natural spawn placement from the rest of their family.
     * <p>
     * The annotated method must have a signature that follows the pattern:
     * <p>
     * {@code public static void METHOD_NAME( MobFamily.Species<?> species )}
     *
     * @see fathertoast.specialmobs.common.event.NaturalSpawnManager
     * @see net.minecraft.entity.EntitySpawnPlacementRegistry
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    @interface SpawnPlacementRegistrar { }
    
    /**
     * REQUIRED. This is called during data generation to build the mod's default lang files.
     * This is not 'overridable' because all species must have unique names.
     * <p>
     * The annotated method must have a signature that follows the pattern:
     * <p>
     * {@code public static String[] METHOD_NAME( String langKey )}
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
     * {@code public static void METHOD_NAME( LootTableBuilder loot )}
     * <p>
     * The builder passed in is a new empty instance and will be 'built' immediately after the call.
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    @interface LootTableProvider { }
    
    /**
     * REQUIRED. This is called during registration to build the mob 'factory'. This will essentially just return the
     * class constructor that takes an entity type and a world for parameters.
     * This is not 'overridable' because all species must have unique factories.
     * <p>
     * The annotated method must have a signature that follows the pattern:
     * <p>
     * {@code public static EntityType.IFactory<T> METHOD_NAME()}
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    @interface Factory { }
}