package fathertoast.specialmobs.common.config.util.environment.time;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.EnumEnvironment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class DayTimeEnvironment extends EnumEnvironment<DayTimeEnvironment.Value> {
    /** Values match up to the vanilla set time command. */
    public enum Value {
        DAY( 1_000, 13_000 ), SUNSET( 12_000, 13_000 ),
        NIGHT( 13_000, 1_000 ), SUNRISE( 23_000, 1_000 );
        
        private final int START, END;
        
        Value( int start, int end ) {
            START = start;
            END = end;
        }
        
        public boolean matches( int dayTime ) {
            if( START < END ) return START <= dayTime && dayTime < END;
            return START <= dayTime || dayTime < END; // Handle day wrapping
        }
    }
    
    public DayTimeEnvironment( Value value, boolean invert ) { super( value, invert ); }
    
    public DayTimeEnvironment( AbstractConfigField field, String line ) { super( field, line, Value.values() ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_DAY_TIME; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public boolean matches( World world, @Nullable BlockPos pos ) {
        return (VALUE.matches( (int) (world.dayTime() / 24_000L) )) != INVERT;
    }
}