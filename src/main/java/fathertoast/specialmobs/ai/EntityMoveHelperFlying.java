package fathertoast.specialmobs.ai;

import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public
class EntityMoveHelperFlying extends EntityMoveHelper
{
	private final EntityFlying theEntity;
	
	private int courseChangeCooldown;
	
	public
	EntityMoveHelperFlying( EntityFlying entity )
	{
		super( entity );
		theEntity = entity;
	}
	
	@Override
	public
	void onUpdateMoveHelper( )
	{
		if( action == EntityMoveHelper.Action.MOVE_TO ) {
			float moveSpeed = (float) (speed * theEntity.getEntityAttribute( SharedMonsterAttributes.MOVEMENT_SPEED ).getAttributeValue( ));
			
			if( courseChangeCooldown-- <= 0 ) {
				courseChangeCooldown += theEntity.getRNG( ).nextInt( 5 ) + 2;
				
				Vec3d  moveVec  = getMovementVector( theEntity );
				double distance = moveVec.lengthVector( );
				if( EntityMoveHelperFlying.isPathTraversable( theEntity, moveVec, distance ) ) {
					theEntity.motionX += moveVec.x / distance * moveSpeed;
					theEntity.motionY += moveVec.y / distance * moveSpeed;
					theEntity.motionZ += moveVec.z / distance * moveSpeed;
				}
				else {
					action = EntityMoveHelper.Action.WAIT;
				}
			}
		}
	}
	
	// Helper methods for entities using the flying move helper ================
	
	public static
	void setRandomPath( EntityFlying entity, float diameter, double speed )
	{
		entity.getMoveHelper( ).setMoveTo(
			entity.posX + (entity.getRNG( ).nextFloat( ) - 0.5F) * diameter,
			entity.posY + (entity.getRNG( ).nextFloat( ) - 0.5F) * diameter,
			entity.posZ + (entity.getRNG( ).nextFloat( ) - 0.5F) * diameter,
			speed
		);
	}
	
	/**
	 * Returns the current movement vector
	 */
	public static
	Vec3d getMovementVector( EntityFlying entity )
	{
		return new Vec3d(
			entity.getMoveHelper( ).getX( ) - entity.posX,
			entity.getMoveHelper( ).getY( ) - entity.posY,
			entity.getMoveHelper( ).getZ( ) - entity.posZ
		);
	}
	
	/**
	 * Checks if entity bounding box is not colliding with terrain
	 * by casting the entity's bounding box along the movement vector with a step size of 1 block
	 */
	public static
	boolean isPathTraversable( EntityFlying entity )
	{
		Vec3d moveVec = getMovementVector( entity );
		return isPathTraversable( entity, moveVec, moveVec.lengthVector( ) );
	}
	
	/**
	 * Checks if entity bounding box is not colliding with terrain
	 * by casting the entity's bounding box along the movement vector with a step size of 1 block
	 */
	public static
	boolean isPathTraversable( EntityFlying entity, Vec3d moveVec, double distance )
	{
		Vec3d         stepVec     = moveVec.scale( 1.0 / distance );
		AxisAlignedBB boundingBox = entity.getEntityBoundingBox( );
		for( int i = 1; i < distance; i++ ) {
			boundingBox = boundingBox.offset( stepVec );
			
			if( !entity.world.getCollisionBoxes( entity, boundingBox ).isEmpty( ) ) {
				return false;
			}
		}
		
		return true;
	}
}
