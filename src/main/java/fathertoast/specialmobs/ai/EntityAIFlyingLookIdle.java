package fathertoast.specialmobs.ai;

import fathertoast.specialmobs.entity.*;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;

public
class EntityAIFlyingLookIdle< T extends EntityFlying & ISpecialMob > extends EntityAIBase
{
	private final T theEntity;
	
	public
	EntityAIFlyingLookIdle( T entity )
	{
		theEntity = entity;
		setMutexBits( MobHelper.AI_BIT_FACING );
	}
	
	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public
	boolean shouldExecute( )
	{
		return true;
	}
	
	/**
	 * Keep ticking a continuous task that has already been started
	 */
	@Override
	public
	void updateTask( )
	{
		EntityLivingBase target = theEntity.getAttackTarget( );
		SpecialMobData   data   = theEntity.getSpecialData( );
		
		if( target != null && target.getDistanceSq( theEntity ) < data.rangedAttackMaxRange * data.rangedAttackMaxRange ) {
			double dX = target.posX - theEntity.posX;
			double dZ = target.posZ - theEntity.posZ;
			theEntity.rotationYaw = (float) -MathHelper.atan2( dX, dZ ) * 180.0F / (float) Math.PI;
			theEntity.renderYawOffset = theEntity.rotationYaw;
		}
		else {
			theEntity.rotationYaw = (float) -MathHelper.atan2( theEntity.motionX, theEntity.motionZ ) * 180.0F / (float) Math.PI;
			theEntity.renderYawOffset = theEntity.rotationYaw;
		}
	}
}
