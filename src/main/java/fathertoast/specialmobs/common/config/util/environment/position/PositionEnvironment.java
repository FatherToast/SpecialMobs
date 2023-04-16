package fathertoast.specialmobs.common.config.util.environment.position;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.EnumEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class PositionEnvironment extends EnumEnvironment<PositionEnvironment.Value> {
    
    public enum Value {
        CAN_SEE_SKY( ( world, pos ) -> pos != null && world.canSeeSky( pos ) ),
        IS_IN_VILLAGE( ( world, pos ) -> pos != null && world instanceof ServerLevel serverLevel && serverLevel.isVillage( pos ) ),
        IS_NEAR_VILLAGE( ( world, pos ) -> pos != null && world instanceof ServerLevel serverLevel &&
                serverLevel.isCloseToVillage( pos, 3 ) ),
        IS_NEAR_RAID( ( world, pos ) -> pos != null && world instanceof ServerLevel serverLevel && serverLevel.isRaided( pos ) ),
        IS_IN_WATER( ( world, pos ) -> pos != null && world.getFluidState( pos ).is( FluidTags.WATER ) ),
        IS_IN_LAVA( ( world, pos ) -> pos != null && world.getFluidState( pos ).is( FluidTags.LAVA ) ),
        IS_IN_FLUID( ( world, pos ) -> pos != null && !world.getFluidState( pos ).isEmpty() ),
        HAS_REDSTONE_POWER( ( world, pos ) -> pos != null && world.getDirectSignalTo( pos ) > 0 );
        
        private final BiFunction<Level, BlockPos, Boolean> SUPPLIER;
        
        Value( BiFunction<Level, BlockPos, Boolean> supplier ) { SUPPLIER = supplier; }
        
        public boolean of( Level level, @Nullable BlockPos pos ) { return SUPPLIER.apply( level, pos ); }
    }
    
    public PositionEnvironment( Value value, boolean invert ) { super( value, invert ); }
    
    public PositionEnvironment( AbstractConfigField field, String line ) { super( field, line, Value.values() ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_POSITION; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public boolean matches( Level level, @Nullable BlockPos pos ) { return VALUE.of( level, pos ) != INVERT; }
}