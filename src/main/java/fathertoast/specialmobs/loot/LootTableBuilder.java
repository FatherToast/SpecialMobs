package fathertoast.specialmobs.loot;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings( { "unused", "WeakerAccess", "UnusedReturnValue" } )
public
class LootTableBuilder
{
	private final List< LootPool > pools = new ArrayList<>( );
	
	/** @return A new loot table object reflecting the current state of this builder. */
	public
	LootTable toLootTable( ) { return new LootTable( pools.toArray( new LootPool[ 0 ] ) ); }
	
	/** @param pool A loot pool to add to this builder. */
	public
	LootTableBuilder addPool( LootPool pool )
	{
		pools.add( pool );
		return this;
	}
	
	/** Adds a pool referencing a loot table. */
	public
	LootTableBuilder addLootTable( String id, String name, ResourceLocation lootTable )
	{
		return addPool( new LootPoolBuilder( id ).addEntryTable( name, lootTable ).toLootPool( ) );
	}
	
	/** Adds a pool with a single item drop. */
	public
	LootTableBuilder addGuaranteedDrop( String id, String name, Block block, int count )
	{
		return addGuaranteedDrop( id, name, Item.getItemFromBlock( block ), count );
	}
	
	/** Adds a pool with a single item drop. */
	public
	LootTableBuilder addGuaranteedDrop( String id, String name, Item item, int count )
	{
		return addPool(
			new LootPoolBuilder( id )
				.addEntry( new LootEntryItemBuilder( name, item ).setCount( count ).toLootEntry( ) )
				.toLootPool( )
		);
	}
	
	/** Adds a pool with an item drop of 0-2 + (0-1 * luck). */
	public
	LootTableBuilder addCommonDrop( String id, String name, Block block )
	{
		return addCommonDrop( id, name, Item.getItemFromBlock( block ) );
	}
	
	/** Adds a pool with an item drop of 0-2 + (0-1 * luck). */
	public
	LootTableBuilder addCommonDrop( String id, String name, Item item )
	{
		return addCommonDrop( id, name, item, 2 );
	}
	
	/** Adds a pool with an item drop of 0-2 + (0-1 * luck). */
	public
	LootTableBuilder addCommonDrop( String id, String name, ItemStack item )
	{
		return addCommonDrop( id, name, item, 2 );
	}
	
	/** Adds a pool with an item drop of 0-max + (0-1 * luck). */
	public
	LootTableBuilder addCommonDrop( String id, String name, Block block, int max )
	{
		return addCommonDrop( id, name, Item.getItemFromBlock( block ), max );
	}
	
	/** Adds a pool with an item drop of 0-max + (0-1 * luck). */
	public
	LootTableBuilder addCommonDrop( String id, String name, Item item, int max )
	{
		return addPool(
			new LootPoolBuilder( id )
				.addEntry( new LootEntryItemBuilder( name, item ).setCount( 0, max ).addLootingBonus( 0, 1 ).toLootEntry( ) )
				.toLootPool( )
		);
	}
	
	/** Adds a pool with an item drop of 0-max + (0-1 * luck). */
	public
	LootTableBuilder addCommonDrop( String id, String name, ItemStack item, int max )
	{
		return addPool(
			new LootPoolBuilder( id )
				.addEntry( new LootEntryItemBuilder( name, item ).setCount( 0, max ).addLootingBonus( 0, 1 ).toLootEntry( ) )
				.toLootPool( )
		);
	}
	
	/** Adds a pool with an item drop of (-1)-1 + (0-1 * luck). */
	public
	LootTableBuilder addSemicommonDrop( String id, String name, Block block )
	{
		return addSemicommonDrop( id, name, Item.getItemFromBlock( block ) );
	}
	
	/** Adds a pool with an item drop of (-1)-1 + (0-1 * luck). */
	public
	LootTableBuilder addSemicommonDrop( String id, String name, Item item )
	{
		return addPool(
			new LootPoolBuilder( id )
				.addConditions( LootPoolBuilder.KILLED_BY_PLAYER_CONDITION )
				.addEntry( new LootEntryItemBuilder( name, item ).setCount( -1, 1 ).addLootingBonus( 0, 1 ).toLootEntry( ) )
				.toLootPool( )
		);
	}
	
