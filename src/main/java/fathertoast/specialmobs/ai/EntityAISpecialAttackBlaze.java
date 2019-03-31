package fathertoast.specialmobs.ai;

import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.entity.blaze.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;

/**
 * The attack ai for blazes. Unlike the ai used by other entities, this ai handles both melee and ranged behaviors.
 */
public
class EntityAISpecialAttackBlaze extends EntityAIBase
{
	private static final int ATTACK_STAGE_UNCHARGED = 0;
	private static final int ATTACK_STAGE_CHARGED   = 1;
	private static final int ATTACK_STAGE_SHOOTING  = 2;
	
	private final Entity_SpecialBlaze blaze;
	
	private int attackTime;
	private int attackStage;
	
	public
	EntityAISpecialAttackBlaze( Entity_SpecialBlaze entity )
	{
		blaze = entity;
		setMutexBits( MobHelper.AI_BIT_MOVEMENT | MobHelper.AI_BIT_FACING );
	}
	
	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public
	boolean shouldExecute( )
	{
		EntityLivingBase entitylivingbase = blaze.getAttackTarget( );
		return entitylivingbase != null && entitylivingbase.isEntityAlive( );
	}
	
	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public
	void startExecuting( )
	{
		attackStage = ATTACK_STAGE_UNCHARGED;
	}
	
	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public
	void resetTask( )
	{
		blaze.setOnFire( false );
	}
	
	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public
	void updateTask( )
	{
		attackTime--;
		
		EntityLivingBase target = blaze.getAttackTarget( );
		if( target == null ) {
			return;
		}
		SpecialMobData data = blaze.getSpecialData( );
		
		double distanceSq = blaze.getDistanceSq( target );
		float  rangeSq    = data.rangedAttackMaxRange * data.rangedAttackMaxRange;
		if( distanceSq < 4.0 ) {
			if( attackTime <= 0 ) {
				attackTime = 20;
				blaze.attackEntityAsMob( target );
			}
			
			blaze.getMoveHelper( ).setMoveTo( target.posX, target.posY, target.posZ, 1.0 );
		}
		else if( distanceSq < rangeSq ) {
			
			if( attackTime <= 0 ) {
				attackStage++;
				
				if( attackStage == ATTACK_STAGE_CHARGED ) {
					attackTime = data.rangedAttackMaxCooldown - data.rangedAttackCooldown;
					blaze.setOnFire( true );
				}
				else if( attackStage < ATTACK_STAGE_SHOOTING + blaze.fireballBurstCount ) {
					attackTime = blaze.fireballBurstDelay;
				}
				else {
					attackTime = data.rangedAttackCooldown;
					attackStage = ATTACK_STAGE_UNCHARGED;
					blaze.setOnFire( false );
				}
				
				if( attackStage >= ATTACK_STAGE_SHOOTING ) {
					float distanceFactor = MathHelper.clamp( (float) (rangeSq - distanceSq) / rangeSq, 0.1F, 1.0F );
					blaze.attackEntityWithRangedAttack( target, distanceFactor );
				}
			}
			
			blaze.getLookHelper( ).setLookPositionWithEntity( target, 10.0F, 10.0F );
		}
		else {
			blaze.getNavigator( ).clearPath( );
			blaze.getMoveHelper( ).setMoveTo( target.posX, target.posY, target.posZ, 1.0 );
		}
		
		super.updateTask( );
	}
}
