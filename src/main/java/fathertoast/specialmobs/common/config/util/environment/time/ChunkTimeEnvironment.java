package fathertoast.specialmobs.common.config.util.environment.time;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.CompareLongEnvironment;
import fathertoast.specialmobs.common.config.util.environment.ComparisonOperator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ChunkTimeEnvironment extends CompareLongEnvironment {
    
    public ChunkTimeEnvironment( ComparisonOperator op, long value ) { super( op, value ); }
    
    public ChunkTimeEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The minimum value that can be given to the value. */
    @Override
    protected long getMinValue() { return 0L; }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_CHUNK_TIME; }
    
    /** @return Returns the actual value to compare, or null if there isn't enough information. */
    @Override
    public Long getActual( World world, @Nullable BlockPos pos ) {
        // Ignore deprecation; this is intentionally the same method used by World#getCurrentDifficultyAt
        //noinspection deprecation
        return pos == null || !world.hasChunkAt( pos ) ? null : world.getChunkAt( pos ).getInhabitedTime();
    }
}