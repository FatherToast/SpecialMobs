package fathertoast.specialmobs.datagen.loot;


import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;

public class LootHelper {
    
    public static final LootItemCondition.Builder KILLED_BY_PLAYER_CONDITION = LootItemKilledByPlayerCondition.killedByPlayer();
    
    public static final LootItemCondition.Builder[] UNCOMMON_CONDITIONS = {
            LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost( 0.25F, 0.05F ),
            KILLED_BY_PLAYER_CONDITION
    };
    public static final LootItemCondition.Builder[] RARE_CONDITIONS = {
            LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost( 0.025F, 0.0F ),
            KILLED_BY_PLAYER_CONDITION
    };
    
    public static final UniformGenerator ONE_ROLL = UniformGenerator.between( 0.0F, 1.0F );
    public static final UniformGenerator NO_ROLL = UniformGenerator.between( 0.0F, 0.0F );
    
    /** Convenience method to put all loot entries, conditions, and functions into a loot pool. Returns the loot pool. */
    public static LootPool.Builder build(LootPool.Builder builder, List<LootItem.Builder<?>> entries,
                                         List<LootItemCondition.Builder> conditions, List<LootItemFunction.Builder> functions ) {
        return build( build( builder, entries ), conditions, functions );
    }
    
    /** Convenience method to put all loot conditions and functions into a loot builder. Returns the loot builder. */
    public static <T extends FunctionUserBuilder<?>> // Can't figure out how to require both, but function consumer is more stringent
    T build( T builder, List<LootItemCondition.Builder> conditions, List<LootItemFunction.Builder> functions ) {
        build( (ConditionUserBuilder<?>) builder, conditions );
        return build( builder, functions );
    }
    
    /** Convenience method to put all loot pools into a loot table. Returns the loot table. */
    public static LootTable.Builder build( LootTable.Builder builder, List<LootPool.Builder> pools ) {
        for( LootPool.Builder pool : pools ) builder.withPool( pool );
        return builder;
    }
    
    /** Convenience method to put all loot entries into a loot pool. Returns the loot pool. */
    public static LootPool.Builder build( LootPool.Builder builder, List<LootItem.Builder<?>> entries ) {
        for( LootItem.Builder<?> entry : entries ) builder.add( entry );
        return builder;
    }
    
    /** Convenience method to put all loot conditions into a loot builder. Returns the loot builder. */
    public static <T extends ConditionUserBuilder<?>> T build( T builder, List<LootItemCondition.Builder> conditions ) {
        for( LootItemCondition.Builder condition : conditions ) builder.when( condition );
        return builder;
    }
    
    /** Convenience method to put all loot functions into a loot builder. Returns the loot builder. */
    public static <T extends FunctionUserBuilder<?>> T build( T builder, List<LootItemFunction.Builder> functions ) {
        for( LootItemFunction.Builder function : functions ) builder.apply( function );
        return builder;
    }
}