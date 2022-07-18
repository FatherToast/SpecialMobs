package fathertoast.specialmobs.common.config.field;

import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.config.util.EnvironmentEntry;
import fathertoast.specialmobs.common.config.util.EnvironmentList;
import fathertoast.specialmobs.common.config.util.environment.*;
import fathertoast.specialmobs.common.config.util.environment.biome.*;
import fathertoast.specialmobs.common.config.util.environment.dimension.DimensionPropertyEnvironment;
import fathertoast.specialmobs.common.config.util.environment.dimension.DimensionTypeEnvironment;
import fathertoast.specialmobs.common.config.util.environment.dimension.DimensionTypeGroupEnvironment;
import fathertoast.specialmobs.common.config.util.environment.position.*;
import fathertoast.specialmobs.common.config.util.environment.time.*;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Represents a config field with an environment list value.
 */
public class EnvironmentListField extends GenericField<EnvironmentList> {
    
    /* When adding new environment conditions, you must:
     *  - Add a new constant for its name here
     *  - Create the environment class and ensure #name() returns the new name constant
     *  - Link the name to construction in #parseCondition(String,String) below
     *  - Add any applicable builder methods in EnvironmentEntry.Builder
     *  - Describe the new environment condition's usage/format in #environmentDescriptions() below
     */
    // Dimension-based
    public static final String ENV_DIMENSION_PROPERTY = "dimension_property";
    public static final String ENV_DIMENSION_TYPE = "dimension_type";
    // Biome-based
    public static final String ENV_RAINFALL = "rainfall";
    public static final String ENV_BIOME_TEMPERATURE = "biome_temp";
    public static final String ENV_TEMPERATURE = "temp";
    public static final String ENV_BIOME_CATEGORY = "biome_category";
    public static final String ENV_BIOME = "biome";
    // Position-based
    public static final String ENV_STRUCTURE = "structure";
    public static final String ENV_Y = "y";
    public static final String ENV_Y_FROM_SEA = "y_from_sea";
    public static final String ENV_POSITION = "position";
    // Time-based
    public static final String ENV_DIFFICULTY = "difficulty";
    public static final String ENV_SPECIAL_DIFFICULTY = "special_difficulty";
    public static final String ENV_WEATHER = "weather";
    public static final String ENV_MOON_BRIGHTNESS = "moon_brightness";
    public static final String ENV_MOON_PHASE = "moon_phase";
    public static final String ENV_DAY_TIME = "day_time";
    public static final String ENV_TIME_FROM_MIDNIGHT = "time_from_midnight";
    public static final String ENV_WORLD_TIME = "world_time";
    public static final String ENV_CHUNK_TIME = "chunk_time";
    
    /**
     * Provides a description of how to use environment lists. Recommended to put at the top of any file using environment lists.
     * Always use put the environment condition descriptions at the bottom of the file if this is used!
     */
    public static List<String> verboseDescription() {
        List<String> comment = new ArrayList<>();
        comment.add( "Environment List fields: General format = [ \"value environment1 condition1 & environment2 condition2 & ...\", ... ]" );
        comment.add( "  Environment lists are arrays of environment entries. Each entry is a value followed by the environment conditions that must be" );
        comment.add( "    satisfied for the value to be chosen. The environments are tested in the order listed, and the first matching entry is chosen." );
        comment.add( "  See the bottom of this file for an explanation on each environment condition available." );
        return comment;
    }
    
