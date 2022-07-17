package fathertoast.specialmobs.common.config.util.environment.dimension;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.EnumEnvironment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Function;

public class DimensionPropertyEnvironment extends EnumEnvironment<DimensionPropertyEnvironment.Value> {
    /**
     * Represents all boolean values defined by dimension type, named to match data pack format.
     *
     * @see <a href="https://minecraft.fandom.com/wiki/Custom_dimension#Syntax">Data pack format (Minecraft Wiki)</a>
     */
    public enum Value {
        @SuppressWarnings( "SpellCheckingInspection" )
        ULTRAWARM( DimensionType::ultraWarm ),
        NATURAL( DimensionType::natural ),
        HAS_SKYLIGHT( DimensionType::hasSkyLight ),
        HAS_CEILING( DimensionType::hasCeiling ),
        FIXED_TIME( DimensionType::hasFixedTime ),
        PIGLIN_SAFE( DimensionType::piglinSafe ),
        BED_WORKS( DimensionType::bedWorks ),
        RESPAWN_ANCHOR_WORKS( DimensionType::respawnAnchorWorks ),
        HAS_RAIDS( DimensionType::hasRaids );
        
        private final Function<DimensionType, Boolean> SUPPLIER;
        
        Value( Function<DimensionType, Boolean> supplier ) { SUPPLIER = supplier; }
        
        public boolean of( DimensionType dimType ) { return SUPPLIER.apply( dimType ); }
    }
    
    public DimensionPropertyEnvironment( Value value ) { super( value ); }
    
    public DimensionPropertyEnvironment( Value value, boolean invert ) { super( value, invert ); }
    
    public DimensionPropertyEnvironment( AbstractConfigField field, String line ) { super( field, line, Value.values() ); }
    
    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_DIMENSION_PROPERTY; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public boolean matches( World world, @Nullable BlockPos pos ) { return VALUE.of( world.dimensionType() ) != INVERT; }
}