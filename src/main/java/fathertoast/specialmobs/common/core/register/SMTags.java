package fathertoast.specialmobs.common.core.register;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class SMTags {
    
    // Damage types
    public static class DamageTypes {
        
        public static final TagKey<DamageType> IS_MAGIC = TagKey.create( Registries.DAMAGE_TYPE, forge( "is_magic" ) );
    }
    
    // Item tags
    public static class Items {
        
        public static final TagKey<Item> GARLIC = ItemTags.create( forge( "vegetables/garlic" ) );
    }
    
    // Entity type tags
    public static class EntityTypes {
        
        public static final TagKey<EntityType<?>> BLAZES = TagKey.create( Registries.ENTITY_TYPE, forge( "blazes" ) );
        public static final TagKey<EntityType<?>> CAVE_SPIDERS = TagKey.create( Registries.ENTITY_TYPE, forge( "cave_spiders" ) );
        public static final TagKey<EntityType<?>> CREEPERS = TagKey.create( Registries.ENTITY_TYPE, forge( "creepers" ) );
        public static final TagKey<EntityType<?>> DROWNED = TagKey.create( Registries.ENTITY_TYPE, forge( "drowned" ) );
        public static final TagKey<EntityType<?>> ENDERMEN = TagKey.create( Registries.ENTITY_TYPE, forge( "endermen" ) );
        public static final TagKey<EntityType<?>> GHASTS = TagKey.create( Registries.ENTITY_TYPE, forge( "ghasts" ) );
        public static final TagKey<EntityType<?>> MAGMA_CUBES = TagKey.create( Registries.ENTITY_TYPE, forge( "magma_cubes" ) );
        public static final TagKey<EntityType<?>> SILVERFISH = TagKey.create( Registries.ENTITY_TYPE, forge( "silverfish" ) );
        public static final TagKey<EntityType<?>> SLIMES = TagKey.create( Registries.ENTITY_TYPE, forge( "slimes" ) );
        public static final TagKey<EntityType<?>> SPIDERS = TagKey.create( Registries.ENTITY_TYPE, forge( "spiders" ) );
        public static final TagKey<EntityType<?>> WITCHES = TagKey.create( Registries.ENTITY_TYPE, forge( "witches" ) );
        public static final TagKey<EntityType<?>> WITHER_SKELETONS = TagKey.create( Registries.ENTITY_TYPE, forge( "wither_skeletons" ) );
        public static final TagKey<EntityType<?>> ZOMBIES = TagKey.create( Registries.ENTITY_TYPE, forge( "zombies" ) );
        public static final TagKey<EntityType<?>> ZOMBIFIED_PIGLINS = TagKey.create( Registries.ENTITY_TYPE, forge( "zombified_piglins" ) );
        
    }
    
    
    private static ResourceLocation forge( String path ) {
        return ResourceLocation.fromNamespaceAndPath( "forge", path );
    }
}