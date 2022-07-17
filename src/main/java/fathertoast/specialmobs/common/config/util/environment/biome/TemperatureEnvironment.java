package fathertoast.specialmobs.common.config.util.environment.biome;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.CompareFloatEnvironment;
import fathertoast.specialmobs.common.config.util.environment.ComparisonOperator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TemperatureEnvironment extends CompareFloatEnvironment {
    
    public static final String FREEZING = "freezing";
    
    public static String handleTempInput( String line ) {
        if( line.equalsIgnoreCase( FREEZING ) )
            return ComparisonOperator.LESS_THAN + " " + 0.15F;
        if( line.equalsIgnoreCase( "!" + FREEZING ) )
            return ComparisonOperator.GREATER_THAN + " " + Math.nextDown( 0.15F );
        return line;
    }
    
    public TemperatureEnvironment( ComparisonOperator op, float value ) { super( op, value ); }
    
    public TemperatureEnvironment( AbstractConfigField field, String line ) { super( field, handleTempInput( line ) ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_TEMPERATURE; }
    
    /** @return The string value of this environment, as it would appear in a config file. */
    @Override
    public String value() {
        if( COMPARATOR == ComparisonOperator.LESS_THAN && VALUE == 0.15F )
            return FREEZING;
        if( COMPARATOR == ComparisonOperator.GREATER_THAN && VALUE == Math.nextDown( 0.15F ) )
            return "!" + FREEZING;
        return super.value();
    }
    
    /** @return Returns the actual value to compare, or Float.NaN if there isn't enough information. */
    @Override
    public float getActual( World world, @Nullable BlockPos pos ) {
        return pos == null ? Float.NaN : world.getBiome( pos ).getTemperature( pos );
    }
}