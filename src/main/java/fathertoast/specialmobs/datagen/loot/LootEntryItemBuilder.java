package fathertoast.specialmobs.datagen.loot;

import fathertoast.specialmobs.datagen.SMLootTableProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings( { "unused", "WeakerAccess", "UnusedReturnValue" } )
public class LootEntryItemBuilder {
    
    private final ItemLike item;
    
    private int weight = 1;
    private int quality = 0;
    
    private final List<LootItemFunction.Builder> itemFunctions = new ArrayList<>();
    private final List<LootItemCondition.Builder> entryConditions = new ArrayList<>();
    
    public LootEntryItemBuilder( ItemLike baseItem ) {
        item = baseItem;
    }
    
    public LootEntryItemBuilder( ItemStack itemStack ) {
        this( itemStack.getItem() );
        if( itemStack.getTag() != null ) {
            setNBTTag( itemStack.getTag().copy() );
        }
    }
    
    /** @return A new loot entry object reflecting the current state of this builder. */
    public LootItem.Builder<?> toLootEntry() {
        return LootHelper.build( LootItem.lootTableItem( item ), entryConditions, itemFunctions )
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
    public LootEntryItemBuilder addCondition( LootItemCondition.Builder condition ) {
        entryConditions.add( condition );
        return this;
    }
    
    /** Adds a stack size function. */
    public LootEntryItemBuilder setCount( int value ) {
        return addFunction( SetItemCountFunction.setCount( UniformGenerator.between( 0.0F, value ) ) );
    }
    
    /** Adds a stack size function. */
    public LootEntryItemBuilder setCount( int min, int max ) {
        return addFunction( SetItemCountFunction.setCount( UniformGenerator.between( min, max ) ) );
    }
    
    /** Adds a looting enchant (luck) bonus function. Gross. */
    public LootEntryItemBuilder addLootingBonus( float value ) {
        return addFunction( LootingEnchantFunction.lootingMultiplier( UniformGenerator.between( 0.0F, value ) ) );
    }
    
    /** Adds a looting enchant (luck) bonus function. Gross. */
    public LootEntryItemBuilder addLootingBonus( float min, float max ) {
        return addFunction( LootingEnchantFunction.lootingMultiplier( UniformGenerator.between( min, max ) ) );
    }
    
    /** Adds a looting enchant (luck) bonus function. Gross. */
    public LootEntryItemBuilder addLootingBonus( float min, float max, int limit ) {
        return addFunction( LootingEnchantFunction.lootingMultiplier( UniformGenerator.between( min, max ) ).setLimit( limit ) );
    }
    
    /** Adds a set damage function. */
    public LootEntryItemBuilder setDamage( int value ) {
        return addFunction( SetItemDamageFunction.setDamage( UniformGenerator.between( 0.0F, value ) ) );
    }
    
    /** Adds a set damage function. */
    public LootEntryItemBuilder setDamage( int min, int max ) {
        return addFunction( SetItemDamageFunction.setDamage( UniformGenerator.between( min, max ) ) );
    }
    
    /** Adds an nbt tag compound function. */
    @SuppressWarnings("deprecation")
    public LootEntryItemBuilder setNBTTag( CompoundTag tag ) { return addFunction( SetNbtFunction.setTag( tag ) ); }
    
    /** Adds a smelt function with the EntityOnFire condition. */
    public LootEntryItemBuilder smeltIfBurning() {
        return addFunction( SmeltItemFunction.smelted().when( LootItemEntityPropertyCondition.hasProperties( LootContext.EntityTarget.THIS,
                SMLootTableProvider.EntitySubProvider.ENTITY_ON_FIRE ) ) );
    }
    
    /** Adds a random enchantment function. */
    public LootEntryItemBuilder applyOneRandomApplicableEnchant() {
        return addFunction( EnchantRandomlyFunction.randomApplicableEnchantment() );
    }
    
    /** Adds a random enchantment function. */
    public LootEntryItemBuilder applyOneRandomEnchant( Enchantment... enchantments ) {
        final EnchantRandomlyFunction.Builder builder = new EnchantRandomlyFunction.Builder();
        for( Enchantment enchant : enchantments ) builder.withEnchantment( enchant );
        return addFunction( builder );
    }
    
    /** Adds an enchanting function. */
    public LootEntryItemBuilder enchant( int level, boolean treasure ) {
        final EnchantWithLevelsFunction.Builder builder = EnchantWithLevelsFunction.enchantWithLevels( UniformGenerator.between( 0.0F, level ) );
        if( treasure ) builder.allowTreasure();
        return addFunction( builder );
    }
    
    /** Adds an enchanting function. */
    public LootEntryItemBuilder enchant( int levelMin, int levelMax, boolean treasure ) {
        final EnchantWithLevelsFunction.Builder builder = EnchantWithLevelsFunction.enchantWithLevels( UniformGenerator.between( levelMin, levelMax ) );
        if( treasure ) builder.allowTreasure();
        return addFunction( builder );
    }
    
    /** Adds an item function to this builder. */
    public LootEntryItemBuilder addFunction( LootItemFunction.Builder function ) {
        itemFunctions.add( function );
        return this;
    }
}