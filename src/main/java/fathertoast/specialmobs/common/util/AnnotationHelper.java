package fathertoast.specialmobs.common.util;

import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Provides helper methods to handle annotation processing through reflection.
 */
@SuppressWarnings( "SameParameterValue" )
public final class AnnotationHelper {
    
    //--------------- PRETTY HELPER METHODS ----------------
    
    /** Injects a reference to the special mob species into the species entity class. Throws an exception if anything goes wrong. */
    public static void injectSpeciesReference( MobFamily.Species<?> species ) {
        try {
            final Field field = getField( species.entityClass, SpecialMob.SpeciesReference.class );
            field.set( null, species );
        }
        catch( IllegalAccessException | NoSuchFieldException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " has invalid species reference holder", ex );
        }
    }
    
    /** Verifies the special mob species's entity class is overriding ISpecialMob#getSpecies(). Throws an exception if anything goes wrong. */
    public static void verifySpeciesSupplier( MobFamily.Species<?> species ) {
        try {
            getNonstaticMethod( species.entityClass, SpecialMob.SpeciesSupplier.class );
        }
        catch( NoSuchMethodException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " does not override ISpecialMob#getSpecies()", ex );
        }
    }
    
    /** Gets bestiary info from a special mob species. Throws an exception if anything goes wrong. */
    public static <T extends Mob> BestiaryInfo.Builder getBestiaryInfo(MobFamily.Species<T> species, BestiaryInfo.Builder bestiaryInfo ) {
        try {
            getMethod( species.entityClass, SpecialMob.BestiaryInfoSupplier.class ).invoke( null, bestiaryInfo );
            return bestiaryInfo;
        }
        catch( IllegalAccessException | NoSuchMethodException | InvocationTargetException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " has invalid bestiary info method", ex );
        }
    }
    
    /** Creates a species config from a special mob species. Throws an exception if anything goes wrong. */
    public static SpeciesConfig createConfig( ConfigManager manager, MobFamily.Species<?> species ) {
        try {
            final Method supplier = getMethodOrSuperOptional( species.entityClass, SpecialMob.ConfigSupplier.class );
            if( supplier == null ) {
                return new SpeciesConfig( manager, species );
            }
            return (SpeciesConfig) supplier.invoke( null, manager, species );
        }
        catch( InvocationTargetException | IllegalAccessException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " has invalid config creation method", ex );
        }
    }
    
    /** Creates an attribute modifier map from a special mob species. Throws an exception if anything goes wrong. */
    public static AttributeSupplier.Builder createAttributes(MobFamily.Species<?> species ) {
        try {
            return (AttributeSupplier.Builder) getMethodOrSuper( species.entityClass, SpecialMob.AttributeSupplier.class )
                    .invoke( null );
        }
        catch( NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " has invalid attribute creation method", ex );
        }
    }
    
    /** Registers the entity spawn placement for a special mob species. Throws an exception if anything goes wrong. */
    public static void registerSpawnPlacement( MobFamily.Species<?> species ) {
        try {
            getMethodOrSuper( species.entityClass, SpecialMob.SpawnPlacementRegistrar.class ).invoke( null, species );
        }
        catch( NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " has invalid spawn placement registration method", ex );
        }
    }
    
    /** Gets the translations from a special mob species. Throws an exception if anything goes wrong. */
    public static String[] getTranslations( MobFamily.Species<?> species ) {
        try {
            return (String[]) getMethod( species.entityClass, SpecialMob.LanguageProvider.class )
                    .invoke( null, species.entityType.get().getDescriptionId() );
        }
        catch( NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " has invalid language provider method", ex );
        }
    }
    
    /** Gets the translations from a block. Throws an exception if anything goes wrong. */
    public static String[] getTranslations( Block block ) { return getTranslations( ForgeRegistries.BLOCKS, block, block.getDescriptionId() ); }
    
    /** Gets the translations from an item. Throws an exception if anything goes wrong. */
    public static String[] getTranslations( Item item ) { return getTranslations( ForgeRegistries.ITEMS, item, item.getDescriptionId() ); }
    
    /** Gets the translations from a registry entry. Throws an exception if anything goes wrong. */
    public static <T> String[] getTranslations( IForgeRegistry<T> registry, T entry, String key ) {
        try {
            return (String[]) getMethod( entry.getClass(), SpecialMob.LanguageProvider.class )
                    .invoke( null, key );
        }
        catch( NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ) {
            throw new RuntimeException( "Class for " + registry.getKey(entry) + " has invalid language provider method", ex );
        }
    }
    
    /** Builds a loot table from a special mob species. Throws an exception if anything goes wrong. */
    public static LootTableBuilder buildLootTable( MobFamily.Species<?> species ) {
        try {
            final LootTableBuilder builder = new LootTableBuilder();
            getMethod( species.entityClass, SpecialMob.LootTableProvider.class ).invoke( null, builder );
            return builder;
        }
        catch( NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " has invalid loot table builder method", ex );
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static List<TagKey<EntityType<?>>> getEntityTags(Class<? extends LivingEntity> entityClass ) {
        try {
            Method method = getMethodOrSuperOptional( entityClass, SpecialMob.EntityTagProvider.class );

            if (method != null) {
                Object ret = method.invoke( null );

                if (ret != null)
                    return (List<TagKey<EntityType<?>>>) ret;
            }
            return null;
        }
        catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /** Creates an entity factory from a special mob species. Throws an exception if anything goes wrong. */
    public static <T extends Mob> EntityType.EntityFactory<T> getEntityFactory( MobFamily.Species<T> species ) {
        try {
            //noinspection unchecked
            return (EntityType.EntityFactory<T>) getMethod( species.entityClass, SpecialMob.Factory.class ).invoke( null );
        }
        catch( NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " has invalid factory provider method", ex );
        }
    }
    
    
    //--------------- RAW ANNOTATION METHODS ----------------
    
    /**
     * @return Pulls a static field with a specific annotation from a class.
     * @throws NoSuchFieldException if the field does not exist in the class.
     */
    private static Field getField( Class<?> type, Class<? extends Annotation> annotation ) throws NoSuchFieldException {
        final Field field = getFieldOptional( type, annotation );
        if( field == null ) {
            throw new NoSuchFieldException( String.format( "Could not find required static @%s annotated field in %s",
                    annotation.getSimpleName(), type.getName() ) );
        }
        return field;
    }
    
    /**
     * @return Pulls a static field with a specific annotation from a class, or null if the field does not exist.
     */
    @Nullable
    private static Field getFieldOptional( Class<?> type, Class<? extends Annotation> annotation ) {
        for( Field field : type.getDeclaredFields() ) {
            if( Modifier.isStatic( field.getModifiers() ) && field.isAnnotationPresent( annotation ) )
                return field;
        }
        return null;
    }
    
    /**
     * @return Pulls a nonstatic method with a specific annotation from a class.
     * @throws NoSuchMethodException if the method does not exist in the class.
     */
    @SuppressWarnings( "UnusedReturnValue" )
    private static Method getNonstaticMethod( Class<?> type, Class<? extends Annotation> annotation ) throws NoSuchMethodException {
        for( Method method : type.getDeclaredMethods() ) {
            if( !Modifier.isStatic( method.getModifiers() ) && method.isAnnotationPresent( annotation ) )
                return method;
        }
        throw new NoSuchMethodException( String.format( "Could not find required nonstatic @%s annotated method in %s",
                annotation.getSimpleName(), type.getName() ) );
    }
    
    /**
     * @return Pulls a static method with a specific annotation from a class.
     * @throws NoSuchMethodException if the method does not exist in the class.
     */
    private static Method getMethod( Class<?> type, Class<? extends Annotation> annotation ) throws NoSuchMethodException {
        for( Method method : type.getDeclaredMethods() ) {
            if( Modifier.isStatic( method.getModifiers() ) && method.isAnnotationPresent( annotation ) )
                return method;
        }
        throw new NoSuchMethodException( String.format( "Could not find required static @%s annotated method in %s",
                annotation.getSimpleName(), type.getName() ) );
    }
    
    /**
     * @return Pulls a static method with a specific annotation from a class, or its super class(es) if none is defined in the class.
     * @throws NoSuchMethodException if the method does not exist in the class or any of its parents.
     */
    private static Method getMethodOrSuper( Class<? extends LivingEntity> type, Class<? extends Annotation> annotation ) throws NoSuchMethodException {
        final Method method = getMethodOrSuperOptional( type, annotation );
        if( method == null ) {
            throw new NoSuchMethodException( String.format( "Could not find 'overridable' static @%s annotated method in %s or its parents",
                    annotation.getSimpleName(), type.getName() ) );
        }
        return method;
    }
    
    /**
     * @return Pulls a static method with a specific annotation from a class, or its super class(es) if none is defined in the class,
     * or null if the method does not exist.
     */
    @Nullable
    private static Method getMethodOrSuperOptional( Class<? extends LivingEntity> type, Class<? extends Annotation> annotation ) {
        Class<?> currentType = type;
        while( currentType != LivingEntity.class ) {
            for( Method method : currentType.getDeclaredMethods() ) {
                if( Modifier.isStatic( method.getModifiers() ) && method.isAnnotationPresent( annotation ) )
                    return method;
            }
            currentType = currentType.getSuperclass();
        }
        return null;
    }
    
    //    /**
    //     * @return Pulls a constructor with a specific annotation from a class.
    //     * @throws NoSuchMethodException if the constructor does not exist.
    //     */
    //    private static <T> Constructor<T> getConstructor( Class<T> type, Class<? extends Annotation> annotation ) throws NoSuchMethodException {
    //        for( Constructor<?> constructor : type.getDeclaredConstructors() ) {
    //            if( constructor.isAnnotationPresent( annotation ) )
    //                //noinspection unchecked
    //                return (Constructor<T>) constructor;
    //        }
    //        throw new NoSuchMethodException( String.format( "Could not find @%s annotated constructor in %s",
    //                annotation.getSimpleName(), type.getName() ) );
    //    }
}