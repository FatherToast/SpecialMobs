package fathertoast.specialmobs.common.config.util.environment.biome;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.CompareFloatEnvironment;
import fathertoast.specialmobs.common.config.util.environment.ComparisonOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

// TODO - get scale from chunk generator; no longer exists in biome
public class TerrainScaleEnvironment extends CompareFloatEnvironment {
    
    public TerrainScaleEnvironment( ComparisonOperator op, float value ) { super( op, value ); }
    
    public TerrainScaleEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_TERRAIN_SCALE; }
    
    /** @return Returns the actual value to compare, or Float.NaN if there isn't enough information. */
    @Override
    public float getActual( Level level, @Nullable BlockPos pos ) {
        return 0;
        //return pos == null ? Float.NaN : level.getBiome( pos ).get().getScale();
    }
}