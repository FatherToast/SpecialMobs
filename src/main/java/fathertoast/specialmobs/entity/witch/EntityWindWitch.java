package fathertoast.specialmobs.entity.witch;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

public
class EntityWindWitch extends Entity_SpecialWitch
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
		new ResourceLocation( GET_TEXTURE_PATH( "wind" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Feathers", Items.FEATHER );
		loot.addSemicommonDrop( "semicommon", "Ender pearl", Items.ENDER_PEARL );
	}
	
	// Ticks before this entity can teleport.
	private int teleportDelay;
	
	public
	EntityWindWitch( World world )
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
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 2.0 );
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 1.2 );
	}
	
	// Called every tick while this entity is alive.
	@Override
	public
	void onLivingUpdate( )
	{
		if( !world.isRemote && isEntityAlive( ) && teleportDelay-- <= 0 && getAttackTarget( ) != null && rand.nextInt( 20 ) == 0 ) {
			if( getAttackTarget( ).getDistanceSq( this ) > 36.0 ) {
				for( int i = 0; i < 16; i++ ) {
					if( teleportToEntity( getAttackTarget( ) ) ) {
						teleportDelay = 60;
						removePotionEffect( MobEffects.INVISIBILITY );
						break;
					}
				}
			}
			else {
				addPotionEffect( new PotionEffect( MobEffects.INVISIBILITY, 30 ) );
				for( int i = 0; i < 16; i++ ) {
					if( teleportRandomly( ) ) {
						teleportDelay = 30;
						break;
					}
				}
			}
		}
		super.onLivingUpdate( );
	}
	
	// Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
	@Override
	public
	boolean attackEntityFrom( DamageSource damageSource, float damage )
	{
		if( !world.isRemote && damageSource.getTrueSource( ) != null ) {
			teleportDelay -= 15;
			if( teleportDelay <= 0 && (damageSource instanceof EntityDamageSourceIndirect || rand.nextBoolean( )) ) {
				double xI = posX;
				double yI = posY;
				double zI = posZ;
				
				for( int i = 0; i < 64; i++ ) {
					if( teleportRandomly( ) ) {
						teleportDelay = 30;
						addPotionEffect( new PotionEffect( MobEffects.INVISIBILITY, 30 ) );
						if( damageSource instanceof EntityDamageSourceIndirect )
							return true;
						boolean hit = super.attackEntityFrom( damageSource, damage );
						
						if( getHealth( ) <= 0.0F ) {
							setPosition( xI, yI, zI );
						}
						return hit;
					}
				}
			}
			else {
				removePotionEffect( MobEffects.INVISIBILITY );
			}
		}
		return super.attackEntityFrom( damageSource, damage );
	}
	
	// Used by the enderman-like AI.
	private
	boolean teleportRandomly( )
	{
		double targetX = posX + (rand.nextDouble( ) - 0.5) * 20.0;
		double targetY = posY + (double) (rand.nextInt( 12 ) - 4);
		double targetZ = posZ + (rand.nextDouble( ) - 0.5) * 20.0;
		return teleportTo( targetX, targetY, targetZ );
	}
	
	// Used by the enderman-like AI.
	private
	boolean teleportToEntity( Entity entity )
	{
		Vec3d vec3d = new Vec3d( posX - entity.posX, getEntityBoundingBox( ).minY + (double) (height / 2.0F) - entity.posY + (double) entity.getEyeHeight( ), posZ - entity.posZ );
		vec3d = vec3d.normalize( );
		double targetX = posX + (rand.nextDouble( ) - 0.5) * 8.0 - vec3d.x * 16.0;
		double targetY = posY + (double) (rand.nextInt( 8 ) - 2) - vec3d.y * 16.0;
		double targetZ = posZ + (rand.nextDouble( ) - 0.5) * 8.0 - vec3d.z * 16.0;
		return teleportTo( targetX, targetY, targetZ );
	}
	
	// Used by the enderman-like AI.
	private
	boolean teleportTo( double x, double y, double z )
	{
		EnderTeleportEvent event = new EnderTeleportEvent( this, x, y, z, 0 );
		if( MinecraftForge.EVENT_BUS.post( event ) )
			return false;
		boolean success = attemptTeleport( event.getTargetX( ), event.getTargetY( ), event.getTargetZ( ) );
		
		if( success ) {
			world.playSound( null, prevPosX, prevPosY, prevPosZ, SoundEvents.ENTITY_GHAST_SHOOT, getSoundCategory( ), 1.0F, 1.0F );
			playSound( SoundEvents.ENTITY_WITHER_SHOOT, 0.4F, 0.6F );
		}
		
		return success;
	}
}