    /** Provides a detailed description of how to use each environment condition. Recommended to put at the bottom of any file using environment lists. */
    public static List<String> environmentDescriptions() {
        List<String> comment = new ArrayList<>();
        comment.add( "Environment conditions (for Environment List entries):" );
        comment.add( "  Many environment conditions can be inverted by using \"!\"; these are shown with (!) in the appropriate location." );
        comment.add( "  Other environment conditions are numerical comparisons; these use the operators (shown as op) <, >, =, <=, >=, or != to compare value." );
        comment.add( "Valid environment conditions are:" );
        // Dimension-based
        comment.add( "  \"" + ENV_DIMENSION_PROPERTY + " (!)property\":" );
        comment.add( "    Valid property values: " + TomlHelper.literalList( (Object[]) DimensionPropertyEnvironment.Value.values() ) );
        comment.add( "    Dimension properties are the true/false values available to dimension types in data packs." );
        comment.add( "    See the wiki for more info: [https://minecraft.fandom.com/wiki/Custom_dimension#Syntax]." );
        comment.add( "  \"" + ENV_DIMENSION_TYPE + " (!)namespace:dimension_type_name\":" );
        comment.add( "    The world's dimension type. In vanilla, these are only \"minecraft:overworld\", \"minecraft:the_nether\", or \"minecraft:the_end\"." );
        // Biome-based
        comment.add( "  \"" + ENV_RAINFALL + " op value\":" );
        comment.add( "    Biome's rainfall parameter. If this is \"= 0\", it checks that rain is disabled. For reference, rainfall > 0.85 suppresses fire." );
        comment.add( "  \"" + ENV_BIOME_TEMPERATURE + " op value\" or \"" + ENV_BIOME_TEMPERATURE + " (!)" + TemperatureEnvironment.FREEZING + "\":" );
        comment.add( "    Biome's temperature parameter. For reference, freezing is < 0.15 and hot is generally considered > 0.95." );
        comment.add( "  \"" + ENV_TEMPERATURE + " op value\" or \"" + ENV_TEMPERATURE + " (!)" + TemperatureEnvironment.FREEZING + "\":" );
        comment.add( "    Height-adjusted temperature. For reference, freezing is < 0.15 and hot is generally considered > 0.95." );
        comment.add( "  \"" + ENV_BIOME_CATEGORY + " (!)category\":" );
        comment.add( "    Valid category values: " + TomlHelper.literalList( (Object[]) BiomeCategory.values() ) );
        comment.add( "  \"" + ENV_BIOME + " (!)namespace:biome_name\":" );
        comment.add( "    The biome. See the wiki for vanilla biome names (resource locations) [https://minecraft.fandom.com/wiki/Biome#Biome_IDs]." );
        // Position-based
        comment.add( "  \"" + ENV_STRUCTURE + " (!)namespace:structure_name\":" );
        comment.add( "    The structure. See the wiki for vanilla structure names [https://minecraft.fandom.com/wiki/Generated_structures#Locating]." );
        comment.add( "  \"" + ENV_Y + " op value\":" );
        comment.add( "    The y-value. For reference, sea level is normally 63 and lava level is normally 10." );//TODO change lava level to -54 for MC 1.18
        comment.add( "  \"" + ENV_Y_FROM_SEA + " op value\":" );
        comment.add( "    The y-value from sea level. Expect the only air <= 0 to be in caves/ravines (which may still have direct view of the sky)." );
        comment.add( "  \"" + ENV_POSITION + " (!)state\":" );
        comment.add( "    Valid state values: " + TomlHelper.literalList( (Object[]) PositionEnvironment.Value.values() ) );
        comment.add( "    Miscellaneous conditions that generally do what you expect. For reference, 'near' a village is ~3 chunks, and redstone checks weak power." );
        // Time-based
        comment.add( "  \"" + ENV_DIFFICULTY + " op value\":" );
        comment.add( "    The regional difficulty (0 to 6.75). This is based on many factors such as difficulty setting, moon brightness, chunk inhabited time, and world time." );
        comment.add( "    For reference, this scales up to the max after 63 days in the world and 150 days in a particular chunk, and peaks during full moons." );
        comment.add( "    On Peaceful this is always 0, on Easy this is 0.75 to 1.5, on Normal this is 1.5 to 4.0, and on Hard this is 2.25 to 6.75." );
        comment.add( "  \"" + ENV_SPECIAL_DIFFICULTY + " op value\":" );
        comment.add( "    The 'special multiplier' for regional difficulty (0 to 1). For reference, this is 0 when difficulty <= 2 and 1 when difficulty >= 4." );
        comment.add( "    This is always 0 in Easy and below. In Normal, it maxes at absolute peak regional difficulty. In Hard, it starts at 0.125 and maxes out in ~50 days." );
        comment.add( "  \"" + ENV_WEATHER + " (!)type\":" );
        comment.add( "    Valid type values: " + TomlHelper.literalList( (Object[]) WeatherEnvironment.Value.values() ) );
        comment.add( "  \"" + ENV_MOON_BRIGHTNESS + " op value\":" );
        comment.add( "    The moon brightness (0 to 1). New moon has 0 brightness, full moon has 1 brightness. Intermediate phases are 0.25, 0.5, or 0.75." );
        comment.add( "  \"" + ENV_MOON_PHASE + " (!)phase\":" );
        comment.add( "    Valid phase values: " + TomlHelper.literalList( (Object[]) MoonPhaseEnvironment.Value.values() ) );
        comment.add( "  \"" + ENV_DAY_TIME + " (!)time\":" );
        comment.add( "    Valid time values: " + TomlHelper.literalList( (Object[]) DayTimeEnvironment.Value.values() ) );
        comment.add( "    Note that the transition periods, sunset & sunrise, are considered as part of day & night, respectively." );
        comment.add( "  \"" + ENV_TIME_FROM_MIDNIGHT + " op value\":" );
        comment.add( "    The absolute time in ticks away from midnight. Value must be 0 to 12000." );
        comment.add( "  \"" + ENV_MOON_PHASE + " (!)phase\":" );
        comment.add( "    Valid phase values: " + TomlHelper.literalList( (Object[]) MoonPhaseEnvironment.Value.values() ) );
        comment.add( "    For reference, the first day in a new world is always a full moon." );
        comment.add( "  \"" + ENV_WORLD_TIME + " op value\":" );
        comment.add( "    The total time the world has existed, in ticks. For reference, each day cycle is 24000 ticks and each lunar cycle is 192000 ticks." );
        comment.add( "  \"" + ENV_CHUNK_TIME + " op value\":" );
        comment.add( "    The total time the chunk has been loaded, in ticks. For reference, each day cycle is 24000 ticks and each lunar cycle is 192000 ticks." );
        return comment;
    }
    
