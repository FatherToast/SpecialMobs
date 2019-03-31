package fathertoast.specialmobs.config;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

@SuppressWarnings( { "WeakerAccess", "unused" } )
public
class EntityListConfig
{
	// The entity descriptions in this set.
	private final EntryEntity[] ENTRIES;
	
	public
	EntityListConfig( String line )
	{
		this( line.split( "," ) );
	}
	
	public
	EntityListConfig( String[] list )
	{
		ArrayList< EntryEntity > entryList = new ArrayList< EntryEntity >( );
		for( String item : list ) {
			boolean extendable = true;
			if( item.startsWith( "~" ) ) {
				item = item.substring( 1 );
				extendable = false;
			}
			String[]                  itemList    = item.split( " " );
			Class< ? extends Entity > entityClass = EntityList.getClass( new ResourceLocation( itemList[ 0 ].trim( ) ) );
			if( entityClass != null ) {
				entryList.add( new EntryEntity( entityClass, extendable, itemList ) );
			}
			else {
				Config.log.error( "Invalid entity id! ({})", item );
			}
		}
		this.ENTRIES = entryList.toArray( new EntryEntity[ 0 ] );
	}
	
	// Returns true if the entity is contained in this list.
	public
	boolean contains( Entity entity )
	{
		EntryEntity entry = new EntryEntity( entity.getClass( ) );
		for( EntryEntity currentEntry : this.ENTRIES ) {
			if( currentEntry.contains( entry ) )
				return true;
		}
		return false;
	}
	
	// Returns the float array of chances for the entry. Returns null if the entity is not contained in the set.
	public
	float[] getChances( Entity entity )
	{
		EntryEntity entry        = new EntryEntity( entity.getClass( ) );
		EntryEntity bestMatch    = null;
		float[]     matchChances = null;
		for( EntryEntity currentEntry : this.ENTRIES ) {
			if( currentEntry.contains( entry ) && (bestMatch == null || bestMatch.contains( currentEntry )) ) {
				bestMatch = currentEntry;
				matchChances = currentEntry.VALUES;
			}
		}
		return matchChances;
	}
}