	/** Adds a pool with an item drop of (-1)-1 + (0-1 * luck). */
	public
	LootTableBuilder addSemicommonDrop( String id, String name, ItemStack item )
	{
		return addPool(
			new LootPoolBuilder( id )
				.addConditions( LootPoolBuilder.KILLED_BY_PLAYER_CONDITION )
				.addEntry( new LootEntryItemBuilder( name, item ).setCount( -1, 1 ).addLootingBonus( 0, 1 ).toLootEntry( ) )
				.toLootPool( )
		);
	}
	
	/** Adds a pool with an item drop of 1-8 + (0-2 * luck) and chance of 25% + (10% * luck). */
	public
	LootTableBuilder addClusterDrop( String id, String name, Block block )
	{
		return addClusterDrop( id, name, Item.getItemFromBlock( block ) );
	}
	
	/** Adds a pool with an item drop of 1-8 + (0-2 * luck) and chance of 25% + (10% * luck). */
	public
	LootTableBuilder addClusterDrop( String id, String name, Item item )
	{
		return addClusterDrop( id, name, item, 8 );
	}
	
	/** Adds a pool with an item drop of 1-8 + (0-2 * luck) and chance of 25% + (10% * luck). */
	public
	LootTableBuilder addClusterDrop( String id, String name, ItemStack item )
	{
		return addClusterDrop( id, name, item, 8 );
	}
	
	/** Adds a pool with an item drop of 1-max + (0-(max/4) * luck) and chance of 25% + (10% * luck). */
	public
	LootTableBuilder addClusterDrop( String id, String name, Block block, int max )
	{
		return addClusterDrop( id, name, Item.getItemFromBlock( block ), max );
	}
	
	/** Adds a pool with an item drop of 1-max + (0-(max/4) * luck) and chance of 25% + (10% * luck). */
	public
	LootTableBuilder addClusterDrop( String id, String name, Item item, int max )
	{
		return addPool(
			new LootPoolBuilder( id )
				.addConditions( LootPoolBuilder.UNCOMMON_CONDITIONS )
				.addEntry( new LootEntryItemBuilder( name, item ).setCount( 1, max ).addLootingBonus( 0, max / 4.0F ).toLootEntry( ) )
				.toLootPool( )
		);
	}
	
	/** Adds a pool with an item drop of 1-max + (0-(max/4) * luck) and chance of 25% + (10% * luck). */
	public
	LootTableBuilder addClusterDrop( String id, String name, ItemStack item, int max )
	{
		return addPool(
			new LootPoolBuilder( id )
				.addConditions( LootPoolBuilder.UNCOMMON_CONDITIONS )
				.addEntry( new LootEntryItemBuilder( name, item ).setCount( 1, max ).addLootingBonus( 0, max / 4.0F ).toLootEntry( ) )
				.toLootPool( )
		);
	}
	
	/** Adds a pool with a single item drop (from a list) and chance of 25% + (10% * luck). */
	public
	LootTableBuilder addUncommonDrop( String id, String name, Block... blocks )
	{
		return addUncommonDrop( id, name, toItemArray( blocks ) );
	}
	
	/** Adds a pool with a single item drop (from a list) and chance of 25% + (10% * luck). */
	public
	LootTableBuilder addUncommonDrop( String id, String name, Item... items )
	{
		LootPoolBuilder pool = new LootPoolBuilder( id ).addConditions( LootPoolBuilder.UNCOMMON_CONDITIONS );
		for( int i = 0; i < items.length; i++ ) {
			pool.addEntry( new LootEntryItemBuilder( name + " " + (i + 1), items[ i ] ).toLootEntry( ) );
		}
		return addPool( pool.toLootPool( ) );
	}
	
