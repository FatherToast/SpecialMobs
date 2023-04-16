package fathertoast.specialmobs.common.config.util.environment.time;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.CompareIntEnvironment;
import fathertoast.specialmobs.common.config.util.environment.ComparisonOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class TimeFromMidnightEnvironment extends CompareIntEnvironment {
    
    public TimeFromMidnightEnvironment( ComparisonOperator op, int value ) { super( op, value ); }
    
    public TimeFromMidnightEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_TIME_FROM_MIDNIGHT; }
    
    /** @return The minimum value that can be given to the value. */
    @Override
    protected int getMinValue() { return 0; }
    
    /** @return The maximum value that can be given to the value. */
    @Override
    protected int getMaxValue() { return 12_000; }
    
    /** @return Returns the actual value to compare, or null if there isn't enough information. */
    @Override
    public Integer getActual( Level world, @Nullable BlockPos pos ) {
        int dayTime = (int) (world.dayTime() / 24_000L);
        if( dayTime < 18_000 ) dayTime += 24_000;
        return dayTime - 18_000;
    }
}