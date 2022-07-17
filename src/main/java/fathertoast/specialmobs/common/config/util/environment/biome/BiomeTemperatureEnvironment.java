package fathertoast.specialmobs.common.config.util.environment.biome;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.CompareFloatEnvironment;
import fathertoast.specialmobs.common.config.util.environment.ComparisonOperator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BiomeTemperatureEnvironment extends CompareFloatEnvironment {
    
    public BiomeTemperatureEnvironment( ComparisonOperator op, float value ) { super( op, value ); }
    
    public BiomeTemperatureEnvironment( AbstractConfigField field, String line ) { super( field, TemperatureEnvironment.handleTempInput( line ) ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_BIOME_TEMPERATURE; }
    
    /** @return The string value of this environment, as it would appear in a config file. */
    @Override
    public String value() {
        if( COMPARATOR == ComparisonOperator.LESS_THAN && VALUE == 0.15F )
            return TemperatureEnvironment.FREEZING;
        if( COMPARATOR == ComparisonOperator.GREATER_THAN && VALUE == Math.nextDown( 0.15F ) )
            return "!" + TemperatureEnvironment.FREEZING;
        return super.value();
    }
    
    /** @return Returns the actual value to compare, or Float.NaN if there isn't enough information. */
    @Override
    public float getActual( World world, @Nullable BlockPos pos ) {
        return pos == null ? Float.NaN : world.getBiome( pos ).getBaseTemperature();
    }
}