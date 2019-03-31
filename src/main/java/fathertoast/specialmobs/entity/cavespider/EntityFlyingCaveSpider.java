package fathertoast.specialmobs.entity.cavespider;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityFlyingCaveSpider extends Entity_SpecialCaveSpider
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x6388b2 );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_MOUNTAIN;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "flying" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "flying_eyes" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Feathers", Items.FEATHER );
	}
	
	public
	EntityFlyingCaveSpider( World world ) { super( world ); }
	
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
		
		getSpecialData( ).setFallDamageMultiplier( 0.0F );
		
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
			if( distance > 6.0F && distance < 12.0F && onGround && rand.nextInt( 10 ) == 0 ) {
				double vX = target.posX - posX;
				double vZ = target.posZ - posZ;
				double vH = Math.sqrt( vX * vX + vZ * vZ );
				motionX = vX / vH * 2.0 + motionX * 0.2;
				motionY = 0.4 * 2.0;
				motionZ = vZ / vH * 2.0 + motionZ * 0.2;
			}
		}
		super.onLivingUpdate( );
	}
}
