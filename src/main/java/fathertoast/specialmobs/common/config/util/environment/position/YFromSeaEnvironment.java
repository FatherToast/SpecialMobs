package fathertoast.specialmobs.common.config.util.environment.position;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.CompareIntEnvironment;
import fathertoast.specialmobs.common.config.util.environment.ComparisonOperator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class YFromSeaEnvironment extends CompareIntEnvironment {
    
    public YFromSeaEnvironment( ComparisonOperator op, int value ) { super( op, value ); }
    
    public YFromSeaEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_Y_FROM_SEA; }
    
    /** @return Returns the actual value to compare, or null if there isn't enough information. */
    @Override
    public Integer getActual( World world, @Nullable BlockPos pos ) { return pos == null ? null : pos.getY() - world.getSeaLevel(); }
}