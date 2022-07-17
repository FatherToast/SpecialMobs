package fathertoast.specialmobs.common.config.util.environment.position;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.EnumEnvironment;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class PositionEnvironment extends EnumEnvironment<PositionEnvironment.Value> {
    
    public enum Value {
        CAN_SEE_SKY( ( world, pos ) -> pos != null && world.canSeeSky( pos ) ),
        IS_IN_VILLAGE( ( world, pos ) -> pos != null && world instanceof ServerWorld && ((ServerWorld) world).isVillage( pos ) ),
        IS_NEAR_VILLAGE( ( world, pos ) -> pos != null && world instanceof ServerWorld &&
                ((ServerWorld) world).isCloseToVillage( pos, 3 ) ),
        IS_RAIDED( ( world, pos ) -> pos != null && world instanceof ServerWorld && ((ServerWorld) world).isRaided( pos ) ),
        IS_IN_WATER( ( world, pos ) -> pos != null && world.getFluidState( pos ).is( FluidTags.WATER ) ),
        IS_IN_LAVA( ( world, pos ) -> pos != null && world.getFluidState( pos ).is( FluidTags.LAVA ) ),
        IS_IN_FLUID( ( world, pos ) -> pos != null && !world.getFluidState( pos ).isEmpty() ),
        HAS_REDSTONE_POWER( ( world, pos ) -> pos != null && world.getDirectSignalTo( pos ) > 0 );
        
        private final BiFunction<World, BlockPos, Boolean> SUPPLIER;
        
        Value( BiFunction<World, BlockPos, Boolean> supplier ) { SUPPLIER = supplier; }
        
        public boolean of( World world, @Nullable BlockPos pos ) { return SUPPLIER.apply( world, pos ); }
    }
    
    public PositionEnvironment( Value value ) { super( value ); }
    
    public PositionEnvironment( Value value, boolean invert ) { super( value, invert ); }
    
    public PositionEnvironment( AbstractConfigField field, String line ) { super( field, line, Value.values() ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_POSITION; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public boolean matches( World world, @Nullable BlockPos pos ) { return VALUE.of( world, pos ) != INVERT; }
}