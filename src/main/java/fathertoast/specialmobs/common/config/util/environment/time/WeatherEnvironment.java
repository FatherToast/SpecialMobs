package fathertoast.specialmobs.common.config.util.environment.time;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.EnumEnvironment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class WeatherEnvironment extends EnumEnvironment<WeatherEnvironment.Value> {
    /** Values match up to the vanilla weather command. */
    public enum Value { CLEAR, RAIN, THUNDER }
    
    public WeatherEnvironment( Value value, boolean invert ) { super( value, invert ); }
    
    public WeatherEnvironment( AbstractConfigField field, String line ) { super( field, line, Value.values() ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_WEATHER; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public boolean matches( World world, @Nullable BlockPos pos ) {
        if( world.getLevelData().isThundering() ) return (VALUE == Value.CLEAR) == INVERT; // Thunder implies rain
        if( world.getLevelData().isRaining() ) return (VALUE == Value.RAIN) != INVERT;
        return (VALUE == Value.CLEAR) != INVERT;
    }
}