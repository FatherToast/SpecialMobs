package fathertoast.specialmobs.config;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings( { "WeakerAccess", "unused" } )
public
class EnvironmentListConfig
{
	// The environment descriptions in this set.
	final List< TargetEnvironment > ENTRIES = new ArrayList<>( );
	
	public
	EnvironmentListConfig( TargetEnvironment... targets )
	{
		ENTRIES.addAll( Arrays.asList( targets ) );
		Collections.sort( ENTRIES );
	}
	
	EnvironmentListConfig( String line )
	{
		this( line.split( "," ) );
	}
	
	EnvironmentListConfig( String[] list )
	{
		for( String item : list ) {
			TargetEnvironment target = TargetEnvironment.read( item );
			if( target != null ) {
				ENTRIES.add( target );
			}
		}
		Collections.sort( ENTRIES );
	}
	
	public
	float getValueForLocation( World world, BlockPos pos, float defaultValue )
	{
		if( !ENTRIES.isEmpty( ) ) {
			LocationInfo location = new LocationInfo( world, pos );
			for( TargetEnvironment target : ENTRIES ) {
				if( target.applies( location ) ) {
					return target.getValue( );
				}
			}
		}
		return defaultValue;
	}
	
	static
	class LocationInfo
	{
		final World    theWorld;
		final BlockPos worldPos;
		
		final int           dimId;
		final DimensionType dimType;
		
		final Biome            exactBiome;
		final Biome            biome;
		final ResourceLocation biomeName;
		
		LocationInfo( World world, BlockPos pos )
		{
			theWorld = world;
			worldPos = pos;
			
			dimId = world.provider.getDimension( );
			dimType = world.provider.getDimensionType( );
			
			exactBiome = world.getBiome( worldPos );
			Biome baseMutation = Biome.getMutationForBiome( exactBiome );
			biome = baseMutation != null ? baseMutation : exactBiome;
			biomeName = biome.getRegistryName( );
		}
	}
}
