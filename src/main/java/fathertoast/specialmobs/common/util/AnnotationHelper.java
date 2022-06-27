package fathertoast.specialmobs.common.util;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.item.Item;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;

/**
 * Provides helper methods to handle annotation processing through reflection.
 */
@SuppressWarnings( "SameParameterValue" )
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AnnotationHelper {
    
    //--------------- PRETTY HELPER METHODS ----------------
    
    /** Creates an entity factory from a special mob species. Throws an exception if anything goes wrong. */
    public static void injectSpeciesReference( MobFamily.Species<?> species ) {
        try {
            final Field field = getField( species.entityClass, SpecialMob.SpeciesReference.class );
            field.set( null, species );
        }
        catch( IllegalAccessException | NoSuchFieldException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " has invalid species reference holder", ex );
        }
    }
    
    /** Creates an entity factory from a special mob species. Throws an exception if anything goes wrong. */
    public static <T extends LivingEntity> EntityType.IFactory<T> getEntityFactory( MobFamily.Species<T> species ) {
        try {
            final Constructor<T> constructor = getConstructor( species.entityClass, SpecialMob.Constructor.class );
            return ( entityType, world ) -> {
                try {
                    return constructor.newInstance( entityType, world );
                }
                catch( InstantiationException | IllegalAccessException | InvocationTargetException ex ) {
                    throw new RuntimeException( "Caught exception during instantiation of " + constructor.getDeclaringClass().getName(), ex );
                }
            };
        }
        catch( NoSuchMethodException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " has no valid constructors", ex );
        }
    }
    
    /** Gets bestiary info from a special mob species. Throws an exception if anything goes wrong. */
    public static <T extends LivingEntity> BestiaryInfo getBestiaryInfo( MobFamily.Species<T> species, EntityType.Builder<T> entityType ) {
        try {
            return (BestiaryInfo) getMethod( species.entityClass, SpecialMob.BestiaryInfoSupplier.class ).invoke( null, entityType );
        }
        catch( IllegalAccessException | NoSuchMethodException | InvocationTargetException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " has invalid bestiary info method", ex );
        }
    }
    
    /** Creates an attribute modifier map from a special mob species. Throws an exception if anything goes wrong. */
    public static AttributeModifierMap createAttributes( MobFamily.Species<?> species ) {
        try {
            return ((AttributeModifierMap.MutableAttribute) getMethodOrSuper( species.entityClass, SpecialMob.AttributeCreator.class )
                    .invoke( null )).build();
        }
        catch( NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " has invalid attribute creation method", ex );
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
    
    /** Gets the translations from a mod item. Throws an exception if anything goes wrong. */
    public static String[] getTranslations( Item item ) {
        try {
            return (String[]) getMethod( item.getClass(), SpecialMob.LanguageProvider.class )
                    .invoke( null, item.getDescriptionId() );
        }
        catch( NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ) {
            throw new RuntimeException( "Item class for " + item.getRegistryName() + " has invalid language provider method", ex );
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
        Class<?> currentType = type;
        while( currentType != LivingEntity.class ) {
            for( Method method : currentType.getDeclaredMethods() ) {
                if( Modifier.isStatic( method.getModifiers() ) && method.isAnnotationPresent( annotation ) )
                    return method;
            }
            currentType = currentType.getSuperclass();
        }
        throw new NoSuchMethodException( String.format( "Could not find 'overridable' static @%s annotated method in %s or its parents",
                annotation.getSimpleName(), type.getName() ) );
    }
    
    /**
     * @return Pulls a constructor with a specific annotation from a class.
     * @throws NoSuchMethodException if the constructor does not exist.
     */
    private static <T> Constructor<T> getConstructor( Class<T> type, Class<? extends Annotation> annotation ) throws NoSuchMethodException {
        for( Constructor<?> constructor : type.getConstructors() ) {
            if( constructor.isAnnotationPresent( annotation ) )
                //noinspection unchecked
                return (Constructor<T>) constructor;
        }
        throw new NoSuchMethodException( String.format( "Could not find @%s annotated constructor in %s",
                annotation.getSimpleName(), type.getName() ) );
    }
}