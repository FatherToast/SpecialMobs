package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.util.environment.*;
import fathertoast.specialmobs.common.config.util.environment.biome.BiomeEnvironment;
import fathertoast.specialmobs.common.config.util.environment.dimension.DimensionTypeEnvironment;
import fathertoast.specialmobs.common.config.util.environment.position.StructureEnvironment;
import fathertoast.specialmobs.common.config.util.environment.position.YEnvironment;
import fathertoast.specialmobs.common.config.util.environment.position.YFromSeaEnvironment;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.Structure;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * One condition-value entry in an environment list. Uses a 'lazy' implementation so any needed registries are
 * not polled until this entry is actually used.
 */
@SuppressWarnings( "unused" )
public class EnvironmentEntry {
    /** The value given to this entry. */
    public final double VALUE;
    /** The conditions that define this entry's environment. */
    private final AbstractEnvironment[] CONDITIONS;
    
    /** Creates an entry with the specified values. */
    public EnvironmentEntry( double value, List<AbstractEnvironment> conditions ) { this( value, conditions.toArray( new AbstractEnvironment[0] ) ); }
    
    /** Creates an entry with the specified values. */
    public EnvironmentEntry( double value, AbstractEnvironment... conditions ) {
        VALUE = value;
        CONDITIONS = conditions;
    }
    
    /** @return Returns true if all this entry's conditions match the provided environment. */
    public boolean matches( World world, @Nullable BlockPos pos ) {
        for( AbstractEnvironment condition : CONDITIONS ) {
            if( !condition.matches( world, pos ) ) return false;
        }
        return true;
    }
    
    /**
     * @return The string representation of this environment entry, as it would appear in a config file.
     * <p>
     * Format is "value condition1 state1 & condition2 state2 & ...".
     */
    @Override
    public String toString() {
        // Start with the value
        final StringBuilder str = new StringBuilder().append( VALUE ).append( ' ' );
        // List all conditions
        boolean first = true;
        for( AbstractEnvironment condition : CONDITIONS ) {
            if( first ) first = false;
            else str.append( " & " );
            str.append( condition );
        }
        return str.toString();
    }
    
    public static Builder builder( double value ) { return new Builder( value ); }
    
    /**
     * Builder class used to simplify creation of environment entries, with shortcuts for the most commonly used environments.
     */
    public static class Builder {
        private final double VALUE;
        private final ArrayList<AbstractEnvironment> CONDITIONS = new ArrayList<>();
        
        private Builder( double value ) { VALUE = value; }
        
        public EnvironmentEntry build() { return new EnvironmentEntry( VALUE, CONDITIONS ); }
        
        public Builder in( AbstractEnvironment condition ) {
            CONDITIONS.add( condition );
            return this;
        }
        
        
        // Dimension-based
        
        public Builder inDimensionType( RegistryKey<DimensionType> dimType ) { return in( new DimensionTypeEnvironment( dimType ) ); }
        
        
        // Biome-based
        
        public Builder inBiome( RegistryKey<Biome> biome ) { return in( new BiomeEnvironment( biome ) ); }
        
        
        // Position-based
        
        public Builder inStructure( Structure<?> structure ) { return in( new StructureEnvironment( structure ) ); }
        
        public Builder belowY( int y ) { return in( new YEnvironment( ComparisonOperator.LESS_THAN, y ) ); }
        
        public Builder atLeastY( int y ) { return in( new YEnvironment( ComparisonOperator.GREATER_OR_EQUAL, y ) ); }
        
        public Builder belowSeaLevel() { return in( new YFromSeaEnvironment( ComparisonOperator.LESS_THAN, 0 ) ); }
        
        public Builder atLeastSeaLevel() { return in( new YFromSeaEnvironment( ComparisonOperator.GREATER_OR_EQUAL, 0 ) ); }
        
        
        // Time-based
        
    }
}