package fathertoast.specialmobs.config;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings( { "WeakerAccess", "unused" } )
public
class EntryEntity
{
	// The class this entry is defined for.
	public final Class< ? extends Entity > CLASS;
	// True if any class extending CLASS is considered within this entry.
	public final boolean                   EXTEND;
	// The values given to this entry (0 to 1). Null for comparison objects.
	public final float[]                   VALUES;
	
	// Constructor used to compare entity classes with the entries in an EntityListConfig.
	public
	EntryEntity( Class< ? extends Entity > entityClass )
	{
		this( entityClass, false, (float[]) null );
	}
	
	// Constructors used for default properties.
	public
	EntryEntity( Class< ? extends Entity > entityClass, float... vals )
	{
		this( entityClass, true, vals );
	}
	
	public
	EntryEntity( Class< ? extends Entity > entityClass, boolean extend, float... vals )
	{
		this.CLASS = entityClass;
		this.EXTEND = extend;
		this.VALUES = vals;
	}
	
	// Constructor used by the property reader.
	public
	EntryEntity( Class< ? extends Entity > entityClass, boolean extend, String[] vals )
	{
		this.CLASS = entityClass;
		this.EXTEND = extend;
		this.VALUES = new float[ vals.length - 1 ];
		
		for( int i = 0; i < this.VALUES.length; i++ ) {
			float val;
			try {
				val = Float.parseFloat( vals[ i + 1 ].trim( ) );
			}
			catch( NumberFormatException ex ) {
				val = -1.0F;
			}
			
			if( val >= 0.0F && val <= 1.0F ) {
				this.VALUES[ i ] = val;
			}
		}
	}
	
	// Returns true if the given entity description is contained within this one (is more specific).
	public
	boolean contains( EntryEntity entry )
	{
		if( this.CLASS == entry.CLASS )
			return !entry.EXTEND;
		if( this.EXTEND )
			return this.CLASS.isAssignableFrom( entry.CLASS );
		return false;
	}
	
	@Override
	public
	String toString( )
	{
		ResourceLocation resource = EntityList.getKey( this.CLASS );
		StringBuilder    str      = new StringBuilder( resource == null ? "null" : resource.toString( ) );
		if( !this.EXTEND )
			str.insert( 0, '~' );
		if( this.VALUES != null && this.VALUES.length > 0 )
			for( float val : this.VALUES ) {
				str.append( ' ' ).append( val );
			}
		return str.toString( );
	}
}
