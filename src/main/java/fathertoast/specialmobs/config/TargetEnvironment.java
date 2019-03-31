package fathertoast.specialmobs.config;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;

/**
 * Used to store config data relative to the world. Smaller-scope environments (eg Biome) take priority over larger ones (eg Dimension).
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
@SuppressWarnings( { "WeakerAccess", "unused" } )
public abstract
class TargetEnvironment implements Comparable< TargetEnvironment >
{
	public static
	TargetEnvironment read( String line )
	{
		// Get the value parameter
		String[] itemList = line.split( "=", 2 );
		float    value;
		if( itemList.length < 2 ) {
			Config.log.error( "Ignoring config line '{}' - contains no value parameter", line );
			return null;
		}
		try {
			value = Float.parseFloat( itemList[ 1 ].trim( ) );
		}
		catch( Exception ex ) {
			Config.log.error( "Exception occurred while reading config line '{}'", line, ex );
			return null;
		}
		
		// Load depending on target type
		String[] target = itemList[ 0 ].trim( ).split( "/", 2 );
		if( target.length < 2 ) {
			Config.log.error( "Ignoring config line '{}' - must declare environment (e.g., 'biome:' or 'dimension:')", line );
		}
		else if( target[ 0 ].equalsIgnoreCase( "biome" ) ) {
			if( !target[ 1 ].endsWith( "*" ) ) {
				return new TargetBiome( 0, value, target[ 1 ] );
			}
			else {
				return new TargetBiomeGroup( 1, value, target[ 1 ].substring( target[ 1 ].length( ) - 1 ) );
			}
		}
		else if( target[ 0 ].equalsIgnoreCase( "dimension" ) ) {
			return new TargetDimension( 2, value, target[ 1 ] );
		}
		else {
			Config.log.error( "Ignoring config line '{}' - unrecognized environment '{}'", line, target[ 0 ] );
		}
		return null;
	}
	
	private final int   priority;
	private final float value;
	
	TargetEnvironment( int scope, float val )
	{
		priority = scope;
		value = val;
	}
	
	public
	float getValue( ) { return value; }
	
	public abstract
	boolean applies( EnvironmentListConfig.LocationInfo location );
	
	@Override
	public
	int compareTo( TargetEnvironment other ) { return priority - other.priority; }
	
	@Override
	public
	String toString( ) { return "=" + value; }
	
	public static
	class TargetBiome extends TargetEnvironment
	{
		private final ResourceLocation registryName;
		private final int              intId;
		
		// Used to make on-demand targets
		public
		TargetBiome( Biome biome, float val )
		{
			super( -1, val );
			registryName = biome.getRegistryName( );
			intId = Biome.REGISTRY.getIDForObject( biome );
		}
		
		TargetBiome( int scope, float val, String biomeId )
		{
			super( scope, val );
			
			Biome biome = Biome.REGISTRY.getObject( new ResourceLocation( biomeId ) );
			if( biome != null ) {
				registryName = biome.getRegistryName( );
				intId = Biome.REGISTRY.getIDForObject( biome );
			}
			else {
				ResourceLocation regName = null;
				int              id      = -1;
				try {
					id = Integer.parseInt( biomeId );
					biome = Biome.REGISTRY.getObjectById( id );
					if( biome != null ) {
						regName = Biome.REGISTRY.getNameForObject( biome );
						Config.log.warn( "Numerical id (biome/{}) used for biome with string id 'biome/{}'! " +
						                 "Please avoid using numerical ids.", id, regName );
					}
					else {
						Config.log.info( "Biome with numerical id 'biome/{}' is invalid or not yet registered. " +
						                 "Please set the biome's mod before this mod in the load order. " +
						                 "Also stop using numerical ids, you hooligan.", id );
					}
				}
				catch( NumberFormatException ex ) {
					regName = new ResourceLocation( biomeId );
					Config.log.info( "Biome 'biome/{}' is invalid or not yet registered. " +
					                 "Please set the biome's mod before this mod in the load order.", regName );
				}
				registryName = regName;
				intId = id;
			}
		}
		
		@Override
		public
		boolean applies( EnvironmentListConfig.LocationInfo location )
		{
			return registryName != null ?
			       registryName.equals( location.biomeName ) :
			       intId == Biome.REGISTRY.getIDForObject( location.biome );
		}
		
		@Override
		public
		String toString( ) { return "biome/" + registryName + super.toString( ); }
	}
	
	public static
	class TargetBiomeGroup extends TargetEnvironment
	{
		private final String registryNamePrefix;
		
		// Used to make on-demand targets
		public
		TargetBiomeGroup( String prefix, float val )
		{
			super( -1, val );
			registryNamePrefix = new ResourceLocation( prefix ).toString( );
		}
		
		TargetBiomeGroup( int scope, float val, String biomeId )
		{
			super( scope, val );
			if( biomeId.isEmpty( ) ) {
				Config.log.warn( "Detected empty biome group string 'biome/*' - this matches all biomes in the 'minecraft:' namespace. " +
				                 "Please use 'biome/minecraft:*' instead if this is your intended purpose!" );
			}
			registryNamePrefix = new ResourceLocation( biomeId ).toString( );
		}
		
		@Override
		public
		boolean applies( EnvironmentListConfig.LocationInfo location )
		{
			return location.biomeName.toString( ).startsWith( registryNamePrefix );
		}
		
		@Override
		public
		String toString( ) { return "biome/" + registryNamePrefix + "*" + super.toString( ); }
	}
	
	public static
	class TargetDimension extends TargetEnvironment
	{
		private final String dimensionName;
		private final int    intId;
		
		// Used to make on-demand targets
		public
		TargetDimension( DimensionType dimensionType, float val )
		{
			super( -1, val );
			dimensionName = dimensionType.getName( );
			intId = dimensionType.getId( );
		}
		
		TargetDimension( int scope, float val, String dimensionId )
		{
			super( scope, val );
			
			DimensionType dimType = null;
			for( DimensionType type : DimensionType.values( ) ) {
				if( type.getName( ).equalsIgnoreCase( dimensionId ) ) {
					dimType = type;
					break;
				}
			}
			if( dimType != null ) {
				dimensionName = dimType.getName( );
				intId = dimType.getId( );
			}
			else {
				String dimName = null;
				int    id      = -70457;
				try {
					id = Integer.parseInt( dimensionId );
					for( DimensionType type : DimensionType.values( ) ) {
						if( type.getId( ) == id ) {
							dimType = type;
							break;
						}
					}
					if( dimType != null ) {
						dimName = dimType.getName( );
						Config.log.warn( "Numerical id (dimension/{}) used for dimension with string id 'dimension/{}'! " +
						                 "Please avoid using numerical ids.", id, dimName );
					}
					else {
						Config.log.info( "Dimension with numerical id 'dimension/{}' is invalid or not yet registered. " +
						                 "Please set the dimension's mod before this mod in the load order. " +
						                 "Also stop using numerical ids, you hooligan.", id );
					}
				}
				catch( NumberFormatException ex ) {
					dimName = dimensionId;
					Config.log.info( "Dimension 'dimension/{}' is invalid or not yet registered. " +
					                 "Please set the dimension's mod before this mod in the load order.", dimName );
				}
				dimensionName = dimName;
				intId = id;
			}
		}
		
		@Override
		public
		boolean applies( EnvironmentListConfig.LocationInfo location )
		{
			return dimensionName != null && location.dimType != null ?
			       dimensionName.equals( location.dimType.getName( ) ) :
			       intId == location.dimId;
		}
		
		@Override
		public
		String toString( ) { return "dimension/" + dimensionName + super.toString( ); }
	}
}
