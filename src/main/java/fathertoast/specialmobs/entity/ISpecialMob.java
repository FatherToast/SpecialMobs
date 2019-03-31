package fathertoast.specialmobs.entity;

import net.minecraft.util.ResourceLocation;

public
interface ISpecialMob
{
	/**
	 * @return this mob's special data
	 */
	SpecialMobData getSpecialData( );
	
	/**
	 * Gets the experience that should be dropped by the entity.
	 */
	int getExperience( );
	
	/**
	 * Sets the experience that should be dropped by the entity.
	 */
	void setExperience( int xp );
	
	/**
	 * Sets the entity's immunity to fire status.
	 */
	void setImmuneToFire( boolean immune );
	
	/**
	 * @return All the textures for the entity.
	 */
	ResourceLocation[] getDefaultTextures( );
}
