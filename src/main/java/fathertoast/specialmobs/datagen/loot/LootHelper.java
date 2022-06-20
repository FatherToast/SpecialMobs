package fathertoast.specialmobs.datagen.loot;

import net.minecraft.loot.*;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.KilledByPlayer;
import net.minecraft.loot.conditions.RandomChanceWithLooting;
import net.minecraft.loot.functions.ILootFunction;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class LootHelper {
    
    public static final ILootCondition.IBuilder KILLED_BY_PLAYER_CONDITION = KilledByPlayer.killedByPlayer();
    
    public static final ILootCondition.IBuilder[] UNCOMMON_CONDITIONS = {
            RandomChanceWithLooting.randomChanceAndLootingBoost( 0.25F, 0.05F ),
            KILLED_BY_PLAYER_CONDITION
    };
    public static final ILootCondition.IBuilder[] RARE_CONDITIONS = {
            RandomChanceWithLooting.randomChanceAndLootingBoost( 0.025F, 0.0F ),
            KILLED_BY_PLAYER_CONDITION
    };
    
    public static final RandomValueRange ONE_ROLL = new RandomValueRange( 1 );
    public static final RandomValueRange NO_ROLL = new RandomValueRange( 0 );
    
    /** Convenience method to put all loot entries, conditions, and functions into a loot pool. Returns the loot pool. */
    public static LootPool.Builder build( LootPool.Builder builder, List<LootEntry.Builder<?>> entries,
                                          List<ILootCondition.IBuilder> conditions, List<ILootFunction.IBuilder> functions ) {
        return build( build( builder, entries ), conditions, functions );
    }
    
    /** Convenience method to put all loot conditions and functions into a loot builder. Returns the loot builder. */
    public static <T extends ILootFunctionConsumer<?>> // Can't figure out how to require both, but function consumer is more stringent
    T build( T builder, List<ILootCondition.IBuilder> conditions, List<ILootFunction.IBuilder> functions ) {
        build( (ILootConditionConsumer<?>) builder, conditions );
        return build( builder, functions );
    }
    
    /** Convenience method to put all loot pools into a loot table. Returns the loot table. */
    public static LootTable.Builder build( LootTable.Builder builder, List<LootPool.Builder> pools ) {
        for( LootPool.Builder pool : pools ) builder.withPool( pool );
        return builder;
    }
    
    /** Convenience method to put all loot entries into a loot pool. Returns the loot pool. */
    public static LootPool.Builder build( LootPool.Builder builder, List<LootEntry.Builder<?>> entries ) {
        for( LootEntry.Builder<?> entry : entries ) builder.add( entry );
        return builder;
    }
    
    /** Convenience method to put all loot conditions into a loot builder. Returns the loot builder. */
    public static <T extends ILootConditionConsumer<?>> T build( T builder, List<ILootCondition.IBuilder> conditions ) {
        for( ILootCondition.IBuilder condition : conditions ) builder.when( condition );
        return builder;
    }
    
    /** Convenience method to put all loot functions into a loot builder. Returns the loot builder. */
    public static <T extends ILootFunctionConsumer<?>> T build( T builder, List<ILootFunction.IBuilder> functions ) {
        for( ILootFunction.IBuilder function : functions ) builder.apply( function );
        return builder;
    }
}