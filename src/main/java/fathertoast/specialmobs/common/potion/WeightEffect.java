package fathertoast.specialmobs.common.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;

/**
 * This effect applies downward acceleration to the entity and multiplies fall distance by amplifier.
 * Only functions when the entity is at least one block above a solid block so that it doesn't hinder basic movement.
 * <p>
 * The pull effect is equivalent to one downward bubble column per effect level.
 *
 * @see net.minecraft.entity.Entity#onInsideBubbleColumn(boolean)
 */
public class WeightEffect extends Effect {
    
    public WeightEffect( EffectType type, int color ) { super( type, color ); }
    
    /** @return True if the duration tick should apply this effect. */
    public boolean isDurationEffectTick( int duration, int amplifier ) { return true; }
    
    /** Applies this effect to the entity. */
    public void applyEffectTick( LivingEntity entity, int amplifier ) {
        // We only want to apply this if the entity is at least one block above solid ground
        if( entity.level == null || entity.isOnGround() ||
                entity.level.getBlockState( entity.blockPosition().below() ).getMaterial().blocksMotion() ||
                entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.flying ) return;
        
        final Vector3d v = entity.getDeltaMovement();
        final double vTick = -0.03 * (amplifier + 1);
        final double vLimit = 10.0 * vTick;
        if( v.y > vLimit ) entity.setDeltaMovement( v.x, Math.max( vLimit, v.y + vTick ), v.z );
    }
}