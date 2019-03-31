package fathertoast.specialmobs.entity.projectile;

import fathertoast.specialmobs.ai.*;
import fathertoast.specialmobs.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public
class EntitySpecialFishHook extends Entity
{
	private static final DataParameter< Integer > ANGLER_ID = EntityDataManager.createKey( EntitySpecialFishHook.class, DataSerializers.VARINT );
	
	private EntityLiving angler;
	
	private final int maxLifetime;
	
	public
	EntitySpecialFishHook( World world )
	{
		super( world );
		maxLifetime = 60;
	}
	
	public
	EntitySpecialFishHook( World world, EntityLiving entity, Entity target )
	{
		super( world );
		setAngler( entity );
		((IAngler) entity).setFishHook( this );
		SpecialMobData data = ((ISpecialMob) entity).getSpecialData( );
		maxLifetime = Math.max( data.rangedAttackMaxCooldown, 30 );
		
		setLocationAndAngles(
			entity.posX - MathHelper.sin( entity.rotationYaw * (float) Math.PI / 180.0F ) * 0.3,
			entity.posY + entity.getEyeHeight( ),
			entity.posZ + MathHelper.cos( entity.rotationYaw * (float) Math.PI / 180.0F ) * 0.3,
			entity.rotationYaw, entity.rotationPitch
		);
		
		double dX = target.posX - posX;
		double dY = target.getEntityBoundingBox( ).minY + target.height / 2.0F - posY;
		double dZ = target.posZ - posZ;
		double dH = MathHelper.sqrt( dX * dX + dZ * dZ );
		
		shoot( dX, dY + dH * 0.2, dZ, 1.0F, data.rangedAttackSpread );
	}
	
	@Override
	protected
	void entityInit( )
	{
		getDataManager( ).register( ANGLER_ID, 0 );
		
		setSize( 0.25F, 0.25F );
		ignoreFrustumCheck = true;
	}
	
	// Gets the angler.
	public
	EntityLiving getAngler( )
	{
		if( angler == null ) {
			int anglerId = getDataManager( ).get( ANGLER_ID );
			if( anglerId > 0 ) {
				Entity entity = world.getEntityByID( anglerId - 1 );
				if( entity instanceof EntityLiving ) {
					angler = (EntityLiving) entity;
				}
			}
		}
		return angler;
	}
	
	// Sets the angler.
	public
	void setAngler( EntityLiving entity )
	{
		angler = entity;
		if( angler == null ) {
			getDataManager( ).set( ANGLER_ID, 0 );
		}
		else {
			getDataManager( ).set( ANGLER_ID, angler.getEntityId( ) + 1 );
		}
	}
	
	@SideOnly( Side.CLIENT )
	public
	boolean isInRangeToRenderDist( double distanceSq ) { return distanceSq < 4096.0; }
	
	@SideOnly( Side.CLIENT )
	public
	void setPositionAndRotationDirect( double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport ) { }
	
	public
	void shoot( double dX, double dY, double dZ, float velocity, float variance )
	{
		shoot( new Vec3d( dX, dY, dZ ).normalize( ), velocity, variance );
	}
	
	public
	void shoot( Vec3d direction, float velocity, float variance )
	{
		Vec3d motion = direction.add(
			new Vec3d( rand.nextGaussian( ), rand.nextGaussian( ), rand.nextGaussian( ) ).scale( variance * 0.0075 )
		).scale( velocity );
		
		motionX = motion.x;
		motionY = motion.y;
		motionZ = motion.z;
		
		float motionH = MathHelper.sqrt( motion.x * motion.x + motion.z * motion.z );
		prevRotationYaw = rotationYaw = (float) (MathHelper.atan2( motion.x, motion.z ) * 180.0 / Math.PI);
		prevRotationPitch = rotationPitch = (float) (MathHelper.atan2( motion.y, motionH ) * 180.0 / Math.PI);
	}
	
	@Override
	public
	void setVelocity( double vX, double vY, double vZ )
	{
		motionX = vX;
		motionY = vY;
		motionZ = vZ;
		if( prevRotationPitch == 0.0F && prevRotationYaw == 0.0F ) {
			float vH = MathHelper.sqrt( vX * vX + vZ * vZ );
			prevRotationYaw = rotationYaw = (float) (Math.atan2( vX, vZ ) * 180.0 / Math.PI);
			prevRotationPitch = rotationPitch = (float) (Math.atan2( vY, vH ) * 180.0 / Math.PI);
		}
	}
	
