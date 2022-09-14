package fathertoast.specialmobs.datagen.loot;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for the vanilla loot table builder, largely a holdover from before the vanilla loot table builders existed,
 * but still offers many convenience methods for simple and standardized loot table construction.
 */
@SuppressWarnings( { "unused", "WeakerAccess", "UnusedReturnValue" } )
public class LootTableBuilder {
    
    private final List<LootPool.Builder> pools = new ArrayList<>();
    //private final List<ILootFunction.IBuilder> tableFunctions = new ArrayList<>(); - Can be implemented if needed
    
    /** @return A new loot table object reflecting the current state of this builder. */
    public LootTable.Builder toLootTable() { return LootHelper.build( LootTable.lootTable(), pools ); }
    
    /** @param pool A loot pool to add to this builder. */
    public LootTableBuilder addPool( LootPool.Builder pool ) {
        pools.add( pool );
        return this;
    }
    
    /** Adds a pool referencing a loot table. */
    public LootTableBuilder addLootTable( String id, ResourceLocation lootTable ) {
        return addPool( new LootPoolBuilder( id ).addEntryTable( lootTable ).toLootPool() );
    }
    
    /** Adds a pool with a single item drop. */
    public LootTableBuilder addGuaranteedDrop( String id, IItemProvider item, int count ) {
        return addPool( new LootPoolBuilder( id )
                .addEntry( new LootEntryItemBuilder( item ).setCount( count ).toLootEntry() )
                .toLootPool() );
    }
    
    /** Adds a pool with an item drop of 0-2 + (0-1 * luck). */
    public LootTableBuilder addCommonDrop( String id, IItemProvider item ) { return addCommonDrop( id, item, 2 ); }
    
    /** Adds a pool with an item drop of 0-2 + (0-1 * luck). */
    public LootTableBuilder addCommonDrop( String id, ItemStack item ) { return addCommonDrop( id, item, 2 ); }
    
    /** Adds a pool with an item drop of 0-max + (0-1 * luck). */
    public LootTableBuilder addCommonDrop( String id, IItemProvider item, int max ) {
        return addPool( new LootPoolBuilder( id )
                .addEntry( new LootEntryItemBuilder( item ).setCount( 0, max ).addLootingBonus( 0, 1 ).toLootEntry() )
                .toLootPool() );
    }
    
    /** Adds a pool with an item drop of 0-max + (0-1 * luck). */
    public LootTableBuilder addCommonDrop( String id, ItemStack item, int max ) {
        return addPool( new LootPoolBuilder( id )
                .addEntry( new LootEntryItemBuilder( item ).setCount( 0, max ).addLootingBonus( 0, 1 ).toLootEntry() )
                .toLootPool() );
    }
    
    /** Adds a pool with an item drop of (-1)-1 + (0-1 * luck). */
    public LootTableBuilder addSemicommonDrop( String id, IItemProvider item ) {
        return addPool( new LootPoolBuilder( id )
                .addConditions( LootHelper.KILLED_BY_PLAYER_CONDITION )
                .addEntry( new LootEntryItemBuilder( item ).setCount( -1, 1 ).addLootingBonus( 0, 1 ).toLootEntry() )
                .toLootPool() );
    }
    
    /** Adds a pool with an item drop of (-1)-1 + (0-1 * luck). */
    public LootTableBuilder addSemicommonDrop( String id, ItemStack item ) {
        return addPool( new LootPoolBuilder( id )
                .addConditions( LootHelper.KILLED_BY_PLAYER_CONDITION )
                .addEntry( new LootEntryItemBuilder( item ).setCount( -1, 1 ).addLootingBonus( 0, 1 ).toLootEntry() )
                .toLootPool() );
    }
    
    /** Adds a pool with an item drop of 1-8 and chance of 25% + (5% * luck). */
    public LootTableBuilder addClusterDrop( String id, IItemProvider item ) { return addClusterDrop( id, item, 8 ); }
    
    /** Adds a pool with an item drop of 1-8 and chance of 25% + (5% * luck). */
    public LootTableBuilder addClusterDrop( String id, ItemStack item ) { return addClusterDrop( id, item, 8 ); }
    
    /** Adds a pool with an item drop of 1-max and chance of 25% + (5% * luck). */
    public LootTableBuilder addClusterDrop( String id, IItemProvider item, int max ) {
        return addPool( new LootPoolBuilder( id )
                .addConditions( LootHelper.UNCOMMON_CONDITIONS )
                .addEntry( new LootEntryItemBuilder( item ).setCount( 1, max ).toLootEntry() )
                .toLootPool() );
    }
    
    /** Adds a pool with an item drop of 1-max and chance of 25% + (5% * luck). */
    public LootTableBuilder addClusterDrop( String id, ItemStack item, int max ) {
        return addPool( new LootPoolBuilder( id )
                .addConditions( LootHelper.UNCOMMON_CONDITIONS )
                .addEntry( new LootEntryItemBuilder( item ).setCount( 1, max ).toLootEntry() )
                .toLootPool() );
    }
    
    /** Adds a pool with a single item drop (from a list) and chance of 25% + (5% * luck). */
    public LootTableBuilder addUncommonDrop( String id, IItemProvider... items ) {
        LootPoolBuilder pool = new LootPoolBuilder( id ).addConditions( LootHelper.UNCOMMON_CONDITIONS );
        for( IItemProvider item : items ) {
            pool.addEntry( new LootEntryItemBuilder( item ).toLootEntry() );
        }
        return addPool( pool.toLootPool() );
    }
    
    /**
     * Adds a pool with a single item drop (from a list) and chance of 25% + (5% * luck).
     * Item stack size is used as the weight of each item.
     */
    public LootTableBuilder addUncommonDrop( String id, ItemStack... items ) {
        LootPoolBuilder pool = new LootPoolBuilder( id ).addConditions( LootHelper.UNCOMMON_CONDITIONS );
        for( ItemStack item : items ) {
            pool.addEntry( new LootEntryItemBuilder( item ).setWeight( item.getCount() ).toLootEntry() );
        }
        return addPool( pool.toLootPool() );
    }
    
    /** Adds a pool with a single item drop (from a list) and chance of 2.5% + (0% * luck). */
    public LootTableBuilder addRareDrop( String id, IItemProvider... items ) {
        LootPoolBuilder pool = new LootPoolBuilder( id ).addConditions( LootHelper.RARE_CONDITIONS );
        for( IItemProvider item : items ) {
            pool.addEntry( new LootEntryItemBuilder( item ).toLootEntry() );
        }
        return addPool( pool.toLootPool() );
    }
    
    /**
     * Adds a pool with a single item drop (from a list) and chance of 2.5% + (0% * luck).
     * Item stack size is used as the weight of each item.
     */
    public LootTableBuilder addRareDrop( String id, ItemStack... items ) {
        LootPoolBuilder pool = new LootPoolBuilder( id ).addConditions( LootHelper.RARE_CONDITIONS );
        for( ItemStack item : items ) {
            pool.addEntry( new LootEntryItemBuilder( item ).setWeight( item.getCount() ).toLootEntry() );
        }
        return addPool( pool.toLootPool() );
    }
}