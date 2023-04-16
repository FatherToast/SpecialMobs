package fathertoast.specialmobs.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.util.AnnotationHelper;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.EntityLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SMLootTableProvider extends LootTableProvider {
    
    public SMLootTableProvider( DataGenerator gen ) { super( gen ); }
    
    /** Provides all loot table sub-providers for this mod, paired with their parameter sets (context for use). */
    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return ImmutableList.of(
                Pair.of( EntitySubProvider::new, LootContextParamSets.ENTITY )
        );
    }
    
    /** Validates this mod's loot tables. */
    @Override
    protected void validate( Map<ResourceLocation, LootTable> tables, ValidationContext ctx ) {
        // We have to disable validation because vanilla entity loot tables are not recognized;
        // this is kinda scary, maybe later we can look into re-enabling validation
        //tables.forEach( ( name, table ) -> LootTableManager.validate( ctx, name, table ) );
    }
    
    /** Provides all entity loot tables for this mod. */
    public static class EntitySubProvider extends EntityLoot {
        // Pull this protected field out into the Court of Public Opinion.
        public static final EntityPredicate.Builder ENTITY_ON_FIRE = EntityLoot.ENTITY_ON_FIRE;
        
        /** Builds all loot tables for this provider. */
        @Override
        protected void addTables() {
            // Bestiary-generated tables
            for( MobFamily.Species<?> species : MobFamily.getAllSpecies() )
                add( species.entityType.get(), AnnotationHelper.buildLootTable( species ).toLootTable() );
        }
        
        /** Supplies the entity types this loot table provider will be used for. */
        @Override
        protected Iterable<EntityType<?>> getKnownEntities() {
            // This is basically pulled straight from the forge docs on data gen for block/entity loot tables
            return SMEntities.REGISTRY.getEntries().stream().flatMap( RegistryObject::stream )::iterator;
        }
    }
}