	@Override
	public
	void onUpdate( )
	{
		super.onUpdate( );
		
		if( ticksExisted < maxLifetime && getAngler( ) != null &&
		    (world.isRemote || !getAngler( ).isDead && getAngler( ).isEntityAlive( ) && getDistanceSq( getAngler( ) ) <= 2304.0)
		) {
			if( !world.isRemote ) {
				checkCollision( );
			}
			
			motionY -= 0.03;
			move( MoverType.SELF, motionX, motionY, motionZ );
			updateRotation( );
			
			double accelFactor = 0.92;
			motionX *= accelFactor;
			motionY *= accelFactor;
			motionZ *= accelFactor;
			
			setPosition( posX, posY, posZ );
		}
		else {
			setDead( );
		}
	}
	
	private
	void checkCollision( )
	{
		Vec3d pos     = new Vec3d( posX, posY, posZ );
		Vec3d nextPos = pos.addVector( motionX, motionY, motionZ );
		
		// Check if we hit any blocks
		RayTraceResult hit = world.rayTraceBlocks( pos, nextPos, false, true, false );
		if( hit != null ) {
			nextPos = hit.hitVec;
		}
		
		// Check if we hit any entities
		Entity closestEntityHit  = null;
		double closestDistanceSq = Double.POSITIVE_INFINITY;
		
		List< Entity > entitiesHit = world.getEntitiesWithinAABBExcludingEntity( this, getEntityBoundingBox( ).expand( motionX, motionY, motionZ ).grow( 1.0 ) );
		for( Entity entityHit : entitiesHit ) {
			if( entityHit != null && entityHit != getAngler( ) && entityHit.canBeCollidedWith( ) || entityHit instanceof EntityItem ) {
				AxisAlignedBB  boundingBox          = entityHit.getEntityBoundingBox( ).grow( 0.3 );
				RayTraceResult boundingBoxCollision = boundingBox.calculateIntercept( pos, nextPos );
				if( boundingBoxCollision != null ) {
					double distanceSq = pos.squareDistanceTo( boundingBoxCollision.hitVec );
					if( distanceSq < closestDistanceSq ) {
						closestEntityHit = entityHit;
						closestDistanceSq = distanceSq;
					}
				}
			}
		}
		if( closestEntityHit != null ) {
			hit = new RayTraceResult( closestEntityHit );
		}
		
		if( hit != null ) {
			onImpact( hit );
		}
	}
	
	private
	void onImpact( RayTraceResult hit )
	{
		if( hit.typeOfHit == RayTraceResult.Type.ENTITY && hit.entityHit != null && getAngler( ) != null ) {
			playSound( SoundEvents.ENTITY_BOBBER_RETRIEVE, 0.5F, 0.4F / (rand.nextFloat( ) * 0.4F + 0.8F) );
			
			Vec3d pullVec = new Vec3d(
				getAngler( ).posX - posX,
				getAngler( ).posY + getAngler( ).getEyeHeight( ) - posY,
				getAngler( ).posZ - posZ
			);
			double distance = pullVec.lengthVector( );
			pullVec = pullVec.scale( 0.42 );
			
			hit.entityHit.motionX = pullVec.x;
			hit.entityHit.motionY = pullVec.y + Math.sqrt( distance ) * 0.1;
			hit.entityHit.motionZ = pullVec.z;
			hit.entityHit.onGround = false;
			
			if( hit.entityHit instanceof EntityPlayerMP ) {
				try {
					((EntityPlayerMP) hit.entityHit).connection.sendPacket( new SPacketEntityVelocity( hit.entityHit ) );
				}
				catch( Exception ex ) {
					ex.printStackTrace( );
				}
			}
		}
		setDead( );
	}
	
	private
	void updateRotation( )
	{
		float vH = MathHelper.sqrt( motionX * motionX + motionZ * motionZ );
		rotationYaw = (float) (MathHelper.atan2( motionX, motionZ ) * 180.0 / Math.PI);
		rotationPitch = (float) (MathHelper.atan2( motionY, vH ) * 180.0 / Math.PI);
		
		while( rotationPitch - prevRotationPitch < -180.0F ) {
			prevRotationPitch -= 360.0F;
		}
		while( rotationPitch - prevRotationPitch >= 180.0F ) {
			prevRotationPitch += 360.0F;
		}
		while( rotationYaw - prevRotationYaw < -180.0F ) {
			prevRotationYaw -= 360.0F;
		}
		while( rotationYaw - prevRotationYaw >= 180.0F ) {
			prevRotationYaw += 360.0F;
		}
		
		rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
		rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
	}
	
	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
	 * prevent them from trampling crops
	 */
	@Override
	protected
	boolean canTriggerWalking( ) { return false; }
	
	/**
	 * Will get destroyed next tick.
	 */
	@Override
	public
	void setDead( )
	{
		super.setDead( );
		if( getAngler( ) != null ) {
			((IAngler) getAngler( )).setFishHook( null );
		}
	}
	
	@Override
	public
	void writeEntityToNBT( NBTTagCompound tag ) { }
	
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag ) { }
}
