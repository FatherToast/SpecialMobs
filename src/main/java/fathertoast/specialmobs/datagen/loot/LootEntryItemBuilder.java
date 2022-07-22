package fathertoast.specialmobs.datagen.loot;

import fathertoast.specialmobs.datagen.SMLootTableProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.conditions.EntityHasProperty;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IItemProvider;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings( { "unused", "WeakerAccess", "UnusedReturnValue" } )
public class LootEntryItemBuilder {
    
    private final IItemProvider item;
    
    private int weight = 1;
    private int quality = 0;
    
    private final List<ILootFunction.IBuilder> itemFunctions = new ArrayList<>();
    private final List<ILootCondition.IBuilder> entryConditions = new ArrayList<>();
    
    public LootEntryItemBuilder( IItemProvider baseItem ) {
        item = baseItem;
    }
    
    public LootEntryItemBuilder( ItemStack itemStack ) {
        this( itemStack.getItem() );
        if( itemStack.getTag() != null ) {
            setNBTTag( itemStack.getTag().copy() );
        }
    }
    
    /** @return A new loot entry object reflecting the current state of this builder. */
    public LootEntry.Builder<?> toLootEntry() {
        return LootHelper.build( ItemLootEntry.lootTableItem( item ), entryConditions, itemFunctions )
                .setWeight( weight ).setQuality( quality );
    }
    
    /** @param value A new weight for the loot entry. */
    public LootEntryItemBuilder setWeight( int value ) {
        weight = value;
        return this;
    }
    
    /** @param value A new quality for the loot entry. Quality alters the weight of this entry based on luck level. */
    public LootEntryItemBuilder setQuality( int value ) {
        quality = value;
        return this;
    }
    
    /** @param condition A condition to add to this builder. */
    public LootEntryItemBuilder addCondition( ILootCondition.IBuilder condition ) {
        entryConditions.add( condition );
        return this;
    }
    
    /** Adds a stack size function. */
    public LootEntryItemBuilder setCount( int value ) {
        return addFunction( SetCount.setCount( new RandomValueRange( value ) ) );
    }
    
    /** Adds a stack size function. */
    public LootEntryItemBuilder setCount( int min, int max ) {
        return addFunction( SetCount.setCount( new RandomValueRange( min, max ) ) );
    }
    
    /** Adds a looting enchant (luck) bonus function. Gross. */
    public LootEntryItemBuilder addLootingBonus( float value ) {
        return addFunction( LootingEnchantBonus.lootingMultiplier( new RandomValueRange( value ) ) );
    }
    
    /** Adds a looting enchant (luck) bonus function. Gross. */
    public LootEntryItemBuilder addLootingBonus( float min, float max ) {
        return addFunction( LootingEnchantBonus.lootingMultiplier( new RandomValueRange( min, max ) ) );
    }
    
    /** Adds a looting enchant (luck) bonus function. Gross. */
    public LootEntryItemBuilder addLootingBonus( float min, float max, int limit ) {
        return addFunction( LootingEnchantBonus.lootingMultiplier( new RandomValueRange( min, max ) ).setLimit( limit ) );
    }
    
    /** Adds a set damage function. */
    public LootEntryItemBuilder setDamage( int value ) {
        return addFunction( SetDamage.setDamage( new RandomValueRange( value ) ) );
    }
    
    /** Adds a set damage function. */
    public LootEntryItemBuilder setDamage( int min, int max ) {
        return addFunction( SetDamage.setDamage( new RandomValueRange( min, max ) ) );
    }
    
    /** Adds an nbt tag compound function. */
    public LootEntryItemBuilder setNBTTag( CompoundNBT tag ) { return addFunction( SetNBT.setTag( tag ) ); }
    
    /** Adds a smelt function with the EntityOnFire condition. */
    public LootEntryItemBuilder smeltIfBurning() {
        return addFunction( Smelt.smelted().when( EntityHasProperty.hasProperties( LootContext.EntityTarget.THIS,
                SMLootTableProvider.EntitySubProvider.ENTITY_ON_FIRE ) ) );
    }
    
    /** Adds a random enchantment function. */
    public LootEntryItemBuilder applyOneRandomApplicableEnchant() {
        return addFunction( EnchantRandomly.randomApplicableEnchantment() );
    }
    
    /** Adds a random enchantment function. */
    public LootEntryItemBuilder applyOneRandomEnchant( Enchantment... enchantments ) {
        final EnchantRandomly.Builder builder = new EnchantRandomly.Builder();
        for( Enchantment enchant : enchantments ) builder.withEnchantment( enchant );
        return addFunction( builder );
    }
    
    /** Adds an enchanting function. */
    public LootEntryItemBuilder enchant( int level, boolean treasure ) {
        final EnchantWithLevels.Builder builder = EnchantWithLevels.enchantWithLevels( new RandomValueRange( level ) );
        if( treasure ) builder.allowTreasure();
        return addFunction( builder );
    }
    
    /** Adds an enchanting function. */
    public LootEntryItemBuilder enchant( int levelMin, int levelMax, boolean treasure ) {
        final EnchantWithLevels.Builder builder = EnchantWithLevels.enchantWithLevels( new RandomValueRange( levelMin, levelMax ) );
        if( treasure ) builder.allowTreasure();
        return addFunction( builder );
    }
    
    /** Adds an item function to this builder. */
    public LootEntryItemBuilder addFunction( ILootFunction.IBuilder function ) {
        itemFunctions.add( function );
        return this;
    }
}