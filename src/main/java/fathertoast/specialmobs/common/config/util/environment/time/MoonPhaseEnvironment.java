package fathertoast.specialmobs.common.config.util.environment.time;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.EnumEnvironment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class MoonPhaseEnvironment extends EnumEnvironment<MoonPhaseEnvironment.Value> {
    /** Values match up to the vanilla weather command. */
    public enum Value {
        FULL( 0 ), WANING_GIBBOUS( 1 ), LAST_QUARTER( 2 ), WANING_CRESCENT( 3 ),
        NEW( 4 ), WAXING_CRESCENT( 5 ), FIRST_QUARTER( 6 ), WAXING_GIBBOUS( 7 );
        
        public final int INDEX;
        
        Value( int i ) { INDEX = i; }
    }
    
    public MoonPhaseEnvironment( Value value ) { super( value ); }
    
    public MoonPhaseEnvironment( Value value, boolean invert ) { super( value, invert ); }
    
    public MoonPhaseEnvironment( AbstractConfigField field, String line ) { super( field, line, Value.values() ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_MOON_PHASE; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public boolean matches( World world, @Nullable BlockPos pos ) {
        final int phase = world.dimensionType().moonPhase( world.dayTime() );
        return (VALUE.INDEX == phase) != INVERT;
    }
}