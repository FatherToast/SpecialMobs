package fathertoast.specialmobs.entity.creeper;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityJumpingCreeper extends Entity_SpecialCreeper
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x7d6097 );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_MOUNTAIN;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "jumping" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addSemicommonDrop( "semicommon", "Slime ball", Items.SLIME_BALL );
	}
	
	public
	EntityJumpingCreeper( World world )
	{
		super( world );
		getSpecialData( ).setFallDamageMultiplier( 0.0F );
	}
	
	@Override
	public
	ResourceLocation[] getDefaultTextures( ) { return TEXTURES; }
	
	@Override
	protected
	ResourceLocation getLootTable( ) { return LOOT_TABLE; }
	
	/** Called to modify the mob's attributes based on the variant. */
	@Override
	protected
	void applyTypeAttributes( )
	{
		experienceValue += 2;
		
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 1.2 );
	}
	
	// Called every tick while this entity is alive.
	@Override
	public
	void onLivingUpdate( )
	{
		Entity target = getAttackTarget( );
		if( target != null ) {
			float distance = getDistance( target );
			if( distance > 6.0F && distance < 10.0F && onGround && rand.nextInt( 10 ) == 0 ) {
				double vX = target.posX - posX;
				double vZ = target.posZ - posZ;
				double vH = Math.sqrt( vX * vX + vZ * vZ );
				motionX = vX / vH * 1.3 + motionX * 0.2;
				motionY = 0.4 * 2.0;
				motionZ = vZ / vH * 1.3 + motionZ * 0.2;
			}
		}
		super.onLivingUpdate( );
	}
}
