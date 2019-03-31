package fathertoast.specialmobs.loot;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.EntityHasProperty;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.*;
import net.minecraft.world.storage.loot.properties.EntityOnFire;
import net.minecraft.world.storage.loot.properties.EntityProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fathertoast.specialmobs.loot.LootPoolBuilder.*;

@SuppressWarnings( { "unused", "WeakerAccess", "UnusedReturnValue" } )
public
class LootEntryItemBuilder
{
	private final String name;
	
	private Item item;
	private int  weight  = 1;
	private int  quality = 0;
	
	private final List< LootFunction >  itemFunctions   = new ArrayList<>( );
	private final List< LootCondition > entryConditions = new ArrayList<>( );
	
	public
	LootEntryItemBuilder( String id, Item baseItem )
	{
		name = id;
		item = baseItem;
	}
	
	public
	LootEntryItemBuilder( String id, ItemStack itemStack )
	{
		this( id, itemStack.getItem( ) );
		if( itemStack.getTagCompound( ) != null ) {
			setNBTTag( itemStack.getTagCompound( ).copy( ) );
		}
		if( !itemStack.isItemStackDamageable( ) && itemStack.getMetadata( ) != 0 ) {
			setMetadata( itemStack.getMetadata( ) );
		}
	}
	
	/** @return A new loot entry object reflecting the current state of this builder. */
	public
	LootEntryItem toLootEntry( ) { return new LootEntryItem( item, weight, quality, itemFunctions.toArray( NO_FUNCTIONS ), entryConditions.toArray( NO_CONDITIONS ), name ); }
	
	/** @param value A new weight for the loot entry. */
	public
	LootEntryItemBuilder setWeight( int value )
	{
		weight = value;
		return this;
	}
	
	/** @param value A new quality for the loot entry. Quality alters the weight of this entry based on luck level. */
	public
	LootEntryItemBuilder setQuality( int value )
	{
		quality = value;
		return this;
	}
	
	/** @param condition A condition to add to this builder. */
	public
	LootEntryItemBuilder addCondition( LootCondition condition )
	{
		entryConditions.add( condition );
		return this;
	}
	
	/** Adds a stack size function. */
	public
	LootEntryItemBuilder setCount( int value )
	{
		return addFunction( new SetCount( NO_CONDITIONS, new RandomValueRange( value ) ) );
	}
	
	/** Adds a stack size function. */
	public
	LootEntryItemBuilder setCount( int min, int max )
	{
		return addFunction( new SetCount( NO_CONDITIONS, new RandomValueRange( min, max ) ) );
	}
	
	/** Adds a looting enchant (luck) bonus function. */
	public
	LootEntryItemBuilder addLootingBonus( float value )
	{
		return addFunction( new LootingEnchantBonus( NO_CONDITIONS, new RandomValueRange( value ), 0 ) );
	}
	
	/** Adds a looting enchant (luck) bonus function. */
	public
	LootEntryItemBuilder addLootingBonus( float min, float max )
	{
		return addLootingBonus( min, max, 0 );
	}
	
	/** Adds a looting enchant (luck) bonus function. */
	public
	LootEntryItemBuilder addLootingBonus( float min, float max, int limit )
	{
		return addFunction( new LootingEnchantBonus( NO_CONDITIONS, new RandomValueRange( min, max ), limit ) );
	}
	
	/** Adds a set damage function. */
	public
	LootEntryItemBuilder setDamage( int value )
	{
		return addFunction( new SetDamage( NO_CONDITIONS, new RandomValueRange( value ) ) );
	}
	
	/** Adds a set metadata function. */
	public
	LootEntryItemBuilder setMetadata( int min, int max )
	{
		return addFunction( new SetMetadata( NO_CONDITIONS, new RandomValueRange( min, max ) ) );
	}
	
	/** Adds a set metadata function. */
	public
	LootEntryItemBuilder setMetadata( int value )
	{
		return addFunction( new SetMetadata( NO_CONDITIONS, new RandomValueRange( value ) ) );
	}
	
	/** Adds a set damage function. */
	public
	LootEntryItemBuilder setDamage( int min, int max )
	{
		return addFunction( new SetDamage( NO_CONDITIONS, new RandomValueRange( min, max ) ) );
	}
	
	/** Adds an nbt tag compound function. */
	public
	LootEntryItemBuilder setNBTTag( NBTTagCompound tag )
	{
		return addFunction( new SetNBT( NO_CONDITIONS, tag ) );
	}
	
	/** Adds a smelt function with the EntityOnFire condition. */
	public
	LootEntryItemBuilder smeltIfBurning( )
	{
		return addFunction( new Smelt( new LootCondition[] {
			new EntityHasProperty( new EntityProperty[] { new EntityOnFire( true ) }, LootContext.EntityTarget.THIS )
		} ) );
	}
	
	/** Adds a random enchantment function. */
	public
	LootEntryItemBuilder applyOneRandomApplicableEnchant( )
	{
		return addFunction( new EnchantRandomly( NO_CONDITIONS, null ) );
	}
	
	/** Adds a random enchantment function. */
	public
	LootEntryItemBuilder applyOneRandomEnchant( Enchantment... enchantments )
	{
		return addFunction( new EnchantRandomly( NO_CONDITIONS, Arrays.asList( enchantments ) ) );
	}
	
	/** Adds an enchanting function. */
	public
	LootEntryItemBuilder enchant( int level, boolean treasure )
	{
		return addFunction( new EnchantWithLevels( NO_CONDITIONS, new RandomValueRange( level ), treasure ) );
	}
	
	/** Adds an enchanting function. */
	public
	LootEntryItemBuilder enchant( int levelMin, int levelMax, boolean treasure )
	{
		return addFunction( new EnchantWithLevels( NO_CONDITIONS, new RandomValueRange( levelMin, levelMax ), treasure ) );
	}
	
	/** Adds an item function to this builder. */
	public
	LootEntryItemBuilder addFunction( LootFunction function )
	{
		itemFunctions.add( function );
		return this;
	}
}
