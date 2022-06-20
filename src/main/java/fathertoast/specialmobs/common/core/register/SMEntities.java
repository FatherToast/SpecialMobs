package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.util.AnnotationHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.InvocationTargetException;

public class SMEntities {
    
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create( ForgeRegistries.ENTITIES, SpecialMobs.MOD_ID );
    
    /** Registers an entity type to the deferred register. */
    public static <T extends Entity> RegistryObject<EntityType<T>> register( String name, EntityType.Builder<T> builder ) {
        return REGISTRY.register( name, () -> builder.build( name ) );
    }
    
    /** Sets the default attributes for entity types, such as max health, attack damage etc. */
    public static void createAttributes( EntityAttributeCreationEvent event ) {
        // Bestiary-generated entities
        for( MobFamily.Species<?> variant : MobFamily.getAllSpecies() )
            createSpeciesAttributes( event, variant );
    }
    
    /** Builds the attributes for a specific entity species. */
    private static void createSpeciesAttributes( EntityAttributeCreationEvent event, MobFamily.Species<?> species ) {
        try {
            event.put( species.entityType.get(), AnnotationHelper.createAttributes( species ) );
        }
        catch( NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ) {
            throw new RuntimeException( "Entity class for " + species.name + " has invalid attribute creation method", ex );
        }
    }
    
    /** Sets the natural spawn placement rules for entity types. */
    public static void registerSpawnPlacements() {
        //TODO
    }
}