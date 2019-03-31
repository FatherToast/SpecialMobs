package fathertoast.specialmobs.entity.enderman;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

public
class EntityThiefEnderman extends Entity_SpecialEnderman
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x04fa00 );
		info.weight = BestiaryInfo.BASE_WEIGHT_UNCOMMON;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "thief" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "thief_eyes" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addGuaranteedDrop( "base", "Ender pearl", Items.ENDER_PEARL, 1 );
	}
	
	private int teleportTargetDelay;
	
	public
	EntityThiefEnderman( World world ) { super( world ); }
	
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
	
	@Override
	public
	void onLivingUpdate( )
	{
		if( !world.isRemote ) {
			teleportTargetDelay--;
		}
		super.onLivingUpdate( );
	}
	
	// Overridden to modify attack effects.
	@Override
	public
	void onTypeAttack( Entity target )
	{
		if( !world.isRemote && target instanceof EntityLivingBase && teleportTargetDelay <= 0 ) {
			for( int i = 64; i-- > 0; ) {
				if( teleportTargetRandomly( (EntityLivingBase) target ) ) {
					teleportTargetDelay = 120;
					
					for( int j = 16; j-- > 0; ) {
						if( teleportToEntity( target ) ) {
							break;
						}
					}
					break;
				}
			}
		}
	}
	
	private
	boolean teleportTargetRandomly( EntityLivingBase target )
	{
		double x = target.posX + (rand.nextDouble( ) - 0.5) * 32.0;
		double y = target.posY + (double) (rand.nextInt( 32 ) - 16);
		double z = target.posZ + (rand.nextDouble( ) - 0.5) * 32.0;
		
		EnderTeleportEvent event = new EnderTeleportEvent( target, x, y, z, 0 );
		if( MinecraftForge.EVENT_BUS.post( event ) )
			return false;
		boolean success = target.attemptTeleport( event.getTargetX( ), event.getTargetY( ), event.getTargetZ( ) );
		
		if( success ) {
			world.playSound( null, target.prevPosX, target.prevPosY, target.prevPosZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, getSoundCategory( ), 1.0F, 1.0F );
			target.playSound( SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F );
		}
		
		return success;
	}
}
