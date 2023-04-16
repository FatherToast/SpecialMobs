package fathertoast.specialmobs.common.config.util.environment.biome;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.ComparisonOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class BiomeTemperatureEnvironment extends TemperatureEnvironment {
    
    public BiomeTemperatureEnvironment( boolean freezing ) { super( freezing ); }
    
    public BiomeTemperatureEnvironment( ComparisonOperator op, float value ) { super( op, value ); }
    
    public BiomeTemperatureEnvironment( AbstractConfigField field, String line ) { super( field, line ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_BIOME_TEMPERATURE; }
    
    /** @return Returns the actual value to compare, or Float.NaN if there isn't enough information. */
    @Override
    public float getActual( Level level, @Nullable BlockPos pos ) {
        return pos == null ? Float.NaN : level.getBiome( pos ).get().getBaseTemperature();
    }
}