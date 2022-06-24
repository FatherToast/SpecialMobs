package fathertoast.specialmobs.common.bestiary;

public class BestiaryInfo {
    
    public enum BaseWeight {
        DEFAULT( 600 ),
        DISABLED( 0 ),
        LOWEST( DEFAULT.value / 8 ),
        LOW( DEFAULT.value / 4 ),
        HIGH( DEFAULT.value * 4 ),
        HIGHEST( DEFAULT.value * 8 );
        
        public final int value;
        
        BaseWeight( int v ) { value = v; }
    }
    
    //TODO default themes
    
    //	public static final EnvironmentListConfig DEFAULT_THEME_FIRE = new EnvironmentListConfig(
    //		new TargetEnvironment.TargetBiomeGroup( "desert", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "savanna", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "mesa", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetDimension( DimensionType.NETHER, BestiaryInfo.BASE_WEIGHT_COMMON ),
    //
    //		new TargetEnvironment.TargetBiomeGroup( "ice", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "frozen", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "cold", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "taiga_cold", BestiaryInfo.BASE_WEIGHT_RARE )
    //	);
    //
    //	public static final EnvironmentListConfig DEFAULT_THEME_ICE = new EnvironmentListConfig(
    //		new TargetEnvironment.TargetBiomeGroup( "ice", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "frozen", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "cold", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "taiga_cold", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //
    //		new TargetEnvironment.TargetBiomeGroup( "desert", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "savanna", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "mesa", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetDimension( DimensionType.NETHER, BestiaryInfo.BASE_WEIGHT_RARE )
    //	);
    //
    //	public static final EnvironmentListConfig DEFAULT_THEME_FOREST = new EnvironmentListConfig(
    //		new TargetEnvironment.TargetBiomeGroup( "swamp", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "forest", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "birch_forest", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "roofed_forest", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "jungle", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "taiga", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "redwood_taiga", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //
    //		new TargetEnvironment.TargetBiomeGroup( "desert", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "ice", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "stone", BestiaryInfo.BASE_WEIGHT_RARE )
    //	);
    //
    //	public static final EnvironmentListConfig DEFAULT_THEME_MOUNTAIN = new EnvironmentListConfig(
    //		new TargetEnvironment.TargetBiomeGroup( "extreme", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "smaller_extreme", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "mesa", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "ice_mountains", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "stone", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //
    //		new TargetEnvironment.TargetBiomeGroup( "plains", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "desert", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "swamp", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "beach", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "cold_beach", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "savanna", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "ice_flats", BestiaryInfo.BASE_WEIGHT_RARE )
    //	);
    //
    //	public static final EnvironmentListConfig DEFAULT_THEME_WATER = new EnvironmentListConfig(
    //		new TargetEnvironment.TargetBiomeGroup( "swamp", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiome( Biomes.BEACH, BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiome( Biomes.OCEAN, BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiome( Biomes.DEEP_OCEAN, BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiome( Biomes.FROZEN_OCEAN, BestiaryInfo.BASE_WEIGHT_COMMON ),
    //
    //		new TargetEnvironment.TargetBiomeGroup( "desert", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetBiomeGroup( "savanna", BestiaryInfo.BASE_WEIGHT_RARE ),
    //		new TargetEnvironment.TargetDimension( DimensionType.NETHER, 0 )
    //	);
    //
    //	public static final EnvironmentListConfig DEFAULT_THEME_FISHING = new EnvironmentListConfig(
    //		new TargetEnvironment.TargetBiomeGroup( "swamp", BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiome( Biomes.BEACH, BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiome( Biomes.OCEAN, BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiome( Biomes.DEEP_OCEAN, BestiaryInfo.BASE_WEIGHT_COMMON ),
    //		new TargetEnvironment.TargetBiome( Biomes.FROZEN_OCEAN, BestiaryInfo.BASE_WEIGHT_COMMON ),
    //
    //		new TargetEnvironment.TargetBiomeGroup( "desert", BestiaryInfo.BASE_WEIGHT_UNCOMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "savanna", BestiaryInfo.BASE_WEIGHT_UNCOMMON ),
    //		new TargetEnvironment.TargetBiomeGroup( "mesa", BestiaryInfo.BASE_WEIGHT_UNCOMMON )
    //	);
    
    // These can be set in the entity class to change these default values.
    //public EnvironmentListConfig weightExceptions = new EnvironmentListConfig( );
    
    public final int eggSpotsColor;
    public final BaseWeight weight;
    
    public BestiaryInfo( int eggColor ) { this( eggColor, BaseWeight.DEFAULT ); }
    
    public BestiaryInfo( int eggColor, BaseWeight baseWeight ) {
        eggSpotsColor = eggColor;
        weight = baseWeight;
    }
    
    public BestiaryInfo setTheme() {
        return this;//TODO theme builder
    }
}