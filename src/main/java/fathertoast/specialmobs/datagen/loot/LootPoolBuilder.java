package fathertoast.specialmobs.datagen.loot;

import net.minecraft.loot.*;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings( { "unused", "WeakerAccess", "UnusedReturnValue" } )
public class LootPoolBuilder {
    
    private final String name;
    
    private RandomValueRange rolls = LootHelper.ONE_ROLL;
    private RandomValueRange bonusRolls = LootHelper.NO_ROLL;
    
    private final List<LootEntry.Builder<?>> lootEntries = new ArrayList<>();
    private final List<ILootCondition.IBuilder> poolConditions = new ArrayList<>();
    private final List<ILootFunction.IBuilder> poolFunctions = new ArrayList<>();
    
    public LootPoolBuilder( String id ) { name = id; }
    
    /** @return A new loot pool object reflecting the current state of this builder. */
    public LootPool.Builder toLootPool() {
        return LootHelper.build( LootPool.lootPool(), lootEntries, poolConditions, poolFunctions )
                .setRolls( rolls ).bonusRolls( bonusRolls.getMin(), bonusRolls.getMax() ).name( name );
    }
    
    /** @param value The number of rolls for this pool. */
    public LootPoolBuilder setRolls( float value ) {
        rolls = new RandomValueRange( value );
        return this;
    }
    
    /**
     * @param min Minimum rolls for this pool (inclusive).
     * @param max Maximum rolls for this pool (inclusive).
     */
    public LootPoolBuilder setRolls( float min, float max ) {
        rolls = new RandomValueRange( min, max );
        return this;
    }
    
    /** @param value The additional rolls for each level of looting. */
    public LootPoolBuilder setBonusRolls( float value ) {
        bonusRolls = new RandomValueRange( value );
        return this;
    }
    
    /**
     * @param min Minimum additional rolls for this pool for each level of looting (inclusive).
     * @param max Maximum additional rolls for this pool for each level of looting (inclusive).
     */
    public LootPoolBuilder setBonusRolls( float min, float max ) {
        bonusRolls = new RandomValueRange( min, max );
        return this;
    }
    
    /** @param entry A loot entry to add to this builder. */
    public LootPoolBuilder addEntry( LootEntry.Builder<?> entry ) {
        lootEntries.add( entry );
        return this;
    }
    
    /** @param conditions Any number of conditions to add to this builder. */
    public LootPoolBuilder addConditions( ILootCondition.IBuilder... conditions ) {
        poolConditions.addAll( Arrays.asList( conditions ) );
        return this;
    }
    
    /** @param functions Any number of functions to add to this builder. */
    public LootPoolBuilder addFunctions( ILootFunction.IBuilder... functions ) {
        poolFunctions.addAll( Arrays.asList( functions ) );
        return this;
    }
    
    /** Adds a loot table as a drop. */
    public LootPoolBuilder addEntryTable( ResourceLocation lootTable ) {
        return addEntryTable( lootTable, 1, 0 );
    }
    
    /** Adds a loot table as a drop. */
    public LootPoolBuilder addEntryTable( ResourceLocation lootTable, int weight, int quality, ILootCondition.IBuilder... conditions ) {
        final StandaloneLootEntry.Builder<?> builder = TableLootEntry.lootTableReference( lootTable );
        // Note: we can also extend this to allow functions, if needed
        for( ILootCondition.IBuilder condition : conditions ) builder.when( condition );
        return addEntry( builder.setWeight( weight ).setQuality( quality ) );
    }
    
    /** Adds an empty entry. */
    public LootPoolBuilder addEntryEmpty( int weight, int quality, ILootCondition.IBuilder... conditions ) {
        StandaloneLootEntry.Builder<?> builder = EmptyLootEntry.emptyItem();
        for( ILootCondition.IBuilder condition : conditions ) builder.when( condition );
        return addEntry( builder.setWeight( weight ).setQuality( quality ) );
    }
}