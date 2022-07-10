package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.projectile.CorporealShiftFireballEntity;
import fathertoast.specialmobs.common.util.AnnotationHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SMEntities {
    
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create( ForgeRegistries.ENTITIES, SpecialMobs.MOD_ID );
    
    /** Misc entities */
    public static final RegistryObject<EntityType<CorporealShiftFireballEntity>> CORPOREAL_FIREBALL = register( "corporeal_shift_fireball",
            EntityType.Builder.<CorporealShiftFireballEntity>of( CorporealShiftFireballEntity::new, EntityClassification.MISC ).sized( 1.0F, 1.0F ).clientTrackingRange( 4 ).updateInterval( 3 ) );
    
    /** Registers an entity type to the deferred register. */
    public static <T extends Entity> RegistryObject<EntityType<T>> register( String name, EntityType.Builder<T> builder ) {
        return REGISTRY.register( name, () -> builder.build( name ) );
    }
    
    /** Sets the default attributes for entity types, such as max health, attack damage etc. */
    public static void createAttributes( EntityAttributeCreationEvent event ) {
        // Bestiary-generated entities
        for( MobFamily.Species<?> species : MobFamily.getAllSpecies() ) {
            final AttributeModifierMap.MutableAttribute attributes = AnnotationHelper.createAttributes( species );
            species.config.GENERAL.attributeChanges.apply( attributes );
            event.put( species.entityType.get(), attributes.build() );
        }
    }
    
    /** Sets the natural spawn placement rules for entity types. */
    public static void registerSpawnPlacements() {
        //TODO
    }
}