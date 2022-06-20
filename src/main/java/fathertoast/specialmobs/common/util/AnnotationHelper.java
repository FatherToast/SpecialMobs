package fathertoast.specialmobs.common.util;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@ParametersAreNonnullByDefault
public final class AnnotationHelper {
    
    //--------------- PRETTY HELPER METHODS ----------------
    
    /** Creates an entity factory from a special mob species. Throws an exception if anything goes wrong. */
    public static <T extends LivingEntity> EntityType.IFactory<T> getEntityFactory( MobFamily.Species<T> species )
            throws NoSuchMethodException {
        final Constructor<T> constructor = getConstructor( species.entityClass, SpecialMob.Constructor.class );
        return ( entityType, world ) -> {
            try {
                return constructor.newInstance( entityType, world );
            }
            catch( InstantiationException | IllegalAccessException | InvocationTargetException ex ) {
                throw new RuntimeException( "Class for " + constructor.getDeclaringClass().getName() + " has invalid constructor", ex );
            }
        };
    }
    
    /** Gets bestiary info from a special mob species. Throws an exception if anything goes wrong. */
    public static <T extends LivingEntity> BestiaryInfo getBestiaryInfo( MobFamily.Species<T> species, EntityType.Builder<T> entityType )
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return (BestiaryInfo) getMethod( species.entityClass, SpecialMob.BestiaryInfoSupplier.class ).invoke( null, entityType );
    }
    
    /** Creates an attribute modifier map from a special mob species. Throws an exception if anything goes wrong. */
    public static AttributeModifierMap createAttributes( MobFamily.Species<?> species )
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return ((AttributeModifierMap.MutableAttribute) getMethod( species.entityClass, SpecialMob.AttributeCreator.class )
                .invoke( null )).build();
    }
    
    /** Gets the translations from a special mob species. Throws an exception if anything goes wrong. */
    public static String[] getTranslations( MobFamily.Species<?> species )
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return (String[]) getMethod( species.entityClass, SpecialMob.LanguageProvider.class )
                .invoke( null, species.entityType.get().getDescriptionId() );
    }
    
    /** Builds a loot table from a special mob species. Throws an exception if anything goes wrong. */
    public static LootTableBuilder buildLootTable( MobFamily.Species<?> species )
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final LootTableBuilder builder = new LootTableBuilder();
        getMethod( species.entityClass, SpecialMob.LootTableProvider.class ).invoke( null, builder );
        return builder;
    }
    
    
    //--------------- RAW ANNOTATION METHODS ----------------
    
    /**
     * @return Pulls a static method with a specific annotation from a class.
     * @throws NoSuchMethodException if the method does not exist.
     */
    private static Method getMethod( Class<?> type, Class<? extends Annotation> annotation ) throws NoSuchMethodException {
        final Method method = getMethodOptional( type, annotation );
        if( method == null ) {
            throw new NoSuchMethodException( String.format( "Could not find static @%s annotated method in %s",
                    annotation.getSimpleName(), type.getName() ) );
        }
        return method;
    }
    
    /**
     * @return Pulls a static method with a specific annotation from a class, or null if the method does not exist.
     */
    private static Method getMethodOptional( Class<?> type, Class<? extends Annotation> annotation ) {
        for( Method method : type.getMethods() ) {
            if( Modifier.isStatic( method.getModifiers() ) && method.isAnnotationPresent( annotation ) )
                return method;
        }
        return null;
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