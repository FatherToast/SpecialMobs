package fathertoast.specialmobs.loot;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.KilledByPlayer;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting;
import net.minecraft.world.storage.loot.functions.LootFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings( { "unused", "WeakerAccess", "UnusedReturnValue" } )
public
class LootPoolBuilder
{
	public static final LootCondition[] NO_CONDITIONS = { };
	public static final LootFunction[]  NO_FUNCTIONS  = { };
	
	public static final LootCondition KILLED_BY_PLAYER_CONDITION = new KilledByPlayer( false );
	
	public static final LootCondition[] UNCOMMON_CONDITIONS = {
		new RandomChanceWithLooting( 0.25F, 0.1F ),
		KILLED_BY_PLAYER_CONDITION
	};
	public static final LootCondition[] RARE_CONDITIONS     = {
		new RandomChanceWithLooting( 0.025F, 0.01F ),
		KILLED_BY_PLAYER_CONDITION
	};
	public static final LootCondition[] EPIC_CONDITIONS     = {
		new RandomChanceWithLooting( 0.0F, 0.015F ),
		KILLED_BY_PLAYER_CONDITION
	};
	
	public static final RandomValueRange ONE_ROLL = new RandomValueRange( 1 );
	public static final RandomValueRange NO_ROLL  = new RandomValueRange( 0 );
	
	private final String name;
	
	private RandomValueRange rolls      = ONE_ROLL;
	private RandomValueRange bonusRolls = NO_ROLL;
	
	private final List< LootEntry >     lootEntries    = new ArrayList<>( );
	private final List< LootCondition > poolConditions = new ArrayList<>( );
	
	public
	LootPoolBuilder( String id ) { name = id; }
	
	/** @return A new loot pool object reflecting the current state of this builder. */
	public
	LootPool toLootPool( ) { return new LootPool( lootEntries.toArray( new LootEntry[ 0 ] ), poolConditions.toArray( NO_CONDITIONS ), rolls, bonusRolls, name ); }
	
	/** @param value The number of rolls for this pool. */
	public
	LootPoolBuilder setRolls( float value )
	{
		rolls = new RandomValueRange( value );
		return this;
	}
	
	/**
	 * @param min Minimum rolls for this pool (inclusive).
	 * @param max Maximum rolls for this pool (inclusive).
	 */
	public
	LootPoolBuilder setRolls( float min, float max )
	{
		rolls = new RandomValueRange( min, max );
		return this;
	}
	
	/** @param value The additional rolls for each level of looting. */
	public
	LootPoolBuilder setBonusRolls( float value )
	{
		bonusRolls = new RandomValueRange( value );
		return this;
	}
	
	/**
	 * @param min Minimum additional rolls for this pool for each level of looting (inclusive).
	 * @param max Maximum additional rolls for this pool for each level of looting (inclusive).
	 */
	public
	LootPoolBuilder setBonusRolls( float min, float max )
	{
		bonusRolls = new RandomValueRange( min, max );
		return this;
	}
	
	/** @param conditions Any number of conditions to add to this builder. */
	public
	LootPoolBuilder addConditions( LootCondition... conditions )
	{
		poolConditions.addAll( Arrays.asList( conditions ) );
		return this;
	}
	
	/** @param entry A loot entry to add to this builder. */
	public
	LootPoolBuilder addEntry( LootEntry entry )
	{
		lootEntries.add( entry );
		return this;
	}
	
	/** Adds a loot table as a drop. */
	public
	LootPoolBuilder addEntryTable( String name, ResourceLocation lootTable )
	{
		return addEntryTable( name, lootTable, 1, 0, NO_CONDITIONS );
	}
	
	/** Adds a loot table as a drop. */
	public
	LootPoolBuilder addEntryTable( String name, ResourceLocation lootTable, int weight, int quality )
	{
		return addEntryTable( name, lootTable, weight, quality, NO_CONDITIONS );
	}
	
	/** Adds a loot table as a drop. */
	public
	LootPoolBuilder addEntryTable( String name, ResourceLocation lootTable, int weight, int quality, LootCondition[] conditions )
	{
		return addEntry( new LootEntryTable( lootTable, weight, quality, conditions, name ) );
	}
	
	/** Adds an empty entry. */
	public
	LootPoolBuilder addEntryEmpty( String name, int weight, int quality )
	{
		return addEntryEmpty( name, weight, quality, NO_CONDITIONS );
	}
	
	/** Adds an empty entry. */
	public
	LootPoolBuilder addEntryEmpty( String name, int weight, int quality, LootCondition[] conditions )
	{
		return addEntry( new LootEntryEmpty( weight, quality, conditions, name ) );
	}
}
