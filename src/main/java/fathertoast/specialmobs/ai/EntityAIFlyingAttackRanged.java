package fathertoast.specialmobs.ai;

import fathertoast.specialmobs.entity.*;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

public
class EntityAIFlyingAttackRanged< T extends EntityFlying & ISpecialMob & IRangedAttackMob > extends EntityAIBase
{
	private final T theEntity;
	
	public int attackTimer;
	
	public
	EntityAIFlyingAttackRanged( T entity )
	{
		theEntity = entity;
		setMutexBits( MobHelper.AI_BIT_NONE );
	}
	
	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public
	boolean shouldExecute( )
	{
		return theEntity.getAttackTarget( ) != null;
	}
	
	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public
	void startExecuting( )
	{
		attackTimer = 0;
	}
	
	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	@Override
	public
	void resetTask( )
	{
		theEntity.setSwingingArms( false );
	}
	
	/**
	 * Keep ticking a continuous task that has already been started
	 */
	@Override
	public
	void updateTask( )
	{
		EntityLivingBase target = theEntity.getAttackTarget( );
		if( target == null ) {
			return;
		}
		SpecialMobData data = theEntity.getSpecialData( );
		
		if( target.getDistanceSq( theEntity ) < data.rangedAttackMaxRange * data.rangedAttackMaxRange && theEntity.canEntityBeSeen( target ) ) {
			attackTimer++;
			
			if( attackTimer == (data.rangedAttackCooldown >> 1) ) {
				// This should probably get changed if wee ever apply this ai to non-ghast mobs
				theEntity.world.playEvent( null, 1015, new BlockPos( theEntity ), 0 );
			}
			
			if( attackTimer >= data.rangedAttackCooldown ) {
				attackTimer = data.rangedAttackCooldown - data.rangedAttackMaxCooldown;
				float distanceFactor = Math.max( 0.1F, target.getDistance( theEntity ) / data.rangedAttackMaxRange );
				theEntity.attackEntityWithRangedAttack( target, distanceFactor );
			}
		}
		else if( attackTimer > 0 ) {
			attackTimer--;
		}
		
		theEntity.setSwingingArms( attackTimer > (data.rangedAttackCooldown >> 1) );
	}
}
