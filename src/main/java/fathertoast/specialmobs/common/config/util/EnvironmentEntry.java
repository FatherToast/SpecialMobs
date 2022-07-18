package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.util.environment.*;
import fathertoast.specialmobs.common.config.util.environment.biome.*;
import fathertoast.specialmobs.common.config.util.environment.dimension.DimensionPropertyEnvironment;
import fathertoast.specialmobs.common.config.util.environment.dimension.DimensionTypeEnvironment;
import fathertoast.specialmobs.common.config.util.environment.dimension.DimensionTypeGroupEnvironment;
import fathertoast.specialmobs.common.config.util.environment.position.PositionEnvironment;
import fathertoast.specialmobs.common.config.util.environment.position.StructureEnvironment;
import fathertoast.specialmobs.common.config.util.environment.position.YEnvironment;
import fathertoast.specialmobs.common.config.util.environment.position.YFromSeaEnvironment;
import fathertoast.specialmobs.common.config.util.environment.time.*;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
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
    
    
    // Builder Implementation
    
    /** Creates a new entry builder. The value is rounded to 2 decimal place precision. */
    public static Builder builder( float value ) { return new Builder( Math.round( value * 100.0 ) / 100.0 ); }
    
    /** Creates a new entry builder. */
    public static Builder builder( double value ) { return new Builder( value ); }
    
    /**
     * Builder class used to simplify creation of environment entries for default configs,
     * with shortcuts for the most commonly used environments.
     * <p>
     * Keep in mind that ALL conditions in an entry must be satisfied for it to be chosen.
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
        
        /** Check if the dimension type causes water to instantly vaporize and has faster lava flow. */
        public Builder inUltraWarmDimension() { return inDimensionWithProperty( DimensionPropertyEnvironment.Value.ULTRAWARM, false ); }
        
        /** Check if the dimension type causes water to instantly vaporize and has faster lava flow. */
        public Builder notInUltraWarmDimension() { return inDimensionWithProperty( DimensionPropertyEnvironment.Value.ULTRAWARM, true ); }
        
        /** Check if the dimension type allows clocks, compasses, and beds to work. */
        public Builder inNaturalDimension() { return inDimensionWithProperty( DimensionPropertyEnvironment.Value.NATURAL, false ); }
        
        /** Check if the dimension type allows clocks, compasses, and beds to work. */
        public Builder notInNaturalDimension() { return inDimensionWithProperty( DimensionPropertyEnvironment.Value.NATURAL, true ); }
        
        private Builder inDimensionWithProperty( DimensionPropertyEnvironment.Value property, boolean invert ) {
            return in( new DimensionPropertyEnvironment( property, invert ) );
        }
        
        public Builder inOverworld() { return inDimensionType( DimensionType.OVERWORLD_LOCATION, false ); }
        
        public Builder notInOverworld() { return inDimensionType( DimensionType.OVERWORLD_LOCATION, true ); }
        
        public Builder inNether() { return inDimensionType( DimensionType.NETHER_LOCATION, false ); }
        
        public Builder notInNether() { return inDimensionType( DimensionType.NETHER_LOCATION, true ); }
        
        public Builder inTheEnd() { return inDimensionType( DimensionType.END_LOCATION, false ); }
        
        public Builder notInTheEnd() { return inDimensionType( DimensionType.END_LOCATION, true ); }
        
        private Builder inDimensionType( RegistryKey<DimensionType> dimType, boolean invert ) { return in( new DimensionTypeEnvironment( dimType, invert ) ); }
        
        /** Check if the dimension type is vanilla (registered with the "minecraft" namespace). */
        public Builder inVanillaDimension() { return in( new DimensionTypeGroupEnvironment( new ResourceLocation( "" ), false ) ); }
        
        /** Check if the dimension type is vanilla (registered with the "minecraft" namespace). */
        public Builder notInVanillaDimension() { return in( new DimensionTypeGroupEnvironment( new ResourceLocation( "" ), true ) ); }
        
        
        // Biome-based
        
        /** Check if the biome has rain disabled. */
        public Builder inDryBiome() { return inAvgRainfall( ComparisonOperator.EQUAL_TO, 0.0F ); }
        
        /** Check if the biome has rain disabled. */
        public Builder notInDryBiome() { return inAvgRainfall( ComparisonOperator.EQUAL_TO.invert(), 0.0F ); }
        
        /** Check if the biome's humidity hinders fire spread. */
        public Builder inHumidBiome() { return inAvgRainfall( ComparisonOperator.GREATER_THAN, 0.85F ); }
        
        /** Check if the biome's humidity hinders fire spread. */
        public Builder notInHumidBiome() { return inAvgRainfall( ComparisonOperator.GREATER_THAN.invert(), 0.85F ); }
        
        private Builder inAvgRainfall( ComparisonOperator op, float value ) { return in( new RainfallEnvironment( op, value ) ); }
        
        /** Check if the temperature is freezing. */
        public Builder isFreezing() { return in( new TemperatureEnvironment( true ) ); }
        
        /** Check if the temperature is freezing. */
        public Builder isNotFreezing() { return in( new TemperatureEnvironment( false ) ); }
        
        /** Check if the temperature is warm (disables snow golem trails). */
        public Builder isWarm() { return isTemperature( ComparisonOperator.GREATER_OR_EQUAL, 0.8F ); }
        
        /** Check if the temperature is warm (disables snow golem trails). */
        public Builder isNotWarm() { return isTemperature( ComparisonOperator.GREATER_OR_EQUAL.invert(), 0.8F ); }
        
        /** Check if the temperature is hot (causes snow golems to die). */
        public Builder isHot() { return isTemperature( ComparisonOperator.GREATER_THAN, 1.0F ); }
        
        /** Check if the temperature is hot (causes snow golems to die). */
        public Builder isNotHot() { return isTemperature( ComparisonOperator.GREATER_THAN.invert(), 1.0F ); }
        
        private Builder isTemperature( ComparisonOperator op, float value ) { return in( new TemperatureEnvironment( op, value ) ); }
        
        /** Check if the biome belongs to a specific category. */
        public Builder inBiomeCategory( BiomeCategory category ) { return in( new BiomeCategoryEnvironment( category, false ) ); }
        
        /** Check if the biome belongs to a specific category. */
        public Builder notInBiomeCategory( BiomeCategory category ) { return in( new BiomeCategoryEnvironment( category, true ) ); }
        
        
        // Position-based
        
        /** Check if the position is inside a particular structure. See {@link Structure}. */
        public Builder inStructure( Structure<?> structure ) { return in( new StructureEnvironment( structure, false ) ); }
        
        /** Check if the position is inside a particular structure. See {@link Structure}. */
        public Builder notInStructure( Structure<?> structure ) { return in( new StructureEnvironment( structure, true ) ); }
        
        /** Check if diamond/redstone ore can generate at the position. */
        public Builder belowDiamondLevel() { return belowY( 15 ); } // TODO update ore-based logic in 1.18
        
        /** Check if diamond/redstone ore can generate at the position. */
        public Builder aboveDiamondLevel() { return aboveY( 15 ); }
        
        /** Check if gold/lapis ore can generate at the position. */
        public Builder belowGoldLevel() { return belowY( 33 ); }
        
        /** Check if gold/lapis ore can generate at the position. */
        public Builder aboveGoldLevel() { return aboveY( 33 ); }
        
        private Builder belowY( int y ) { return in( new YEnvironment( ComparisonOperator.LESS_OR_EQUAL, y ) ); }
        
        private Builder aboveY( int y ) { return in( new YEnvironment( ComparisonOperator.LESS_OR_EQUAL.invert(), y ) ); }
        
        /** Check if the position is above/below sea level. */
        public Builder belowSeaLevel() { return in( new YFromSeaEnvironment( ComparisonOperator.LESS_OR_EQUAL, 0 ) ); }
        
        /** Check if the position is above/below sea level. */
        public Builder aboveSeaLevel() { return in( new YFromSeaEnvironment( ComparisonOperator.LESS_OR_EQUAL.invert(), 0 ) ); }
        
        /** Check if the position is above/below the average sea floor. */
        public Builder belowSeaFloor() { return in( new YFromSeaEnvironment( ComparisonOperator.LESS_OR_EQUAL, -18 ) ); }
        
        /** Check if the position is above/below the average sea floor. */
        public Builder aboveSeaFloor() { return in( new YFromSeaEnvironment( ComparisonOperator.LESS_OR_EQUAL.invert(), -18 ) ); }
        
        public Builder canSeeSky() { return inPositionWithState( PositionEnvironment.Value.CAN_SEE_SKY, false ); }
        
        public Builder cannotSeeSky() { return inPositionWithState( PositionEnvironment.Value.CAN_SEE_SKY, true ); }
        
        public Builder isNearVillage() { return inPositionWithState( PositionEnvironment.Value.IS_NEAR_VILLAGE, false ); }
        
        public Builder isNotNearVillage() { return inPositionWithState( PositionEnvironment.Value.IS_NEAR_VILLAGE, true ); }
        
        public Builder isNearRaid() { return inPositionWithState( PositionEnvironment.Value.IS_NEAR_RAID, false ); }
        
        public Builder isNotNearRaid() { return inPositionWithState( PositionEnvironment.Value.IS_NEAR_RAID, true ); }
        
        private Builder inPositionWithState( PositionEnvironment.Value state, boolean invert ) { return in( new PositionEnvironment( state, invert ) ); }
        
        
        // Time-based
        
        /** Check if the special difficulty multiplier is above a threshold (0 - 1). */
        public Builder aboveDifficulty( float percent ) { return in( new SpecialDifficultyEnvironment( ComparisonOperator.GREATER_OR_EQUAL, percent ) ); }
        
        /** Check if the special difficulty multiplier is above a threshold (0 - 1). */
        public Builder belowDifficulty( float percent ) { return in( new SpecialDifficultyEnvironment( ComparisonOperator.GREATER_OR_EQUAL.invert(), percent ) ); }
        
        public Builder isRaining() { return inWeather( WeatherEnvironment.Value.RAIN, false ); } // same as "is not clear"
        
        public Builder isNotRaining() { return inWeather( WeatherEnvironment.Value.RAIN, true ); } // same as "is clear"
        
        public Builder isThundering() { return inWeather( WeatherEnvironment.Value.THUNDER, false ); }
        
        public Builder isNotThundering() { return inWeather( WeatherEnvironment.Value.THUNDER, true ); }
        
        private Builder inWeather( WeatherEnvironment.Value weather, boolean invert ) { return in( new WeatherEnvironment( weather, invert ) ); }
        
        public Builder atMaxMoonLight() { return in( new MoonPhaseEnvironment( MoonPhaseEnvironment.Value.FULL, false ) ); }
        
        public Builder aboveHalfMoonLight() { return fromHalfMoonLight( ComparisonOperator.GREATER_THAN ); }
        
        public Builder atHalfMoonLight() { return fromHalfMoonLight( ComparisonOperator.EQUAL_TO ); }
        
        public Builder belowHalfMoonLight() { return fromHalfMoonLight( ComparisonOperator.LESS_THAN ); }
        
        public Builder atNoMoonLight() { return in( new MoonPhaseEnvironment( MoonPhaseEnvironment.Value.NEW, true ) ); }
        
        private Builder fromHalfMoonLight( ComparisonOperator op ) { return in( new MoonBrightnessEnvironment( op, 0.5F ) ); }
        
        public Builder isNight() { return in( new DayTimeEnvironment( DayTimeEnvironment.Value.NIGHT, false ) ); }
        
        public Builder isDay() { return in( new DayTimeEnvironment( DayTimeEnvironment.Value.DAY, false ) ); }
        
        /** Check if the time is during a quarter of the night centered on midnight. */
        public Builder isNearMidnight() { return in( new TimeFromMidnightEnvironment( ComparisonOperator.LESS_OR_EQUAL, 1_500 ) ); }
        
        /** Check if the time is during a quarter of the night centered on midnight. */
        public Builder isNotNearMidnight() { return in( new TimeFromMidnightEnvironment( ComparisonOperator.LESS_OR_EQUAL.invert(), 1_500 ) ); }
        
        /** Check if the world time is after a certain number of days. */
        public Builder afterDays( int days ) { return in( new WorldTimeEnvironment( ComparisonOperator.GREATER_OR_EQUAL, 24_000L * days ) ); }
        
        /** Check if the world time is after a certain number of days. */
        public Builder beforeDays( int days ) { return in( new WorldTimeEnvironment( ComparisonOperator.GREATER_OR_EQUAL.invert(), 24_000L * days ) ); }
        
        /** Check if the chunk inhabited time is after a certain number of days. */
        public Builder afterDaysInChunk( int days ) { return in( new ChunkTimeEnvironment( ComparisonOperator.GREATER_OR_EQUAL, 24_000L * days ) ); }
        
        /** Check if the chunk inhabited time is after a certain number of days. */
        public Builder beforeDaysInChunk( int days ) { return in( new ChunkTimeEnvironment( ComparisonOperator.GREATER_OR_EQUAL.invert(), 24_000L * days ) ); }
    }
}