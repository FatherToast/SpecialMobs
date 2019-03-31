package fathertoast.specialmobs.ai;

import fathertoast.specialmobs.entity.*;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;

public
class EntityAIFlyingAttackMelee< T extends EntityFlying & ISpecialMob > extends EntityAIBase
{
	private final T theEntity;
	
	private int attackTimer;
	private int pathUpdateCooldown;
	
	public
	EntityAIFlyingAttackMelee( T entity )
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
		
		EntityLivingBase target = theEntity.getAttackTarget( );
		if( target == null ) {
			theEntity.getMoveHelper( ).setMoveTo(
				target.posX,
				target.posY + target.height / 2.0F,
				target.posZ,
				1.0
			);
		}
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
		
		// Move towards the target
		if( pathUpdateCooldown-- <= 0 || theEntity.getMoveHelper( ).action == EntityMoveHelper.Action.WAIT ) {
			pathUpdateCooldown = theEntity.getRNG( ).nextInt( 5 ) + 3;
			
			theEntity.getMoveHelper( ).setMoveTo(
				target.posX,
				target.posY + target.height / 2.0F,
				target.posZ,
				1.0
			);
			if( !EntityMoveHelperFlying.isPathTraversable( theEntity ) ) {
				EntityMoveHelperFlying.setRandomPath( theEntity, 8.0F, 0.7 );
			}
		}
		
		// Attack the target when able
		if( attackTimer > 0 ) {
			attackTimer--;
		}
		else {
			double reachSq = theEntity.width * theEntity.width + target.width * target.width;
			if( target.getDistanceSq( theEntity ) < reachSq ) {
				attackTimer = 20;
				theEntity.attackEntityAsMob( target );
			}
		}
	}
}