    /** Creates a new field. */
    public EnvironmentListField( String key, EnvironmentList defaultValue, String... description ) {
        super( key, defaultValue, description );
    }
    
    /** Adds info about the field type, format, and bounds to the end of a field's description. */
    public void appendFieldInfo( List<String> comment ) {
        comment.add( TomlHelper.fieldInfoFormat( "Environment List", valueDefault, "[ \"value condition1 state1 & condition2 state2 & ...\", ... ]" ) );
        comment.add( "   Range for Values: " + TomlHelper.fieldRange( valueDefault.getMinValue(), valueDefault.getMaxValue() ) );
    }
    
    /**
     * Loads this field's value from the given raw toml value. If anything goes wrong, correct it at the lowest level possible.
     * <p>
     * For example, a missing value should be set to the default, while an out-of-range value should be adjusted to the
     * nearest in-range value
     */
    @Override
    public void load( @Nullable Object raw ) {
        if( raw == null ) {
            value = valueDefault;
            return;
        }
        List<String> list = TomlHelper.parseStringList( raw );
        List<EnvironmentEntry> entryList = new ArrayList<>();
        for( String line : list ) {
            entryList.add( parseEntry( line ) );
        }
        value = new EnvironmentList( entryList );
    }
    
    /** Parses a single entry line and returns the result. */
    private EnvironmentEntry parseEntry( final String line ) {
        // Parse the value out of the conditions
        final String[] args = line.split( " ", 2 );
        final double value = parseValue( args[0], line );
        
        final List<AbstractEnvironment> conditions = new ArrayList<>();
        if( args.length > 1 ) {
            final String[] condArgs = args[1].split( "&" );
            for( String condArg : condArgs ) {
                conditions.add( parseCondition( condArg.trim(), line ) );
            }
        }
        if( conditions.isEmpty() ) {
            SpecialMobs.LOG.warn( "No environments defined in entry for {} \"{}\"! Invalid entry: {}",
                    getClass(), getKey(), line );
        }
        
        return new EnvironmentEntry( value, conditions );
    }
    
    /** Parses a single value argument and returns a valid result. */
    private double parseValue( final String arg, final String line ) {
        // Try to parse the value
        double value;
        try {
            value = Double.parseDouble( arg );
        }
        catch( NumberFormatException ex ) {
            // This is thrown if the string is not a parsable number
            SpecialMobs.LOG.warn( "Invalid value for {} \"{}\"! Falling back to 0. Invalid entry: {}",
                    getClass(), getKey(), line );
            value = 0.0;
        }
        // Verify value is within range
        if( value < valueDefault.getMinValue() ) {
            SpecialMobs.LOG.warn( "Value for {} \"{}\" is below the minimum ({})! Clamping value. Invalid value: {}",
                    getClass(), getKey(), valueDefault.getMinValue(), value );
            value = valueDefault.getMinValue();
        }
        else if( value > valueDefault.getMaxValue() ) {
            SpecialMobs.LOG.warn( "Value for {} \"{}\" is above the maximum ({})! Clamping value. Invalid value: {}",
                    getClass(), getKey(), valueDefault.getMaxValue(), value );
            value = valueDefault.getMaxValue();
        }
        return value;
    }
    
