package fathertoast.specialmobs.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIAttackMelee;

/**
 * This is a simple tweak to the melee attack ai to make use of the ranged attack mob hooks.
 * Essentially copied from the abstract skeleton template.
 */
public
class EntityAISpecialAttackMelee< T extends EntityCreature & IRangedAttackMob > extends EntityAIAttackMelee
{
	private final T theEntity;
	
	public
	EntityAISpecialAttackMelee( T entity, double speedMult, boolean useLongMemory )
	{
		super( entity, speedMult, useLongMemory );
		theEntity = entity;
	}
	
	@Override
	public
	void resetTask( )
	{
		super.resetTask( );
		theEntity.setSwingingArms( false );
	}
	
	@Override
	public
	void startExecuting( )
	{
		super.startExecuting( );
		theEntity.setSwingingArms( true );
	}
}
