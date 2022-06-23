package fathertoast.specialmobs.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.util.AnnotationHelper;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.EntityLootTables;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SMLootTableProvider extends LootTableProvider {
    
    public SMLootTableProvider( DataGenerator gen ) { super( gen ); }
    
    /** Provides all loot table sub-providers for this mod, paired with their parameter sets (context for use). */
    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return ImmutableList.of(
                Pair.of( EntitySubProvider::new, LootParameterSets.ENTITY )
        );
    }
    
    /** Validates this mod's loot tables. */
    @Override
    protected void validate( Map<ResourceLocation, LootTable> tables, ValidationTracker ctx ) {
        // We have to disable validation because vanilla entity loot tables are not recognized;
        // this is kinda scary, maybe later we can look into re-enabling validation
        //tables.forEach( ( name, table ) -> LootTableManager.validate( ctx, name, table ) );
    }
    
    /** Provides all entity loot tables for this mod. */
    public static class EntitySubProvider extends EntityLootTables {
        // Pull this protected field out into the Court of Public Opinion.
        public static final EntityPredicate.Builder ENTITY_ON_FIRE = EntityLootTables.ENTITY_ON_FIRE;
        
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