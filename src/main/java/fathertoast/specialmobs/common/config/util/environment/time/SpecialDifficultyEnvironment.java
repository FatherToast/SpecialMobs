package fathertoast.specialmobs.common.config.util.environment.time;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.CompareFloatEnvironment;
import fathertoast.specialmobs.common.config.util.environment.ComparisonOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Notes on special multiplier difficulty:
 * This is 0 while regional difficulty is <= 2 and this is 1 while regional difficulty is >= 4 (linearly scales between).
 * <p>
 * In Peaceful and Easy, this is always 0. In Normal, this only maxes out at the absolute peak regional difficulty.
 * In Hard, this starts out as 0.125 and reaches 1 during new moons with only ~50 days in the area.
 */
public class SpecialDifficultyEnvironment extends CompareFloatEnvironment {
    
    public SpecialDifficultyEnvironment( ComparisonOperator op, float value ) { super( op, value ); }
    
    public SpecialDifficultyEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The minimum value that can be given to the value. */
    @Override
    protected float getMinValue() { return 0.0F; }
    
    /** @return The maximum value that can be given to the value. */
    @Override
    protected float getMaxValue() { return 1.0F; }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_SPECIAL_DIFFICULTY; }
    
    /** @return Returns the actual value to compare, or Float.NaN if there isn't enough information. */
    @Override
    public float getActual( Level level, @Nullable BlockPos pos ) {
        return pos == null ? Float.NaN : level.getCurrentDifficultyAt( pos ).getSpecialMultiplier();
    }
}