    /** Parses a single environment condition argument and returns a valid result. */
    private AbstractEnvironment parseCondition( final String arg, final String line ) {
        // First parse the environment name, since it defines the format for the rest
        final String[] args = arg.split( " ", 2 );
        
        final String value;
        if( args.length < 2 ) value = "";
        else value = args[1].trim();
        
        switch( args[0].toLowerCase( Locale.ROOT ) ) {
            // Dimension-based
            case ENV_DIMENSION_PROPERTY:
                return new DimensionPropertyEnvironment( this, value );
            case ENV_DIMENSION_TYPE:
                return value.endsWith( "*" ) ? new DimensionTypeGroupEnvironment( this, value ) : new DimensionTypeEnvironment( this, value );
            // Biome-based
            case ENV_RAINFALL:
                return new RainfallEnvironment( this, value );
            case ENV_BIOME_TEMPERATURE:
                return new BiomeTemperatureEnvironment( this, value );
            case ENV_TEMPERATURE:
                return new TemperatureEnvironment( this, value );
            case ENV_BIOME_CATEGORY:
                return new BiomeCategoryEnvironment( this, value );
            case ENV_BIOME:
                return value.endsWith( "*" ) ? new BiomeGroupEnvironment( this, value ) : new BiomeEnvironment( this, value );
            // Position-based
            case ENV_STRUCTURE:
                return value.endsWith( "*" ) ? new StructureGroupEnvironment( this, value ) : new StructureEnvironment( this, value );
            case ENV_Y:
                return new YEnvironment( this, value );
            case ENV_Y_FROM_SEA:
                return new YFromSeaEnvironment( this, value );
            case ENV_POSITION:
                return new PositionEnvironment( this, value );
            // Time-based
            case ENV_DIFFICULTY:
                return new DifficultyEnvironment( this, value );
            case ENV_SPECIAL_DIFFICULTY:
                return new SpecialDifficultyEnvironment( this, value );
            case ENV_WEATHER:
                return new WeatherEnvironment( this, value );
            case ENV_MOON_BRIGHTNESS:
                return new MoonBrightnessEnvironment( this, value );
            case ENV_MOON_PHASE:
                return new MoonPhaseEnvironment( this, value );
            case ENV_DAY_TIME:
                return new DayTimeEnvironment( this, value );
            case ENV_TIME_FROM_MIDNIGHT:
                return new TimeFromMidnightEnvironment( this, value );
            case ENV_WORLD_TIME:
                return new WorldTimeEnvironment( this, value );
            case ENV_CHUNK_TIME:
                return new ChunkTimeEnvironment( this, value );
        }
        
        // The environment name was not recognized; try to provide some good feedback because this field is complicated
        final String[] environmentNames = {
                // Dimension-based
                ENV_DIMENSION_PROPERTY, ENV_DIMENSION_TYPE,
                // Biome-based
                ENV_RAINFALL, ENV_BIOME_TEMPERATURE, ENV_TEMPERATURE, ENV_BIOME_CATEGORY, ENV_BIOME,
                // Position-based
                ENV_STRUCTURE, ENV_Y, ENV_Y_FROM_SEA, ENV_POSITION,
                // Time-based
                ENV_DIFFICULTY, ENV_SPECIAL_DIFFICULTY, ENV_WEATHER, ENV_MOON_BRIGHTNESS, ENV_MOON_PHASE, ENV_DAY_TIME,
                ENV_TIME_FROM_MIDNIGHT, ENV_WORLD_TIME, ENV_CHUNK_TIME
        };
        final AbstractEnvironment fallback = new YEnvironment( ComparisonOperator.LESS_THAN, 0 );
        SpecialMobs.LOG.warn( "Invalid environment '{}' for {} \"{}\"! Falling back to \"{}\". Environment name must be in the set [ {} ]. Invalid environment: {}",
                args[0], getClass(), getKey(), fallback, TomlHelper.literalList( (Object[]) environmentNames ), line );
        return fallback;
    }
    
    
    // Convenience methods
    
    /** @return The value matching the given environment, or the default value if no matching environment is defined. */
    public double getOrElse( World world, @Nullable BlockPos pos, DoubleField defaultValue ) { return get().getOrElse( world, pos, defaultValue ); }
    
    /** @return The value matching the given environment, or the default value if no matching environment is defined. */
    public double getOrElse( World world, @Nullable BlockPos pos, double defaultValue ) { return get().getOrElse( world, pos, defaultValue ); }
    
    /** @return The value matching the given environment, or null if no matching environment is defined. */
    @Nullable
    public Double get( World world, @Nullable BlockPos pos ) { return get().get( world, pos ); }
}