	/** Adds a pool with a single item drop (from a list) and chance of 25% + (10% * luck). Item stack size is used as the weight of each item. */
	public
	LootTableBuilder addUncommonDrop( String id, String name, ItemStack... items )
	{
		LootPoolBuilder pool = new LootPoolBuilder( id ).addConditions( LootPoolBuilder.UNCOMMON_CONDITIONS );
		for( int i = 0; i < items.length; i++ ) {
			pool.addEntry( new LootEntryItemBuilder( name + " " + (i + 1), items[ i ] ).setWeight( items[ i ].getCount( ) ).toLootEntry( ) );
		}
		return addPool( pool.toLootPool( ) );
	}
	
	/** Adds a pool with a single item drop (from a list) and chance of 2.5% + (1% * luck). */
	public
	LootTableBuilder addRareDrop( String id, String name, Block... blocks )
	{
		return addRareDrop( id, name, toItemArray( blocks ) );
	}
	
	/** Adds a pool with a single item drop (from a list) and chance of 2.5% + (1% * luck). */
	public
	LootTableBuilder addRareDrop( String id, String name, Item... items )
	{
		LootPoolBuilder pool = new LootPoolBuilder( id ).addConditions( LootPoolBuilder.RARE_CONDITIONS );
		for( int i = 0; i < items.length; i++ ) {
			pool.addEntry( new LootEntryItemBuilder( name + " " + (i + 1), items[ i ] ).toLootEntry( ) );
		}
		return addPool( pool.toLootPool( ) );
	}
	
	/** Adds a pool with a single item drop (from a list) and chance of 2.5% + (1% * luck). Item stack size is used as the weight of each item. */
	public
	LootTableBuilder addRareDrop( String id, String name, ItemStack... items )
	{
		LootPoolBuilder pool = new LootPoolBuilder( id ).addConditions( LootPoolBuilder.RARE_CONDITIONS );
		for( int i = 0; i < items.length; i++ ) {
			pool.addEntry( new LootEntryItemBuilder( name + " " + (i + 1), items[ i ] ).setWeight( items[ i ].getCount( ) ).toLootEntry( ) );
		}
		return addPool( pool.toLootPool( ) );
	}
	
	/** Adds a pool with a single item drop (from a list) and chance of 0% + (1.5% * luck). */
	public
	LootTableBuilder addEpicDrop( String id, String name, Block... blocks )
	{
		return addEpicDrop( id, name, toItemArray( blocks ) );
	}
	
	/** Adds a pool with a single item drop (from a list) and chance of 0% + (1.5% * luck). */
	public
	LootTableBuilder addEpicDrop( String id, String name, Item... items )
	{
		LootPoolBuilder pool = new LootPoolBuilder( id ).addConditions( LootPoolBuilder.EPIC_CONDITIONS );
		for( int i = 0; i < items.length; i++ ) {
			pool.addEntry( new LootEntryItemBuilder( name + " " + (i + 1), items[ i ] ).toLootEntry( ) );
		}
		return addPool( pool.toLootPool( ) );
	}
	
	/** Adds a pool with a single item drop (from a list) and chance of 0% + (1.5% * luck). Item stack size is used as the weight of each item. */
	public
	LootTableBuilder addEpicDrop( String id, String name, ItemStack... items )
	{
		LootPoolBuilder pool = new LootPoolBuilder( id ).addConditions( LootPoolBuilder.EPIC_CONDITIONS );
		for( int i = 0; i < items.length; i++ ) {
			pool.addEntry( new LootEntryItemBuilder( name + " " + (i + 1), items[ i ] ).setWeight( items[ i ].getCount( ) ).toLootEntry( ) );
		}
		return addPool( pool.toLootPool( ) );
	}
	
	private static
	Item[] toItemArray( Block[] blocks )
	{
		Item[] items = new Item[ blocks.length ];
		for( int i = 0; i < blocks.length; i++ ) {
			items[ i ] = Item.getItemFromBlock( blocks[ i ] );
		}
		return items;
	}
}
