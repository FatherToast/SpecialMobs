package fathertoast.specialmobs.ai;

import fathertoast.specialmobs.entity.*;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.Vec3d;

public
class EntityAIFlyingWander< T extends EntityFlying & ISpecialMob > extends EntityAIBase
{
	private final T theEntity;
	
	public
	EntityAIFlyingWander( T entity )
	{
		theEntity = entity;
		setMutexBits( MobHelper.AI_BIT_MOVEMENT );
	}
	
	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public
	boolean shouldExecute( )
	{
		EntityMoveHelper moveHelper = theEntity.getMoveHelper( );
		
		if( !moveHelper.isUpdating( ) ) {
			return true;
		}
		else {
			Vec3d  moveVec    = EntityMoveHelperFlying.getMovementVector( theEntity );
			double distanceSq = moveVec.lengthSquared( );
			return distanceSq < 1.0 || distanceSq > 3600.0;
		}
	}
	
	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public
	boolean shouldContinueExecuting( )
	{
		return false;
	}
	
	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public
	void startExecuting( )
	{
		EntityMoveHelperFlying.setRandomPath( theEntity, 32.0F, 1.0 );
	}
}
